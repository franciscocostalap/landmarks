import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import landmark_service.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


interface ClientWorker {
    void doWork();
}

class App {

    private static final String IPLookupURL = "https://europe-west1-cn2223-t1-g06.cloudfunctions.net/cn-landkmarks-http-lookup";
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static LandmarkDetectionServiceGrpc.LandmarkDetectionServiceStub asyncStub;


    private enum Option {
        Unknown,
        Exit,
        SubmitImage,
        GetSubmissionResult,
        GetLandmarkListByConfidenceThresholdResult
    }

    private static App __instance = null;
    private final HashMap<Option,ClientWorker> __AppMethods;
    private final ChannelManager channelManager;
    private ManagedChannel channel;

    private App(int svcPort) {
        __AppMethods = new HashMap<Option, ClientWorker>();
        __AppMethods.put(Option.SubmitImage, new ClientWorker() {public void doWork() {App.this.SubmitImage();}});
        __AppMethods.put(Option.GetSubmissionResult, new ClientWorker() {public void doWork() {App.this.GetSubmissionResult();}});
        __AppMethods.put(Option.GetLandmarkListByConfidenceThresholdResult, new ClientWorker() {public void doWork() {App.this.GetLandmarkListByConfidenceThresholdResult();}});
        channelManager = new ChannelManager(IPLookupURL, svcPort);
    }

    private void StartUp() throws Exception {
        System.out.println("Starting up...");
        channel = channelManager.getChannel();
        asyncStub = LandmarkDetectionServiceGrpc.newStub(channel);
        System.out.println("Starting up complete.");
        System.out.println();
    }

    public static App getInstance(int svcPort) {
        logger.setLevel(Level.INFO); //TODO: change here the log level
        if(__instance == null) {
            __instance = new App(svcPort);
        }
        return __instance;
    }

    private Option DisplayMenu() {
        Option option = Option.Unknown;
        try {
            System.out.println("Landmarks vision app");
            System.out.println();
            System.out.println("1. Exit");
            System.out.println("2. Submit Image");
            System.out.println("3. Get Submission Result");
            System.out.println("4. Get Landmark List By Confidence Threshold Result");
            System.out.print(">");
            Scanner s = new Scanner(System.in);
            int result = s.nextInt();
            option = Option.values()[result];
        }
        catch(RuntimeException ex) {
            //nothing to do.
        }

        return option;
    }

    private static void clearConsole() throws Exception {
        for (int y = 0; y < 25; y++) //console is 80 columns and 25 lines
            System.out.println("\n");
    }

    public void Run() throws Exception {
        StartUp();
        Option userInput = Option.Unknown;
        do {
            clearConsole();
            userInput = DisplayMenu();
            clearConsole();
            try {
                __AppMethods.get(userInput).doWork();
                System.in.read();
            }
            catch(NullPointerException ex) {
                //Nothing to do. The option was not a valid one. Read another.
            }

        }while(userInput!=Option.Exit);
    }


    private void printResults(ResultSet dr) {
        //TODO
    }

    private void PrintLandMark(Landmark landmark){
        System.out.println("Landmark: ");
        System.out.println("Name: " + landmark.getName());
        System.out.println("Latitude: " + landmark.getLatitude() + " Longitude: " + landmark.getLongitude());
        System.out.println("Confidence: " + landmark.getConfidence());
    }


    //---------------------

    private void SubmitImage() {
        ImageSubmitResponseStreamObserver responseObserver = new ImageSubmitResponseStreamObserver();
        Scanner s = new Scanner(System.in);


        try{
            StreamObserver<ImageSubmissionChunk> serverStreamObserver = asyncStub.submitImage(responseObserver);
            logger.info("Sending image chunks...");

            System.out.println("Enter the path to the image you want to submit:");
            String imagePath = s.nextLine();

            Path path = Paths.get(imagePath);
            String contentType = Files.probeContentType(path);
            if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")){
                logger.severe("File is not a .jpeg or .png image.");
                System.out.println("File is not a .jpeg or .png image.");
                return;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            System.out.println("Sending image...");
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
                System.out.println("Image sent.");
                logger.info("Stream completed. Closing stream.");
                serverStreamObserver.onCompleted();

                responseObserver.waitForCompletion();
            }
            logger.info("Response received: " + responseObserver.getRequestId());
            System.out.println("Response received, ID: " + responseObserver.getRequestId());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void GetSubmissionResult() {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the ID of the submission you want to get the result:");
        String id = s.nextLine();
        GetSubmissionResultRequest submissionID = GetSubmissionResultRequest.newBuilder().setRequestId(id).build();
        asyncStub.getSubmissionResult(submissionID, new StreamObserver<GetSubmissionResultResponse>() {
            @Override
            public void onNext(GetSubmissionResultResponse value) {
                value.getLandmarksList().forEach(landmark -> PrintLandMark(landmark));
                ByteString imgByteString = value.getMapImage();

                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgByteString.toByteArray()));

                    File outputfile = new File(id + "-map.png");
                    ImageIO.write(img, "png", outputfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
            }
        });
    }


    private void GetLandmarkListByConfidenceThresholdResult() {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the confidence threshold:");
        double thresholdInput = s.nextDouble();
        GetSubmissionResultByConfidenceThresholdRequest threshold = GetSubmissionResultByConfidenceThresholdRequest.newBuilder().setConfidence(thresholdInput).build();
//        asyncStub.getLandmarkListByConfidenceThresholdResult(threshold, new StreamObserver<GetLandmarkListByConfidenceThresholdResponse>() {
//            @Override
//            public void onNext(GetLandmarkListByConfidenceThresholdResponse value) {
//                value
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                System.out.println("Error: " + t.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//            }
//        });
//TODO: finish this
    }


}


public class Main {
    public static void main(String[] args) throws SQLException,Exception {
        //get the port from args
        int svcPort = 0;
        if(args.length > 0) {
            svcPort = Integer.parseInt(args[0]);
            if(svcPort < 0 || svcPort > 65535) {
                System.out.println("Invalid port number.");
                return;
            }
        }
        else {
            System.out.println("Usage: java <Jar> <port>");
            return;
        }

        App.getInstance(svcPort).Run();
    }
}

