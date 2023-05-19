import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionResponse;

public class ImageSubmitResponseStreamObserver implements StreamObserver<ImageSubmissionResponse> {

    private Boolean isCompleted = false;
    private Boolean hasErrorOccurred = false;
    private String requestId;

    @Override
    public void onNext(ImageSubmissionResponse imageSubmissionResponse) {
        requestId = imageSubmissionResponse.getRequestId();
    }

    @Override
    public void onError(Throwable throwable) {
        hasErrorOccurred = true;
    }

    @Override
    public void onCompleted() {
        isCompleted = true;
    }

    public Boolean isCompleted(){
        return isCompleted;
    }

    public String getRequestId(){
        return requestId;
    }

    public Boolean hasErrorOccured(){
        return hasErrorOccurred;
    }
}
