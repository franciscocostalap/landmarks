package pt.isel.cn;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class MapsAPIClient {
    private static final Logger logger = Logger.getLogger(MapsAPIClient.class.getName());
    private final static int ZOOM = 15; // Streets
    private final static String SIZE = "600x300";
    private final String apiKey;

    MapsAPIClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Fetches a static map from Google Maps API and stores it in a Cloud Storage bucket
     * @param latitude landmark prediction latitude
     * @param longitude landmark prediction longitude
     * @param streamObjectUpload object that handles the storage of the map in a Cloud Storage bucket
     */
    public void fetchStaticMapToCloudStorage(
        String latitude,
        String longitude,
        StreamObjectUpload streamObjectUpload
    ) {
        String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + latitude + "," + longitude
                + "&zoom=" + ZOOM
                + "&size=" + SIZE
                + "&key=" + apiKey;
        try {
            URL url = new URL(mapUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            System.out.println("Request to: " + mapUrl);
            InputStream in = conn.getInputStream();
            BufferedInputStream bufIn = new BufferedInputStream(in);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = bufIn.read(buffer)) != -1) {
                logger.info("Received chunk of size: " + buffer.length);
                streamObjectUpload.storeObject(buffer, bytesRead);
                buffer = new byte[8 * 1024];
            }
            bufIn.close();
            in.close();
            streamObjectUpload.closeWriteChannel();
            System.out.println("Stored static map to Cloud Storage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
