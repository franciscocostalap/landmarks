package functionlookup;


import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;

public class Entrypoint implements HttpFunction {

    private String projectID = "cn2223-t1-g06";
    private String zone = "europe-west1-b";
    private String groupName = "landmarks-grcp-server-instance-group";

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        BufferedWriter writer = response.getWriter();
        try (InstancesClient client = InstancesClient.create()) {
            for (Instance curInst : client.list(this.projectID, this.zone).iterateAll()) {
                if (curInst.getName().contains(this.groupName)) {
                    String ip = curInst.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    writer.write(ip + ";");
                }
            }
        }
    }
}
