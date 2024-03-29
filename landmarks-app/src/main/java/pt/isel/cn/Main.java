package pt.isel.cn;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import pt.isel.cn.firestore.FirestoreRepository;
import pt.isel.cn.message.MessageReceiveHandler;
import pt.isel.cn.message.MessageService;
import pt.isel.cn.vision.VisionAPIClient;

import java.io.IOException;

import static pt.isel.cn.Constants.FIRESTORE_COLLECTION_NAME;

public class Main {

    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("GOOGLE_API_KEY");

        GoogleCredentials credentials =
                GoogleCredentials.getApplicationDefault();
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setCredentials(credentials).build();
        Firestore db = options.getService();

        Storage storage = StorageOptions.getDefaultInstance().getService();

        final VisionAPIClient visionAPIClient = new VisionAPIClient();
        final MapsAPIClient mapsAPIClient = new MapsAPIClient(apiKey);
        final FirestoreRepository firestoreRepository = new FirestoreRepository(db);

        final MessageService service = new MessageService(visionAPIClient, mapsAPIClient, firestoreRepository, storage);
        final MessageReceiveHandler msgHandler = new MessageReceiveHandler(service);

        final PubSubClient pubSubClient = new PubSubClient(msgHandler);
        pubSubClient.startConsumer();
    }
}

