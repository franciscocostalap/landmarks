package pt.isel.cn.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import pt.isel.cn.vision.LandmarkPrediction;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static pt.isel.cn.Constants.NO_LANDMARKS_RESULT;

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
    public void save(@Nullable LandmarkPrediction landmarkPrediction, String collectionName) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection(collectionName);
       if(landmarkPrediction == null){
           collectionRef
                   .document(NO_LANDMARKS_RESULT)
                   .set(new HashMap<>());
          return;
       }
        DocumentReference docRef = collectionRef.document(landmarkPrediction.getName());

        ApiFuture<WriteResult> resultApiFuture = docRef.set(landmarkPrediction);
        WriteResult writeResult = resultApiFuture.get();

        System.out.println("Update time: " + writeResult.getUpdateTime());
    }
}