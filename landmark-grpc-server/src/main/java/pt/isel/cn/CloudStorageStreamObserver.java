package pt.isel.cn;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionChunk;
import landmark_service.ImageSubmissionResponse;
import java.io.IOException;

public class CloudStorageStreamObserver implements StreamObserver<ImageSubmissionChunk> {

    private final WriteChannel writeChannel;
    private final StreamObserver<ImageSubmissionResponse> responseObserver;
    private final BlobId blobId;

    public CloudStorageStreamObserver(
            Storage storage,
            BlobInfo blobInfo,
            StreamObserver<ImageSubmissionResponse> responseObserver
    ){
        writeChannel = storage.writer(blobInfo);
        this.responseObserver = responseObserver;
        blobId = blobInfo.getBlobId();
    }

    @Override
    public void onNext(ImageSubmissionChunk imageSubmissionChunk) {
        try{
            byte[] chunkData = imageSubmissionChunk.getChunkData().toByteArray();
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
            responseObserver.onCompleted();
            writeChannel.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted() {
        try {
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
