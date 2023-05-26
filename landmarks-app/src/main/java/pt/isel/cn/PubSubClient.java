package pt.isel.cn;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import pt.isel.cn.vision.VisionAPIClient;

import static pt.isel.cn.Constants.PROJECT_ID;

public class PubSubClient {
    private final VisionAPIClient visionAPIClient;
    private final MapsAPIClient mapsAPIClient;
    private final Firestore db;

    PubSubClient(VisionAPIClient visionAPIClient, MapsAPIClient mapsAPIClient, Firestore db) {
        this.visionAPIClient = visionAPIClient;
        this.mapsAPIClient = mapsAPIClient;
        this.db = db;
    }

    public void startConsumer() {
        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(5)
                // uma só thread no handler
                .build();


        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(PROJECT_ID, Constants.subscriptionName);

        Subscriber subscriber =
                Subscriber.newBuilder(subscriptionName, new MessageReceiveHandler(visionAPIClient, mapsAPIClient, db))
                        .setExecutorProvider(executorProvider)
                        .build();

        subscriber.startAsync().awaitTerminated();
    }
}

