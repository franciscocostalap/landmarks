package pt.isel.cn;

import pt.isel.cn.vision.LandmarkPrediction;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class MapsAPIClient {
    private final static int ZOOM = 15; // Streets
    private final static String SIZE = "600x300";
    private final String apiKey;

    MapsAPIClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public void getStaticMapSaveImages(ArrayList<LandmarkPrediction> landmarks){
        landmarks.forEach(landmark -> {
            getStaticMapSaveImage(landmark.getLatitude(), landmark.getLongitude());
        });
    }

    public void getStaticMapSaveImage(String latitude, String longitude) {
        String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + latitude + "," + longitude
                + "&zoom=" + ZOOM
                + "&size=" + SIZE
                + "&key=" + apiKey;
        System.out.println(mapUrl);
        try {
            URL url = new URL(mapUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();
            BufferedInputStream bufIn = new BufferedInputStream(in);
            FileOutputStream out = new FileOutputStream("static_map_"+ UUID.randomUUID() +".png");
            byte[] buffer = new byte[8*1024];
            int bytesRead;
            while ((bytesRead = bufIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            bufIn.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
