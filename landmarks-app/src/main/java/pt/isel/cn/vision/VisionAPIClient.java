package pt.isel.cn.vision;

import com.google.cloud.vision.v1.*;
import com.google.type.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisionAPIClient {

    /**
     * Detects landmarks in the specified remote image on Google Cloud Storage.
     *
     * @param blobGsPath The path to the image in the Google Cloud Storage
     * @return A list of landmarks detected in the image by the Google Vision API
     * @throws IOException
     */
    public ArrayList<LandmarkPrediction> detectLandmarksGcs(String blobGsPath) throws IOException {
        System.out.println("Detecting landmarks for: " + blobGsPath);
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(blobGsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            ArrayList<LandmarkPrediction> landmarkPredictions = new ArrayList<>(Collections.emptyList());
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return new ArrayList<>(Collections.emptyList());
                }

                System.out.println("Landmarks list size: " + res.getLandmarkAnnotationsList().size());

                for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
                    addLandmarkPredictionTo(landmarkPredictions, annotation);
                }

            }
            return landmarkPredictions;
        }
    }

    /**
     * Builds a LandmarkPrediction from an EntityAnnotation and adds it to the landmarkPredictions list
     * @param landmarkPredictions The list of LandmarkPredictions to add the new prediction to
     * @param annotation The EntityAnnotation to build the LandmarkPrediction from
     */
    private void addLandmarkPredictionTo(
            ArrayList<LandmarkPrediction> landmarkPredictions,
            EntityAnnotation annotation
    ){
        LocationInfo info = annotation.getLocationsList().listIterator().next();
        LatLng coordinates = info.getLatLng();
        LandmarkPrediction landmarkPrediction = new LandmarkPrediction(
                annotation.getDescription(),
                annotation.getScore(),
                String.valueOf(coordinates.getLatitude()),
                String.valueOf(coordinates.getLongitude())
        );
        landmarkPredictions.add(landmarkPrediction);
    }
}


