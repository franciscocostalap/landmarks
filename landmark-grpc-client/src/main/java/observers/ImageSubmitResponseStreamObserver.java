package observers;

import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionResponse;

import java.util.concurrent.CountDownLatch;

public class ImageSubmitResponseStreamObserver implements LandmarkObserver<ImageSubmissionResponse> {

    private Boolean hasErrorOccurred = false;
    private String requestId;
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onNext(ImageSubmissionResponse imageSubmissionResponse) {
        requestId = imageSubmissionResponse.getRequestId();
    }

    @Override
    public void onError(Throwable throwable) {
        hasErrorOccurred = true;
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        latch.countDown();
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        latch.await();
    }

    public String getRequestId(){
        return requestId;
    }

    public Boolean hasErrorOccured(){
        return hasErrorOccurred;
    }
}
