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

public class MessageReceiveHandler implements MessageReceiver {

    private static final String idDelimiter = ";";
    private static final String messageProperty = "request_id";
    private final MessageService service;

    public MessageReceiveHandler(MessageService service) {
        this.service = service;
    }

    @Override
    public void receiveMessage(PubsubMessage msg, AckReplyConsumer ackReply) {
        System.out.println("Message received on Thread: " + Thread.currentThread().getName() +
                ".\n Message (Id:" + msg.getMessageId() + " Data:" + msg.getData().toStringUtf8() + ")");

        RequestID requestID = getMessageRequestID(msg);
        String gcsUrl = "gs://" + requestID.bucketName + "/" + requestID.blobName;

        try {
            if(!service.isAlreadyStored(requestID.blobName)){
                ArrayList<LandmarkPrediction> landmarksPredictions = service.getLandmarksPredictions(gcsUrl);
                service.storeLandmarksPredictions(requestID.blobName, landmarksPredictions);
            }else{ System.out.println("Predictions for " + requestID.blobName + " already stored, skipping..."); }
            ackReply.ack();
        } catch (ExecutionException | InterruptedException | IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private RequestID getMessageRequestID(PubsubMessage msg) {
        String jsonContent = msg.getData().toStringUtf8();
        JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
        String request_id = json.get(messageProperty).getAsString();
        String[] msgParts = request_id.split(idDelimiter);
        String bucketName = msgParts[0];
        String blobName = msgParts[1];
        return new RequestID(bucketName, blobName);
    }
}
