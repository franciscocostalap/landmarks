package pt.isel.cn.message;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.pubsub.v1.PubsubMessage;
import pt.isel.cn.vision.LandmarkPrediction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static pt.isel.cn.Constants.MESSAGE_ATTRIBUTE_KEY;

public class MessageReceiveHandler implements MessageReceiver {

    private static final String idDelimiter = ";";
    private static final String messageProperty = "request_id";
    private final MessageService service;

    public MessageReceiveHandler(MessageService service) {
        this.service = service;
    }

    /**
     * Parses the message to get the request id and handles
     * @param msg message received from PubSub
     * @param ackReply used to acknowledge the message
     */
    @Override
    public void receiveMessage(PubsubMessage msg, AckReplyConsumer ackReply) {
        System.out.println("Message received on Thread: " + Thread.currentThread().getName() +
                ".\n Message (Id:" + msg.getMessageId() + " Data:" + msg.getData().toStringUtf8() + ")");

        RequestID requestID = getMessageRequestID(msg);
        String gcsUrl = "gs://" + requestID.bucketName + "/" + requestID.blobName;

        try {
            ArrayList<LandmarkPrediction> landmarkPredictions =
                    service.fetchLandmarksToFirestore(requestID.blobName, gcsUrl);
            service.fetchStaticMapsToCloudStorage(requestID.blobName, landmarkPredictions);

            ackReply.ack();
        } catch (NoLandMarkFoundException e) {
            ackReply.ack(); //TODO:
        } catch (ExecutionException | InterruptedException | RuntimeException | IOException e ) {
            ackReply.nack();
            e.printStackTrace();
        }

    }

    /**
     * Parses the message to get the request id
     * @param msg message received from PubSub
     * @return the request id [RequestID] parsed from the message
     */
    private RequestID getMessageRequestID(PubsubMessage msg) {
        String request_id = msg.getAttributesOrThrow(MESSAGE_ATTRIBUTE_KEY);
        String[] msgParts = request_id.split(idDelimiter);
        String bucketName = msgParts[0];
        String blobName = msgParts[1];
        return new RequestID(bucketName, blobName);
    }
}
