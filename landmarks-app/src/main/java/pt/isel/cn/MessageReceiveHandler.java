package pt.isel.cn;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.pubsub.v1.PubsubMessage;
import pt.isel.cn.firestore.FirestoreRepository;
import pt.isel.cn.vision.LandmarkPrediction;
import pt.isel.cn.vision.VisionAPIClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MessageReceiveHandler implements MessageReceiver {

    private final VisionAPIClient visionAPIClient;
    private final MapsAPIClient mapsAPIClient;
    private static FirestoreRepository repository;

    MessageReceiveHandler(VisionAPIClient visionAPIClient, MapsAPIClient mapsAPIClient, Firestore db) {
        this.visionAPIClient = visionAPIClient;
        this.mapsAPIClient = mapsAPIClient;
        repository = new FirestoreRepository(db, "ImageLandmarks");
    }

    public void receiveMessage(PubsubMessage msg, AckReplyConsumer ackReply) {
        System.out.println("Thread: " + Thread.currentThread().getName() + " Message (Id:" + msg.getMessageId() +
                " Data:" + msg.getData().toStringUtf8() + ")");

        String jsonContent = msg.getData().toStringUtf8();
        JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
        String request_id = json.get("request_id").getAsString();
        System.out.println("request_id: " + request_id);
        String[] msgParts = request_id.split(";");
        String bucketName = msgParts[0];
        String blobName = msgParts[1];

        String gcsUrl = "gs://" + bucketName + "/" + blobName;

        try {
            ArrayList<LandmarkPrediction> landmarks = visionAPIClient.detectLandmarksGcs(gcsUrl);

            for (LandmarkPrediction landmark : landmarks) {
                repository.save(landmark, blobName);
            }

            System.out.println(landmarks);
            mapsAPIClient.getStaticMapSaveImages(landmarks);
            ackReply.ack();
        } catch (IOException e) {
            ackReply.nack();
            throw new RuntimeException(e);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
