import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionResponse;

import java.util.concurrent.CountDownLatch;

public class ImageSubmitResponseStreamObserver implements StreamObserver<ImageSubmissionResponse> {

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
    }

    @Override
    public void onCompleted() {
        latch.countDown();
    }

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
