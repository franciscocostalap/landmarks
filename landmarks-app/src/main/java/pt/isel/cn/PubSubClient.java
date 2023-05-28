package pt.isel.cn;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import pt.isel.cn.message.MessageReceiveHandler;

import static pt.isel.cn.Constants.PROJECT_ID;
import static pt.isel.cn.Constants.SUBSCRIPTION_NAME;

public class PubSubClient {
    private final MessageReceiveHandler msgHandler;

    PubSubClient(MessageReceiveHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    /**
     * Starts the subscriber consumers.
     */
    public void startConsumer() {
        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(5)
                // uma só thread no handler
                .build();


        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_NAME);

        Subscriber subscriber =
                Subscriber.newBuilder(subscriptionName, msgHandler)
                        .setExecutorProvider(executorProvider)
                        .build();

        subscriber.startAsync().awaitTerminated();
    }
}

