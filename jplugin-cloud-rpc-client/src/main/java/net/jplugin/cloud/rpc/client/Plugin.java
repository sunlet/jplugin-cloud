package net.jplugin.cloud.rpc.client;

import net.jplugin.cloud.common.CloudPluginPriority;
import net.jplugin.cloud.rpc.client.annotation.BindRemoteService;
import net.jplugin.cloud.rpc.client.annotation.Protocol;
import net.jplugin.cloud.rpc.client.extension.EsfRemoteServiceAnnoHandler;
import net.jplugin.cloud.rpc.client.api.ExtensionESFHelper;
import net.jplugin.cloud.rpc.client.extension.RpcClientHandler;
import net.jplugin.cloud.rpc.client.extension.RpcJsonClientHandler;
import net.jplugin.cloud.rpc.client.imp.RpcClientManager;
import net.jplugin.core.kernel.api.*;
import net.jplugin.core.rclient.ExtendsionClientHelper;
import net.jplugin.core.rclient.api.Client;
import net.jplugin.core.service.api.RefService;

@PluginAnnotation

public class Plugin extends AbstractPlugin {
    public static final String EP_RPC_CLIENT_FILTER = "EP_RPC_CLIENT_FILTER";

    @RefService
    RpcClientManager clientManager;


    static {
        try {
            AutoBindExtensionManager.INSTANCE.addBindExtensionTransformer(BindRemoteService.class, (p, clazz, anno)->{
                BindRemoteService theAnno = (BindRemoteService) anno;
                if (theAnno.protocol()== Protocol.rpc){
                    ExtensionESFHelper.addRPCProxyExtension(p, clazz, theAnno.url());

                    PluginEnvirement.INSTANCE.getStartLogger().log("$$$ Auto add extension for remote service proxy : protocol="
                            + theAnno.protocol() + ",url=" + theAnno.url() + ",class=" + clazz.getName());
                }else
                    if (theAnno.protocol() == Protocol.rpc_json) {
                        ExtensionESFHelper.addRpcJsonProxyExtension(p, clazz, theAnno.url());

                        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ Auto add extension for remote service proxy : protocol="
                                + theAnno.protocol() + ",url=" + theAnno.url() + ",class=" + clazz.getName());

                }else{
                        throw new RuntimeException("not support :"+theAnno.protocol());
                    }



            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Plugin(){
        ExtendsionClientHelper.addClientHandlerExtension(this, Client.PROTOCOL_RPC, RpcClientHandler.class);
        ExtendsionClientHelper.addClientHandlerExtension(this, Client.PROTOCOL_RPC_JSON, RpcJsonClientHandler.class);
        ExtensionKernelHelper.addAnnoAttrHandlerExtension(this, EsfRemoteServiceAnnoHandler.class);
    }

    @Override
    public int getPrivority() {
        return CloudPluginPriority.CLIENT;
    }

    @Override
    public void init() {
        clientManager.start();
    }
}
