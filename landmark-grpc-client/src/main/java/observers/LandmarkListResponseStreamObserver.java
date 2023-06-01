package observers;

import com.google.protobuf.ByteString;
import landmark_service.GetSubmissionResultResponse;
import landmark_service.Landmark;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;



public class LandmarkListResponseStreamObserver implements LandmarkObserver<GetSubmissionResultResponse> {
    private final CountDownLatch latch = new CountDownLatch(1);

    private final String id;

    private void PrintLandMark(Landmark landmark){
        System.out.println("Landmark: ");
        System.out.println("Name: " + landmark.getName());
        System.out.println("Latitude: " + landmark.getLatitude() + " Longitude: " + landmark.getLongitude());
        System.out.println("Confidence: " + landmark.getConfidence());
    }

    public LandmarkListResponseStreamObserver(String id ){
        this.id = id;
    }

    @Override
    public void onNext(GetSubmissionResultResponse value) {
        if(value.getLandmarksList().size() == 0){
            System.out.println("No landmarks found.");
            return;
        }
        System.out.println("Landmarks found: " + value.getLandmarksList().size());
        value.getLandmarksList().forEach(this::PrintLandMark);
        ByteString imgByteString = value.getMapImage();

        try {
            System.out.println("Saving map image...");
            PrintStream writeTo = new PrintStream(Files.newOutputStream(Paths.get(id + "-map.png")));
            imgByteString.writeTo(writeTo);
            System.out.println("Map image saved!");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}

