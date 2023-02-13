package net.jplugin.cloud.rpc.server;

import net.jplugin.cloud.rpc.server.imp.RpcServer;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.service.api.BindService;
import net.jplugin.core.service.api.RefService;


@PluginAnnotation
public class Plugin extends AbstractPlugin {

    @RefService
    RpcServer server;

    @Override
    public int getPrivority() {
        return 0;
    }

    @Override
    public void init() {
        server.start();
    }
}
