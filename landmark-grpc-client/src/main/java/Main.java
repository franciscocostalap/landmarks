import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import landmark_service.*;
import landmark_service.Void;
import observers.LandmarkListResponseStreamObserver;
import observers.ImageSubmitResponseStreamObserver;
import observers.LandmarkImageResponseObserver;
import observers.ThresholdImagesResponseStreamObserver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
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
   // private final ChannelManager channelManager;
    private ManagedChannel channel;

    private App(int svcPort) {
        __AppMethods = new HashMap<Option, ClientWorker>();
        __AppMethods.put(Option.SubmitImage, new ClientWorker() {public void doWork() {App.this.SubmitImage();}});
        __AppMethods.put(Option.GetSubmissionResult, new ClientWorker() {public void doWork() {App.this.GetSubmissionResult();}});
        __AppMethods.put(Option.GetLandmarkListByConfidenceThresholdResult, new ClientWorker() {public void doWork() {App.this.GetLandmarkListByConfidenceThresholdResult();}});
       // channelManager = new ChannelManager(IPLookupURL, svcPort);
    }

    private void StartUp() throws Exception {
        System.out.println("Starting up...");
       // channel = channelManager.getChannel();

        ImageSubmitResponseStreamObserver responseObserver = new ImageSubmitResponseStreamObserver();
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7500).usePlaintext().build();
        logger.info("Waiting for connection to be ready...");

        LandmarkDetectionServiceGrpc.LandmarkDetectionServiceBlockingStub blockingStub = LandmarkDetectionServiceGrpc.newBlockingStub(channel);


            blockingStub.isAlive(Void.newBuilder().build());



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
                System.out.println("Press any key to continue...");
                System.in.read();
            }catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
                System.out.flush();
                System.out.println("Press any key to continue...");
                System.in.read();
            }
            catch( NullPointerException ex) {
                //Nothing to do. The option was not a valid one. Read another.
            }

        }while(userInput!=Option.Exit);
    }


    private void PrintLandMark(Landmark landmark){
        System.out.println("  Landmark: ");
        System.out.println("  Name: " + landmark.getName());
        System.out.println("  Latitude: " + landmark.getLatitude() + " Longitude: " + landmark.getLongitude());
        System.out.println("  Confidence: " + landmark.getConfidence());
    }

    private void PrintLandMarks(List<Landmark> landmarkList) {
        int idx = 0;
        for (Landmark landmark : landmarkList) {
            System.out.println("Landmark nr:" +  idx++ + ".");
            PrintLandMark(landmark);
            System.out.println();
        }
    }



    //---------------------

    private Path verifyImage(String imagePath){
        try {
            Path path = Paths.get(imagePath);
            String contentType = Files.probeContentType(path);
            if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                logger.severe("File is not a .jpeg or .png image.");
                System.out.println("File is not a .jpeg or .png image.");
                throw new IllegalArgumentException("File is not a .jpeg or .png image.");
            }
            return path;
        } catch(Exception ex) {
            logger.severe("File not found.");
            throw new IllegalArgumentException("File not found.");
        }
    }

    private void SubmitImage() {
        ImageSubmitResponseStreamObserver responseObserver = new ImageSubmitResponseStreamObserver();
        Scanner s = new Scanner(System.in);

        System.out.println("Enter the path to the image you want to submit(.JPEG or .PNG):");
        String imagePath = s.nextLine();
        Path path = verifyImage(imagePath);

        try{

            StreamObserver<ImageSubmissionChunk> serverStreamObserver = asyncStub.submitImage(responseObserver);
            logger.info("Sending image chunks...");

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

    private void verifyID(String id){
        if(id == null || id.isEmpty()){
            logger.severe("ID is null or empty.");
            throw new IllegalArgumentException("ID is null or empty.");
        }
        try{
            String[] parts = id.split(";");
            String p1 = parts[0];
            String p2 = parts[1];
        }catch (Exception e){
            throw new IllegalArgumentException("Invalid ID");
        }
    }

    private void GetSubmissionResult() {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the ID of the submission you want to get the result:");
        String id = s.nextLine();
        verifyID(id);

        GetSubmissionResultRequest submissionID = GetSubmissionResultRequest.newBuilder().setRequestId(id).build();
        LandmarkListResponseStreamObserver landmarkListResultObserver = new LandmarkListResponseStreamObserver();

        try {
            asyncStub.getSubmissionResult(submissionID, landmarkListResultObserver);

            landmarkListResultObserver.waitForCompletion();
        } catch (Exception e) {
            System.out.println("Could not get submission result: " + e.getMessage());
        }
        List<Landmark> landmarkList = landmarkListResultObserver.getLandmarks();
        if(landmarkList.size() == 0){
            System.out.println("No landmarks found.");
            return;
        }
        System.out.println("Landmarks found: " + landmarkList.size());

        PrintLandMarks(landmarkList);

        System.out.println("Enter the number of the landmark you want to get the map image from:");
        int chosenIDX = s.nextInt();
        if(chosenIDX < 0 || chosenIDX >= landmarkList.size()){
            throw new IllegalArgumentException("Invalid option.");
        }
        String fileName = id + "-" + chosenIDX + "-map";
        LandmarkImageResponseObserver landmarkImageObserver = new LandmarkImageResponseObserver(fileName);
        GetLandmarkImageRequest requestInfo = GetLandmarkImageRequest
                .newBuilder()
                .setRequestId(id)
                .setLandmarkIdx(chosenIDX)
                .build();
        try{
            asyncStub.getLandmarkImage(requestInfo, landmarkImageObserver);

            landmarkImageObserver.waitForCompletion();
        }catch (Exception e) {
            System.out.println("Could not get landmark image: " + e.getMessage());
        }
    }

    private double getThreshold(){
        try{
            Scanner s = new Scanner(System.in);
            System.out.println("Enter the confidence threshold(e.g 0,3):");
            double thresholdInput = s.nextDouble();
            if(thresholdInput < 0 || thresholdInput > 1){
                logger.severe("Threshold is not between 0 and 1.");
                throw new IllegalArgumentException("Threshold is not between 0 and 1.");
            }
            return thresholdInput;
        }catch (Exception e){
            System.out.println("Invalid threshold.");
            return getThreshold();
        }
    }

    private void GetLandmarkListByConfidenceThresholdResult() {
        double thresholdInput = getThreshold();
        GetSubmissionResultByConfidenceThresholdRequest threshold = GetSubmissionResultByConfidenceThresholdRequest.newBuilder().setConfidence(thresholdInput).build();
        ThresholdImagesResponseStreamObserver resultObserver = new ThresholdImagesResponseStreamObserver();
        try{
            asyncStub.getLandmarkListByConfidenceThresholdResult(threshold,resultObserver);
            resultObserver.waitForCompletion();
        }catch (Exception e){
            System.out.println("Could not get submission result: " + e.getMessage());
        }
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

