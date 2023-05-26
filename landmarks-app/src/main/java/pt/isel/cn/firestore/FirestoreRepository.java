package pt.isel.cn.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import pt.isel.cn.vision.LandmarkPrediction;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class FirestoreRepository implements Repository<LandmarkPrediction>{

    private final Firestore firestore;
    private final String collectionName;

    public FirestoreRepository(Firestore firestore, String collectionName){
        this.firestore = firestore;
        this.collectionName = collectionName;
    }

    @Override
    public void save(LandmarkPrediction document, String name) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection(collectionName);
        DocumentReference docRef = collectionRef.document(name);

        //get document
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot documentSnapshot = future.get();

        ApiFuture<WriteResult> resultApiFuture = null;
        if(!documentSnapshot.exists()){
            ArrayList<LandmarkPrediction> landmarkPredictions = new ArrayList<LandmarkPrediction>();
            landmarkPredictions.add(document);
            resultApiFuture = docRef.create(new ImageLandmarksDocument(landmarkPredictions));

        }else{
            //n adicionar se ja existe
            Map<String, Object> documentData = Objects.requireNonNull(documentSnapshot.getData());

            ArrayList<LandmarkPrediction> landmarkPredictions =
                    ((ArrayList<LandmarkPrediction>) documentData.get("landmarkPredictions"));

            System.out.println(landmarkPredictions);
            /*if(!landmarkPredictions.contains(document)){
                landmarkPredictions.add(document);
                resultApiFuture = docRef.set(new ImageLandmarksDocument(landmarkPredictions));
            }*/
        }

        if(resultApiFuture == null) return;

        WriteResult writeResult = resultApiFuture.get();
        System.out.println("Update time: " + writeResult.getUpdateTime());
    }

}