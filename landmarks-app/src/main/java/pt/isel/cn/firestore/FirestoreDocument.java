package pt.isel.cn.firestore;

import pt.isel.cn.vision.LandmarkPrediction;

import java.util.ArrayList;

public class FirestoreDocument {
    private ArrayList<LandmarkPrediction> landmarkPredictions;

    public FirestoreDocument() {}

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
