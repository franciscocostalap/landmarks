package observers;

import landmark_service.GetSubmissionResultResponse;
import landmark_service.Landmark;

import java.util.concurrent.CountDownLatch;



public class ImageSubmissionResponseStreamObserver implements LandmarkObserver<GetSubmissionResultResponse> {
    private final CountDownLatch latch = new CountDownLatch(1);

    private java.util.List<landmark_service.Landmark> landmarks = null;

    @Override
    public void onNext(GetSubmissionResultResponse value) {
        landmarks = new java.util.ArrayList<>();
        landmarks.addAll(value.getLandmarksList());
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
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

    public java.util.List<Landmark> getLandmarks(){
        if(landmarks == null)
            throw new IllegalStateException("Landmarks not yet received.");
        return landmarks;
    }

}

