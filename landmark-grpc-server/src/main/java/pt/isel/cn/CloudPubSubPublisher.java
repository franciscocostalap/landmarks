package pt.isel.cn;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Encoding;
import com.google.pubsub.v1.TopicName;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static pt.isel.cn.Constants.*;

public class CloudPubSubPublisher {
    public void publish(String requestID){
        TopicName topicName = TopicName.of(PROJECT_ID, TOPIC_NAME);
        Publisher publisher = null;

        try{
            publisher = Publisher.newBuilder(topicName).build();

            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .putAttributes(MESSAGE_ATTRIBUTE_KEY, requestID)
                    .build();

            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            System.out.println("Published a message with id=" + messageId);

        } catch (IOException | InterruptedException | ExecutionException e){
            e.printStackTrace();
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                try {
                    publisher.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.getStackTrace();
                }
            }
        }
    }
}
