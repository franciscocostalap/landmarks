package pt.isel.cn;


import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import landmark_service.*;
import landmark_service.Void;
import pt.isel.cn.firestore.FirestoreRepository;
import pt.isel.cn.firestore.LandmarkPrediction;
import pt.isel.cn.firestore.Pair;
import pt.isel.cn.utils.RandomNameGenerator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class LandmarkDetectionServiceImpl extends LandmarkDetectionServiceGrpc.LandmarkDetectionServiceImplBase{

    private static final Logger logger = Logger.getLogger(LandmarkDetectionServiceImpl.class.getName());

    private final Storage cloudStorage;
    private final String landmarkBucket;
    private final RandomNameGenerator randomNameGenerator;
    private final CloudPubSubPublisher cloudPubSubPublisher;
    private static FirestoreRepository firestoreRepository;
    private static CloudStorageAccess cloudStorageAccess;

    public LandmarkDetectionServiceImpl(
            Storage storage,
            RandomNameGenerator randomNameGenerator,
            String landmarkBucket,
            CloudPubSubPublisher cloudPubSubPublisher,
            Firestore db
    ){
        this.cloudStorage = storage;
        this.randomNameGenerator = randomNameGenerator;
        this.landmarkBucket = landmarkBucket;
        this.cloudPubSubPublisher = cloudPubSubPublisher;
        firestoreRepository = new FirestoreRepository(db);
        cloudStorageAccess = new CloudStorageAccess(storage);

    }

    @Override
    public StreamObserver<ImageSubmissionChunk> submitImage(StreamObserver<ImageSubmissionResponse> responseObserver) {
        logger.info( "Received image submission request.");
        String blobName = randomNameGenerator.generateName();
        logger.info( "Generated random name for blob: " + blobName);
        BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(landmarkBucket, blobName);
        return new CloudStorageStreamObserver(
                this.cloudStorage,
                blobInfoBuilder,
                responseObserver,
                cloudPubSubPublisher
        );
    }

    @Override
    public void getSubmissionResult(
            GetSubmissionResultRequest request,
            StreamObserver<GetSubmissionResultResponse> responseObserver)
    {
        logger.info( "Received submission result request with id " + request.getRequestId());
        String[] parts = request.getRequestId().split(";");

        try {
            String blobName = parts[1];

            List<LandmarkPrediction> landmarkPredictions =
                    firestoreRepository.getAll(blobName);

            byte[] landmarkStaticImage = cloudStorageAccess.getBlobContent(blobName + "-0");
            logger.info("Landmark static image retrieved from cloud storage.");
            List<Landmark> landmarks = landmarkPredictions.stream().map(prediction ->
                    {
                        float score = prediction.getScore();
                        return Landmark.newBuilder()
                                .setName(prediction.getName())
                                .setLatitude(prediction.getLatitude())
                                .setLongitude(prediction.getLatitude())
                                .setConfidence(score)
                                .build();
                    }
            ).collect(Collectors.toList());

            logger.info( landmarks.size() + " landmarks found.");

            GetSubmissionResultResponse submissionResultResponse = GetSubmissionResultResponse.newBuilder()
                    .addAllLandmarks(landmarks)
                    .setMapImage(ByteString.copyFrom(landmarkStaticImage))
                    .build();
            logger.info("Sending response to client.");
            responseObserver.onNext(submissionResultResponse);
            responseObserver.onCompleted();
            logger.info("Response sent to client.");

        } catch (RuntimeException e) {
            var status = Status.NOT_FOUND.withDescription("Landmark predictions not found.");
            responseObserver.onError(status.asException());
            e.printStackTrace();
        }
    }

    @Override
    public void getLandmarkListByConfidenceThresholdResult(
        GetSubmissionResultByConfidenceThresholdRequest request,
        StreamObserver<GetLandmarkListByConfidenceThresholdResponse> responseObserver
    ){
        logger.info("Received Request to get Landmark List by Confidence Threshold: " + request.getConfidence());

        List<Pair<String, List<LandmarkPrediction>>> filteredLandmarks =
                firestoreRepository.getByThresholdScore(request.getConfidence());

        if(filteredLandmarks.isEmpty()){
            logger.info("Landmark predictions not found.");
            var status = Status.NOT_FOUND.withDescription("Landmark predictions not found.");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        logger.info("Landmark predictions found.");
        List<MonumentFilteredByConfidenceThreshold> result = filteredLandmarks.stream().flatMap(pair -> {
            return pair.getSecond().stream().map(landmark ->{
                return MonumentFilteredByConfidenceThreshold.newBuilder()
                        .setMonumentImageName(pair.getFirst())
                        .setIdentifiedLandmark(landmark.getName())
                        .build();
                });
        }).collect(Collectors.toList());


        GetLandmarkListByConfidenceThresholdResponse response = GetLandmarkListByConfidenceThresholdResponse.newBuilder()
                .addAllMonument(result)
                .build();
        logger.info("Sending response to client.");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        logger.info("Response sent to client.");
    }


    @Override
    public void isAlive(Void request, StreamObserver<Text> responseObserver) {
        System.out.println("isAlive() called");
        Text reply = Text.newBuilder()
                .setMsg("I'm alive!")
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
