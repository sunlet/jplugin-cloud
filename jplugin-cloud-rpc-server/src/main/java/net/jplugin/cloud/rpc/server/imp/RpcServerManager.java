package net.jplugin.cloud.rpc.server.imp;

import net.jplugin.cloud.rpc.io.server.NettyServer;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.config.api.RefConfig;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.service.api.BindService;

@BindService
public class RpcServerManager {
    NettyServer nettyServer ;

    Integer port;
    @RefConfig(path = "cloud-rpc.server-boss",defaultValue = "1")
    Integer boss;
    @RefConfig(path = "cloud-rpc.server-workers",defaultValue = "5")
    Integer workers;



    public void start() {
        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC Server starting...");
        this.port = Integer.parseInt(CloudEnvironment.INSTANCE.getRpcPort());
        nettyServer = new NettyServer(port,boss,workers);
        nettyServer.boostrap();
        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC Server starting success");
    }
}
