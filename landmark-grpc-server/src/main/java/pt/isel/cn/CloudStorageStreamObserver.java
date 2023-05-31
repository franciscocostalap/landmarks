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

    private final StreamObserver<ImageSubmissionResponse> responseObserver;
    private BlobId blobId;
    private final StreamObjectUpload streamObjectUpload;
    private final CloudPubSubPublisher cloudPubSubPublisher;

    public CloudStorageStreamObserver(
            Storage storage,
            BlobInfo.Builder blobInfoBuilder,
            StreamObserver<ImageSubmissionResponse> responseObserver,
            CloudPubSubPublisher cloudPubSubPublisher
    ){
        this.responseObserver = responseObserver;
        this.streamObjectUpload = new StreamObjectUpload(storage, blobInfoBuilder);
        this.cloudPubSubPublisher = cloudPubSubPublisher;
    }

    @Override
    public void onNext(ImageSubmissionChunk imageSubmissionChunk) {
        try{
            byte[] chunkData = imageSubmissionChunk.getChunkData().toByteArray();
            this.blobId = streamObjectUpload.storeObject(chunkData, chunkData.length);
        }catch(IOException e){
            responseObserver.onError(e);
            responseObserver.onCompleted();
            try {
                streamObjectUpload.closeWriteChannel();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        try{
            logger.info("Error occurred: " + throwable.getMessage());
            responseObserver.onCompleted();
            streamObjectUpload.closeWriteChannel();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted() {
        try {
            String blobName = blobId.getName();
            String blobBucket = blobId.getBucket();
            String newID = blobBucket + ";" + blobName;
            ImageSubmissionResponse imageSubmissionResponse =
                    ImageSubmissionResponse.newBuilder().setRequestId(newID).build();
            responseObserver.onNext(imageSubmissionResponse);
            responseObserver.onCompleted();
            logger.info("Completed upload of blob: " + blobId.getName());
            cloudPubSubPublisher.publish(newID);
            streamObjectUpload.closeWriteChannel();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
