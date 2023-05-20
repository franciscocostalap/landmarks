import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
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
    private static final String svcIP = "localhost";
    private static final int svcPort = 8080;
    private static LandmarkDetectionServiceGrpc.LandmarkDetectionServiceStub asyncStub;

    public static void main(String[] args) {
        ImageSubmitResponseStreamObserver responseObserver = new ImageSubmitResponseStreamObserver();
        Channel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort).usePlaintext().build();
        asyncStub = LandmarkDetectionServiceGrpc.newStub(channel);

        try{
            StreamObserver<ImageSubmissionChunk> streamObserver = asyncStub.submitImage(responseObserver);
            logger.info("Sending image chunks...");
            Path path = Paths.get("Lenna.png");

            byte[] buffer = new byte[1024];
            int bytesRead;

            try(InputStream in = Files.newInputStream(path)){
                while((bytesRead = in.read(buffer)) != -1) {
                    if(responseObserver.hasErrorOccured()) break;
                    logger.info("Sending chunk of size: " + bytesRead);
                    ByteString chunkData = ByteString.copyFrom(buffer, 0, bytesRead);
                    ImageSubmissionChunk chunk = ImageSubmissionChunk.newBuilder().setChunkData(chunkData).build();
                    streamObserver.onNext(chunk);
                    logger.info("Chunk sent.");
                }
            }catch(IOException e){
                logger.severe("Error reading file: " + e.getMessage());
                streamObserver.onError(e);
            } finally{
                logger.info("Stream completed. Closing stream.");
                streamObserver.onCompleted();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
