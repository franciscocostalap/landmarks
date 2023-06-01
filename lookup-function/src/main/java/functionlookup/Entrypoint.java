package functionlookup;


import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Entrypoint implements HttpFunction {

    private static final String projectID = "cn2223-t1-g06";
    private static final String zone = "europe-southwest1-a";
    private static final String groupName = "landmark-grpc-server-group";

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        BufferedWriter writer = response.getWriter();
        ArrayList<String> instance = new ArrayList<>(Collections.emptyList());
        try (InstancesClient client = InstancesClient.create()) {
            for (Instance curInst : client.list(projectID, zone).iterateAll()) {
                if (curInst.getName().contains(groupName)) {
                    String ip = curInst.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    instance.add(ip);
                }
            }
        }
        if(instance.size() == 0) {
            return;
        }
        Collections.shuffle(instance);
        instance.forEach( ip -> {
            try {
                writer.write(ip + ";");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
