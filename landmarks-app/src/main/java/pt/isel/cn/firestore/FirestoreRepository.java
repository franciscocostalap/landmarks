package pt.isel.cn.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import pt.isel.cn.vision.LandmarkPrediction;

import java.util.concurrent.ExecutionException;

public class FirestoreRepository implements Repository<LandmarkPrediction, String>{

    private final Firestore firestore;

    public FirestoreRepository(Firestore firestore){
        this.firestore = firestore;
    }


    /**
     * Save a document in the Firestore database
     * The document´s name will be the landmarkPrediction´s name
     * @param landmarkPrediction The document [LandmarkPrediction] to save
     * @param collectionName The collection's name
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void save(LandmarkPrediction landmarkPrediction, String collectionName) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection(collectionName);
        DocumentReference docRef = collectionRef.document(landmarkPrediction.getName());

        ApiFuture<WriteResult> resultApiFuture = docRef.set(landmarkPrediction);
        WriteResult writeResult = resultApiFuture.get();

        System.out.println("Update time: " + writeResult.getUpdateTime());

    }
}