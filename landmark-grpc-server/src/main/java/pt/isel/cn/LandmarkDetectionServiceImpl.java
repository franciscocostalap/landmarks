package pt.isel.cn;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import landmark_service.*;
import landmark_service.Void;
import pt.isel.cn.utils.RandomNameGenerator;

import java.util.logging.Logger;

public class LandmarkDetectionServiceImpl extends LandmarkDetectionServiceGrpc.LandmarkDetectionServiceImplBase{

    private static final Logger logger = Logger.getLogger(LandmarkDetectionServiceImpl.class.getName());

    private final Storage cloudStorage;
    private final String landmarkBucket;
    private final RandomNameGenerator randomNameGenerator;

    public LandmarkDetectionServiceImpl(Storage storage, RandomNameGenerator randomNameGenerator, String landmarkBucket){
        this.cloudStorage = storage;
        this.randomNameGenerator = randomNameGenerator;
        this.landmarkBucket = landmarkBucket;
    }

    @Override
    public StreamObserver<ImageSubmissionChunk> submitImage(StreamObserver<ImageSubmissionResponse> responseObserver) {
        logger.info( "Received image submission request.");
        String blobName = randomNameGenerator.generateName();
        logger.info( "Generated random name for blob: " + blobName);
        BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(landmarkBucket, blobName);
        return new CloudStorageStreamObserver(this.cloudStorage, blobInfoBuilder, responseObserver);
    }

    @Override
    public void getSubmissionResult(GetSubmissionResultRequest request, StreamObserver<GetSubmissionResultResponse> responseObserver) {
        logger.info( "Received submission result request with id " + request.getRequestId());
        //send id to pubsub
        GetSubmissionResultResponse submissionResultResponse = GetSubmissionResultResponse.newBuilder()
                //... build result
                .build();
        responseObserver.onNext(submissionResultResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void isAlive(Void request, StreamObserver<Text> responseObserver) {
        System.out.println("isAlive() called");
        Text reply = Text.newBuilder()
                .setMsg("I'm alive!")
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
