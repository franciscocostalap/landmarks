package pt.isel.cn.message;

import pt.isel.cn.MapsAPIClient;
import pt.isel.cn.firestore.FirestoreDocument;
import pt.isel.cn.firestore.FirestoreRepository;
import pt.isel.cn.vision.LandmarkPrediction;
import pt.isel.cn.vision.VisionAPIClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MessageService {

    private final FirestoreRepository firestoreRepository;
    private final VisionAPIClient visionAPIClient;
    private final MapsAPIClient mapsAPIClient;

    public MessageService(
            VisionAPIClient visionAPIClient,
            MapsAPIClient mapsAPIClient,
            FirestoreRepository firestoreRepository
    ){
        this.visionAPIClient = visionAPIClient;
        this.mapsAPIClient = mapsAPIClient;
        this.firestoreRepository = firestoreRepository;
    }

    public ArrayList<LandmarkPrediction> getLandmarksPredictions(String gcsUrl) throws IOException {
        return visionAPIClient.detectLandmarksGcs(gcsUrl);
    }

    public Boolean isAlreadyStored(String blobName) throws ExecutionException, InterruptedException {
        FirestoreDocument document = firestoreRepository.getByID(blobName);
        return document != null;
    }

    public void storeLandmarksPredictions(
            String documentName,
            ArrayList<LandmarkPrediction> landmarkPredictions
    ) throws ExecutionException, InterruptedException, IOException {
        System.out.println("Storing predictions for " + documentName);
        FirestoreDocument document = new FirestoreDocument(landmarkPredictions);
        firestoreRepository.save(document, documentName);
    }


}
