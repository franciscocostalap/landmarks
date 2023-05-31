package observers;

import landmark_service.GetLandmarkListByConfidenceThresholdResponse;
import java.util.concurrent.CountDownLatch;

public class ThresholdImagesResponseStreamObserver implements LandmarkObserver<GetLandmarkListByConfidenceThresholdResponse> {
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onNext(GetLandmarkListByConfidenceThresholdResponse getLandmarkListByConfidenceThresholdResponse) {
        System.out.println("Landmarks found: " + getLandmarkListByConfidenceThresholdResponse.getMonumentList().size());
        getLandmarkListByConfidenceThresholdResponse.getMonumentList().forEach(System.out::println);

    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error: " + throwable.getMessage());
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
}
