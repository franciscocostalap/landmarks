package pt.isel.cn.firestore;

import pt.isel.cn.vision.LandmarkPrediction;

import java.util.ArrayList;

public class FirestoreDocument {
    private final ArrayList<LandmarkPrediction> landmarkPredictions;

    public FirestoreDocument() {
        this.landmarkPredictions = new ArrayList<>();
    }

    public FirestoreDocument(ArrayList<LandmarkPrediction> landmarkPredictions) {
        this.landmarkPredictions = landmarkPredictions;
    }

    public ArrayList<LandmarkPrediction> getLandmarkPredictions() {
        return landmarkPredictions;
    }

    public String toString() {
        return "FirestoreDocument{" +
                "landmarkPredictions=" + landmarkPredictions +
                '}';
    }
}
