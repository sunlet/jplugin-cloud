package net.jplugin.cloud.rpc.server;

import net.jplugin.cloud.common.CloudPluginPriority;
import net.jplugin.cloud.rpc.server.imp.RpcServerManager;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.service.api.BindService;
import net.jplugin.core.service.api.RefService;


@PluginAnnotation
public class Plugin extends AbstractPlugin {

    @RefService
    RpcServerManager server;

    @Override
    public int getPrivority() {
        return CloudPluginPriority.SERVER;
    }

    @Override
    public void init() {
        server.start();
    }
}
