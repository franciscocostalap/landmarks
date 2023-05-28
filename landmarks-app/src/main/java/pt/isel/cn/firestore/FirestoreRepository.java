package pt.isel.cn.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.util.concurrent.ExecutionException;

public class FirestoreRepository implements Repository<FirestoreDocument, String>{

    private final Firestore firestore;
    private final String collectionName;

    public FirestoreRepository(Firestore firestore, String collectionName){
        this.firestore = firestore;
        this.collectionName = collectionName;
    }

    /**
     * Get a document from the Firestore database
     * @param id The document id
     * @return a [FirestoreDocument] object
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public FirestoreDocument getByID(String id) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection(collectionName);
        DocumentReference docRef = collectionRef.document(id);

        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot documentSnapshot = future.get();
        return documentSnapshot.toObject(FirestoreDocument.class);
    }

    /**
     * Save a document in the Firestore database
     * @param document The document [FirestoreDocument] to save
     * @param name The document's name
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void save(FirestoreDocument document, String name) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection(collectionName);
        DocumentReference docRef = collectionRef.document(name);

        ApiFuture<WriteResult> resultApiFuture = docRef.set(document);
        WriteResult writeResult = resultApiFuture.get();

        System.out.println("Update time: " + writeResult.getUpdateTime());
    }
}