package pt.isel.cn.message;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import pt.isel.cn.MapsAPIClient;
import pt.isel.cn.StreamObjectUpload;
import pt.isel.cn.firestore.FirestoreDocument;
import pt.isel.cn.firestore.FirestoreRepository;
import pt.isel.cn.vision.LandmarkPrediction;
import pt.isel.cn.vision.VisionAPIClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static pt.isel.cn.Constants.STATIC_MAPS_BUCKET_NAME;

public class MessageService {

    private final FirestoreRepository firestoreRepository;
    private final VisionAPIClient visionAPIClient;
    private final MapsAPIClient mapsAPIClient;
    private final Storage storage;

    public MessageService(
            VisionAPIClient visionAPIClient,
            MapsAPIClient mapsAPIClient,
            FirestoreRepository firestoreRepository,
            Storage storage
    ){
        this.visionAPIClient = visionAPIClient;
        this.mapsAPIClient = mapsAPIClient;
        this.firestoreRepository = firestoreRepository;
        this.storage = storage;
    }


    /**
     * Fetches the landmarks predictions from the Vision API and stores them in Firestore.
     * @param blobName name of the document to be stored in Firestore
     * @param gcsUrl url of the image to be fetched from GCS
     * @return an array list of landmark predictions
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public ArrayList<LandmarkPrediction> fetchLandmarksToFirestore(
            String blobName,
            String gcsUrl
    ) throws ExecutionException, InterruptedException, IOException, NoLandMarkFoundException {
        ArrayList<LandmarkPrediction> landmarkPredictions = visionAPIClient.detectLandmarksGcs(gcsUrl);
        if(landmarkPredictions.isEmpty()){
            firestoreRepository.save(null, blobName);
            System.out.println("No landmarks detected");
            throw new NoLandMarkFoundException("No landmarks detected");
        }
        System.out.println("Storing predictions for " + blobName);
        landmarkPredictions.forEach( landmarkPrediction -> {
            try {
                firestoreRepository.save(landmarkPrediction, blobName);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        return landmarkPredictions;
    }

    /**
     * Fetches the static maps from the Maps API and stores them in GCS.
     * @param blobName name of the blob to be stored in GCS
     * @param landmarksPredictions array list of landmark predictions to fetch the static maps
     */
    public void fetchStaticMapsToCloudStorage(
            String blobName,
            ArrayList<LandmarkPrediction> landmarksPredictions
    ){
        int idx = -1;
        for(LandmarkPrediction landmarkPrediction : landmarksPredictions){
            idx++;
            mapsAPIClient.fetchStaticMapToCloudStorage(
                    landmarkPrediction.getLatitude(),
                    landmarkPrediction.getLongitude(),
                    getStreamObjectUpload(
                            blobName,
                            idx
                    )
            );
        }
    }

    /**
     * Creates a StreamObjectUpload object to be used in the Maps API to upload the static maps to GCS.
     * @param blobName name of the blob to be stored in GCS
     * @param idx index of the landmark prediction in the array list
     * @return a [StreamObjectUpload] object
     */
    private StreamObjectUpload getStreamObjectUpload(String blobName, int idx){
        BlobInfo.Builder blobInfoBuilder =
                BlobInfo.newBuilder(STATIC_MAPS_BUCKET_NAME, blobName + "-" + idx);
        return new StreamObjectUpload(storage, blobInfoBuilder);
    }
}
