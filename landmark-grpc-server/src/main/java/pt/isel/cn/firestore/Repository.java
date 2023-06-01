package pt.isel.cn.firestore;

import pt.isel.cn.NoLandMarkFoundException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface Repository<Document, Key> {
    List<LandmarkPrediction> getAll(String collectionName) throws NoLandMarkFoundException;
    List<Pair<String, List<LandmarkPrediction>>> getByThresholdScore(double scoreThreshold) throws NoLandMarkFoundException;
}