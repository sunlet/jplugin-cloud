package net.jplugin.cloud.rpc.server.imp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.org.apache.xml.internal.security.Init;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.jplugin.cloud.common.CloudEnvironment;
import net.jplugin.cloud.rpc.io.bootstrap.impl.rpc.NettyServer;
import net.jplugin.core.config.api.RefConfig;
import net.jplugin.core.kernel.api.Initializable;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.log.api.LogFactory;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;
import net.jplugin.core.service.api.BindService;

import java.util.concurrent.TimeUnit;

@BindService
public class RpcServerManager {
    NettyServer nettyServer ;

    Integer port;
    @RefConfig(path = "rpc.server-boss",defaultValue = "1")
    Integer boss;
    @RefConfig(path = "rpc.server-boss",defaultValue = "5")
    Integer workers;



    public void start() {
        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC Server starting...");
        this.port = Integer.parseInt(CloudEnvironment.INSTANCE.getRpcPort());
        nettyServer = new NettyServer(port,boss,workers);
        nettyServer.boostrap();
        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC Server starting success");
    }
}
