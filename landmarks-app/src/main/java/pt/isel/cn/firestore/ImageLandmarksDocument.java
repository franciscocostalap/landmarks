package pt.isel.cn.firestore;

import pt.isel.cn.vision.LandmarkPrediction;

import java.util.ArrayList;

public class ImageLandmarksDocument {
    private final ArrayList<LandmarkPrediction> landmarkPredictions;

    public ImageLandmarksDocument(ArrayList<LandmarkPrediction> landmarkPredictions) {
        this.landmarkPredictions = landmarkPredictions;
    }

    public ArrayList<LandmarkPrediction> getLandmarkPredictions() {
        return landmarkPredictions;
    }
}
