import com.google.cloud.storage.Storage;
import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionChunk;
import landmark_service.ImageSubmissionResponse;
import landmark_service.LandmarkDetectionServiceGrpc;

import java.util.logging.Logger;

public class LandmarkDetectionServiceImpl extends LandmarkDetectionServiceGrpc.LandmarkDetectionServiceImplBase{

    private static final Logger logger = Logger.getLogger(LandmarkDetectionServiceImpl.class.getName());

    private Storage cloudStorage;

    public LandmarkDetectionServiceImpl(Storage storage){
        this.cloudStorage = storage;
    }


    @Override
    public StreamObserver<ImageSubmissionChunk> submitImage(StreamObserver<ImageSubmissionResponse> responseObserver) {
        logger.info( "Received image submission request.");
        return new CloudStorageStreamObserver(this.cloudStorage, responseObserver);
    }

}
