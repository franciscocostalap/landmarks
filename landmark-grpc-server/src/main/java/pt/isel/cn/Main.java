package pt.isel.cn;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.Server;
import io.grpc.ServerBuilder;


import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        Storage storage = StorageOptions.getDefaultInstance().getService();
        // por porto na env
        Server server = ServerBuilder.forPort(8080)
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
        }
    }
}
