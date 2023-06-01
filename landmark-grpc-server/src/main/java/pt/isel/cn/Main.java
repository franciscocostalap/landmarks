package pt.isel.cn;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.isel.cn.utils.UUIDRandomNameGenerator;


import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

import static pt.isel.cn.Constants.LANDMARK_BUCKET;
import static pt.isel.cn.Constants.STATIC_IMAGE_BUCKET;

public class Main {

    private static int svcPort = 7500;
    private static Server server;

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port > 0 && port < 65535) {
                    System.out.println("Port set to " + port);
                    svcPort = port;
                } else {
                    System.out.println("Port must be between 0 and 65535");
                }
            } catch (NumberFormatException e) {
                System.out.println("Port must be a number");
            }
        }
        Logger logger = Logger.getLogger(Main.class.getName());
        try {
            System.out.println("Starting server on port " + svcPort);


            Storage storage = StorageOptions.getDefaultInstance().getService();
            String projectId = storage.getOptions().getProjectId();
            if (projectId == null)
                throw new RuntimeException("Please set the environment variable " +
                        "GOOGLE_APPLICATION_CREDENTIALS to the path of your service account key file.");
            logger.info("Project ID: " + projectId);

            GoogleCredentials credentials =
                    GoogleCredentials.getApplicationDefault();
            FirestoreOptions options = FirestoreOptions
                    .newBuilder().setCredentials(credentials).build();
            Firestore db = options.getService();

            // por porto na env
            server = ServerBuilder.forPort(svcPort)
                    .addService(
                        new LandmarkDetectionServiceImpl(
                            storage,
                            new  UUIDRandomNameGenerator(),
                            LANDMARK_BUCKET,
                            new CloudPubSubPublisher(),
                            db
                    )).build();

            logger.info("Starting server...");
            server.start();
            logger.info("Server started!");
           server.awaitTermination();

            logger.info("Server terminated!");
        } catch (Throwable e) {
            logger.warning("Server interrupted!");
            logger.warning(e.getMessage());
            e.printStackTrace();
        } finally {
            server.shutdown();
        }
    }
}
