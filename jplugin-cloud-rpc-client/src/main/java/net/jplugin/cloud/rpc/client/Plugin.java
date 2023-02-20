package net.jplugin.cloud.rpc.client;

import net.jplugin.cloud.rpc.client.imp.RpcClientManager;
import net.jplugin.cloud.rpc.client.imp.RpcServiceClient;
import net.jplugin.common.kits.ReflactKit;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.service.api.BindService;
import net.jplugin.core.service.api.RefService;

@PluginAnnotation

public class Plugin extends AbstractPlugin {
    public static final String EP_RPC_CLIENT_FILTER = "EP_RPC_CLIENT_FILTER";

    @RefService
    RpcClientManager clientManager;

    public Plugin(){

    }

    @Override
    public int getPrivority() {
        return 1;
    }

    @Override
    public void init() {
        clientManager.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RpcServiceClient svcClient = clientManager.getServiceClient("app1");
        try {
            Object ret = svcClient.invoke4Json("/svc1", ReflactKit.findSingeMethodExactly(Service1.class, "greet"), new String[]{"meme"});
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
