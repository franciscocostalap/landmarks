import com.google.protobuf.BlockingService;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import landmark_service.ImageSubmissionChunk;
import landmark_service.LandmarkDetectionServiceGrpc;
import landmark_service.Void;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final String svcIP = "localhost";
    private static final int svcPort = 15004;
    private static LandmarkDetectionServiceGrpc.LandmarkDetectionServiceStub asyncStub;
    private static LandmarkDetectionServiceGrpc.LandmarkDetectionServiceBlockingStub blockingStub;

    public static void main(String[] args) {
        ImageSubmitResponseStreamObserver responseObserver = new ImageSubmitResponseStreamObserver();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort).usePlaintext().build();
        logger.info("Waiting for connection to be ready...");

        asyncStub = LandmarkDetectionServiceGrpc.newStub(channel);
        blockingStub = LandmarkDetectionServiceGrpc.newBlockingStub(channel);

        try{
            blockingStub.isAlive(Void.newBuilder().build());
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
