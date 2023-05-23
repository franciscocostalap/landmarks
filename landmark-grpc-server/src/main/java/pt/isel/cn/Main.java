package pt.isel.cn;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.Server;
import io.grpc.ServerBuilder;


import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {

    private static final int svcPort = 8080;

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        Storage storage = StorageOptions.getDefaultInstance().getService();
        String projectId = storage.getOptions().getProjectId();
        if (projectId == null)
            throw new RuntimeException("Please set the environment variable " +
                    "GOOGLE_APPLICATION_CREDENTIALS to the path of your service account key file.");
        logger.info("Project ID: " + projectId);

        // por porto na env
        Server server = ServerBuilder.forPort(15004)
                .addService(new LandmarkDetectionServiceImpl(
                        storage,
                        new  pt.isel.cn.utils.UUIDRandomNameGenerator(),
                        "landmark-bucket-tf")
                )
                .build();
        try {
            logger.info("Starting server...");
            server.start();
            logger.info("Server started!");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();

            logger.info("Server terminated!");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            server.shutdown();
        }
    }
}
