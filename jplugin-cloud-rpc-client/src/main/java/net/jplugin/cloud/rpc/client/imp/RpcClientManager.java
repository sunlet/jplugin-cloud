package net.jplugin.cloud.rpc.client.imp;

import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.service.api.BindService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@BindService
public class RpcClientManager {

    Map<String,RpcServiceClient> serviceClientMap = new ConcurrentHashMap<>();

    public void start() {
        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC ClientManager starting!");

        RpcServiceClient client1 = new RpcServiceClient();
        serviceClientMap.put("app1", client1);
        client1.start();

        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC ClientManager started!");
    }


    public RpcServiceClient getServiceClient(String appcode){
        return serviceClientMap.get(appcode);
    }
}
