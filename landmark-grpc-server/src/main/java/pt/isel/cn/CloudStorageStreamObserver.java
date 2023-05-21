package pt.isel.cn;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionChunk;
import landmark_service.ImageSubmissionResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class CloudStorageStreamObserver implements StreamObserver<ImageSubmissionChunk> {

    private static final Logger logger = Logger.getLogger(CloudStorageStreamObserver.class.getName());

    private WriteChannel writeChannel;
    private final StreamObserver<ImageSubmissionResponse> responseObserver;
    private BlobId blobId;
    private final BlobInfo.Builder blobInfoBuilder;
    private final Storage storage;

    public CloudStorageStreamObserver(
            Storage storage,
            BlobInfo.Builder blobInfoBuilder,
            StreamObserver<ImageSubmissionResponse> responseObserver
    ){
        this.storage = storage;
        this.blobInfoBuilder = blobInfoBuilder;
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(ImageSubmissionChunk imageSubmissionChunk) {
        try{
            logger.info("Received chunk of size: " + imageSubmissionChunk.getChunkData().size());
            byte[] chunkData = imageSubmissionChunk.getChunkData().toByteArray();

            if(writeChannel == null){
                String contentType = ImageContentTypeChecker.getContentType(chunkData);
                BlobInfo blobInfo = blobInfoBuilder.setContentType(contentType).build();
                this.writeChannel = this.storage.writer(blobInfo);
                this.blobId = blobInfo.getBlobId();
            }

            writeChannel.write(java.nio.ByteBuffer.wrap(chunkData, 0, chunkData.length));
        }catch(IOException e){
            try{
                responseObserver.onError(e);
                responseObserver.onCompleted();
                writeChannel.close();
            }catch (IOException e2){
                e2.printStackTrace();
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        try{
            logger.info("Error occurred: " + throwable.getMessage());
            responseObserver.onCompleted();
            writeChannel.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted() {
        try {
            logger.info("Completed upload of blob: " + blobId.getName());

            String blobName = blobId.getName();
            String blobBucket = blobId.getBucket();
            ImageSubmissionResponse imageSubmissionResponse =
                    ImageSubmissionResponse.newBuilder().setRequestId(blobName + "-" + blobBucket).build();
            responseObserver.onNext(imageSubmissionResponse);
            responseObserver.onCompleted();
            writeChannel.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
