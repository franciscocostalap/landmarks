package pt.isel.cn.firestore;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface Repository<Document, Key> {
    List<LandmarkPrediction> getAll(String collectionName);
    List<Pair<String, List<LandmarkPrediction>>> getByThresholdScore(double scoreThreshold);
}