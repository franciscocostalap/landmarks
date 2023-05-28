import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IPLooker {

    public static String[] lookupPossibleGRCPServers(String lookupURL)  {
        StringBuilder response = new StringBuilder();

        try {
            URL request_uri = new URL(lookupURL);
            HttpURLConnection connection = (HttpURLConnection) request_uri.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();
        } catch (IOException e2){
            e2.printStackTrace();
        }
        if (response.toString().equals("")){
            return new String[0];
        }

        return parseResponse(response.toString());

    }


    private static String[] parseResponse(String response){
        return response.split(";");
    }
}
