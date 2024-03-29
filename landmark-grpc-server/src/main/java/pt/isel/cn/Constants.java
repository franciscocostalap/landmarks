package pt.isel.cn;

public class Constants {
    public static final int svcPort = 8080;
    public static final String LANDMARK_BUCKET = "landmark-bucket-tf";
    public static final String STATIC_IMAGE_BUCKET = "landmark-static-maps-bucket-tf";
    public static final String PROJECT_ID = "CN2223-T1-G06";
    public static final String TOPIC_NAME = "landmarks-app-requests-id";
    public static final String MESSAGE_ATTRIBUTE_KEY = "request-id";
    public static final String FIRESTORE_COLLECTION = "ImageLandmarks";
    public static final String NO_LANDMARKS_RESULT = "no-landmarks";
    public static final int CONTENT_TYPE_SIZE = 5;
}
