import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import landmark_service.LandmarkDetectionServiceGrpc;
import landmark_service.Void;

import java.util.logging.Logger;


public class ChannelManager {
    private static final Logger logger = Logger.getLogger(ChannelManager.class.getName());

    private String[] possibleServers;

    private final String IPLookupURL;
    private final int svcPort;

    public ChannelManager(String IPLookupURL, int svcPort){
        this.IPLookupURL = IPLookupURL;
        this.svcPort = svcPort;
        getPossibleServers();
    }

    private static LandmarkDetectionServiceGrpc.LandmarkDetectionServiceBlockingStub blockingStub;


    /**
     * Tries to get a channel to a server.
     * This channel is guaranteed to be ready.
     * @return The channel to a server.
     */
    public ManagedChannel getChannel () {
        while (true) {
            getPossibleServers();
            ManagedChannel channel = tryToGetChannel();
            if (channel != null) {
                return channel;
            }
            logger.info("No server available. Retrying in 5 seconds...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Tries to get a channel to a server.
     * If a server is reachable, it will block until the connection is ready.
     * @return A channel to a server, or null if no server is available.
     */
    private ManagedChannel tryToGetChannel(){
        for(String server : possibleServers){
            try{
                logger.info("Trying to connect to server " + server + "...");
                ManagedChannel channel = ManagedChannelBuilder.forAddress(server, svcPort).usePlaintext().build();

                blockingStub = LandmarkDetectionServiceGrpc.newBlockingStub(channel);

                blockingStub.isAlive(Void.newBuilder().build());
                logger.info("Connection ready...");
                return channel;
            } catch (Exception e){
                logger.info("Server " + server + " is not available.");
            }
        }
        return null;
    }

    /**
     * Gets the possible servers from the IPLookup.
     */
    private void getPossibleServers(){
        possibleServers = IPLooker.lookupPossibleGRCPServers(IPLookupURL);
        logger.info("Found " + possibleServers.length + " possible servers.");

        while (possibleServers.length == 0){
            logger.info("No servers found. Retrying in 5 seconds...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            possibleServers = IPLooker.lookupPossibleGRCPServers(IPLookupURL);
        }
    }
}

