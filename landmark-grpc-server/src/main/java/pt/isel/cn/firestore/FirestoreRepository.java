package pt.isel.cn.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FirestoreRepository implements Repository<LandmarkPrediction, String> {

    private final Firestore firestore;

    public FirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<LandmarkPrediction> getAll(String collectionName){
        CollectionReference collectionRef = firestore.collection(collectionName);
        Iterable<DocumentReference> documentReferences = collectionRef.listDocuments();

        List<LandmarkPrediction> documents = new java.util.ArrayList<>(Collections.emptyList());

        documentReferences.forEach(documentReference -> {
            try {
                ApiFuture<DocumentSnapshot> future = documentReference.get();
                DocumentSnapshot documentSnapshot = future.get();
                LandmarkPrediction landmarkPrediction = documentSnapshot.toObject(LandmarkPrediction.class);
                documents.add(landmarkPrediction);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        if(documents.stream().anyMatch(Objects::isNull)){
            throw new RuntimeException("Error getting landmarks");
        }

        return documents;
    }

    /**
     * Gets a list of pairs of collection names and a list of predictions
     * @param scoreThreshold The score threshold
     * @return a list of pairs of collection names and a list of predictions
     */
    @Override
    public List<Pair<String, List<LandmarkPrediction>>> getByThresholdScore(double scoreThreshold)  {
        Iterable<CollectionReference> collections = firestore.listCollections();
        List<Pair<String, List<LandmarkPrediction>>> filteredLandmarks;

        Stream<CollectionReference> collectionReferenceList =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(collections.iterator(), Spliterator.ORDERED),false);


        filteredLandmarks = collectionReferenceList.map(collectionReference -> {
            Pair<String, List<LandmarkPrediction>> pair;
            try{
                pair = new Pair<String, List<LandmarkPrediction>>(
                        collectionReference.getId(),
                        filterDocumentsBy(scoreThreshold, collectionReference)
                        );
            }catch (ExecutionException | InterruptedException e) {
                return null;
            }
             return pair;
        }).collect(Collectors.toList());


        if(filteredLandmarks.stream().anyMatch(Objects::isNull)){
            throw new RuntimeException("Error getting landmarks");
        }

        return filteredLandmarks;
    }

    /**
     * Filters the documents by the score threshold
     * @param scoreThreshold The score threshold
     * @param collectionReference The collection reference
     * @return a list of predictions
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private List<LandmarkPrediction> filterDocumentsBy(double scoreThreshold, CollectionReference collectionReference) throws ExecutionException, InterruptedException {
        Query query = collectionReference.whereGreaterThanOrEqualTo("score", scoreThreshold);
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> queryDocuments = future.get().getDocuments();

        return queryDocuments.stream().map(queryDocument ->
                queryDocument.toObject(LandmarkPrediction.class)).collect(Collectors.toList());
    }
}
