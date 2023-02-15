package net.jplugin.cloud.rpc.client;

import net.jplugin.cloud.rpc.client.imp.RpcClientManager;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.service.api.BindService;
import net.jplugin.core.service.api.RefService;

@PluginAnnotation

public class Plugin extends AbstractPlugin {

    @RefService
    RpcClientManager clientManager;

    public Plugin(){

    }

    @Override
    public int getPrivority() {
        return 0;
    }

    @Override
    public void init() {
        clientManager.start();
    }
}
