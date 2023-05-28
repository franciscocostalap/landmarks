import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionChunk;
import landmark_service.LandmarkDetectionServiceGrpc;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final int svcPort = 7500;

    private static final String IPLookupURL = "https://europe-west1-cn2223-t1-g06.cloudfunctions.net/cn-landkmarks-http-lookup";

    private static final ChannelManager channelManager = new ChannelManager(IPLookupURL, svcPort);


    private static LandmarkDetectionServiceGrpc.LandmarkDetectionServiceStub asyncStub;

    public static void main(String[] args) {
        logger.info("Starting client...");

        ManagedChannel channel = channelManager.getChannel();

        ImageSubmitResponseStreamObserver responseObserver = new ImageSubmitResponseStreamObserver();

        asyncStub = LandmarkDetectionServiceGrpc.newStub(channel);
        try{
            logger.info("Connection ready...");
            StreamObserver<ImageSubmissionChunk> serverStreamObserver = asyncStub.submitImage(responseObserver);

            logger.info("Sending image chunks...");
            Path path = Paths.get("transferir.jpg");
            String contentType = Files.probeContentType(path);
            if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")){
                logger.severe("File is not a .jpeg or .png image.");
                return;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;

            try(InputStream in = Files.newInputStream(path)){
                while((bytesRead = in.read(buffer)) != -1) {
                    if(responseObserver.hasErrorOccured()) break;
                    logger.info("Sending chunk of size: " + bytesRead);
                    ByteString chunkData = ByteString.copyFrom(buffer, 0, bytesRead);
                    ImageSubmissionChunk chunk = ImageSubmissionChunk.newBuilder().setChunkData(chunkData).build();
                    serverStreamObserver.onNext(chunk);
                    logger.info("Chunk sent.");
                }
            }catch(IOException e){
                logger.severe("Error reading file: " + e.getMessage());
                serverStreamObserver.onError(e);
            } finally{
                logger.info("Stream completed. Closing stream.");
                serverStreamObserver.onCompleted();

                responseObserver.waitForCompletion();
            }
            logger.info("Response received: " + responseObserver.getRequestId());


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
