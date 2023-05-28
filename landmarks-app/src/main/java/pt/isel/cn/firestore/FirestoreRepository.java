package pt.isel.cn.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import pt.isel.cn.vision.LandmarkPrediction;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toCollection;

public class FirestoreRepository implements Repository<FirestoreDocument, String>{

    private final Firestore firestore;
    private final String collectionName;

    public FirestoreRepository(Firestore firestore, String collectionName){
        this.firestore = firestore;
        this.collectionName = collectionName;
    }

    @Override
    public FirestoreDocument getByID(String id) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection(collectionName);
        DocumentReference docRef = collectionRef.document(id);

        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot documentSnapshot = future.get();
        System.out.println(documentSnapshot.toObject(FirestoreDocument.class));
        return documentSnapshot.toObject(FirestoreDocument.class);
    }

    @Override
    public void save(FirestoreDocument document, String name) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection(collectionName);
        DocumentReference docRef = collectionRef.document(name);

        ApiFuture<WriteResult> resultApiFuture = docRef.set(document);
        WriteResult writeResult = resultApiFuture.get();

        System.out.println("Update time: " + writeResult.getUpdateTime());
    }
}