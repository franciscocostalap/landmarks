package pt.isel.cn;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import pt.isel.cn.firestore.FirestoreRepository;
import pt.isel.cn.vision.VisionAPIClient;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("GOOGLE_API_KEY");

        GoogleCredentials credentials =
                GoogleCredentials.getApplicationDefault();
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setCredentials(credentials).build();
        Firestore db = options.getService();

        final VisionAPIClient visionAPIClient = new VisionAPIClient();
        final MapsAPIClient mapsAPIClient = new MapsAPIClient(apiKey);

        final PubSubClient pubSubClient = new PubSubClient(visionAPIClient, mapsAPIClient, db);
        pubSubClient.startConsumer();
    }
}

