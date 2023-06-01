package observers;

import com.google.protobuf.ByteString;
import landmark_service.GetLandmarkImageResponse;
import landmark_service.GetSubmissionResultResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;


public class LandmarkImageResponseObserver implements LandmarkObserver<GetLandmarkImageResponse> {

    private final CountDownLatch latch = new CountDownLatch(1);

    public final String fileName;

    public LandmarkImageResponseObserver(String fileName){
        this.fileName = fileName;
    }

    @Override
    public void onNext(GetLandmarkImageResponse value) {
        ByteString imgByteString = value.getLandmarkImage();

        try {
            System.out.println("Saving map image...");
            PrintStream writeTo = new PrintStream(Files.newOutputStream(Paths.get(fileName + ".png")));
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