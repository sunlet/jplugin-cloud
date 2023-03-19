package net.jplugin.cloud.rpc.client.api;

import net.jplugin.cloud.rpc.client.imp.RpcServiceClient;
import net.jplugin.cloud.rpc.io.api.InvocationContext;
import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;

import java.lang.reflect.Type;

public class NodeContext {
    private final RpcServiceClient client;
    private final String remoteAddr;

    public NodeContext(RpcServiceClient aClient, String aRemoteAddr){
        this.client = aClient;
        this.remoteAddr = aRemoteAddr;
    }

    public String getRemoteHostIp(){
        return this.remoteAddr.substring(0,remoteAddr.indexOf(':'));
    }

    public String getRemoteHostPort(){
        return this.remoteAddr.substring(remoteAddr.indexOf(':')+1);
    }

    /**
     * 调用指定的服务
     * @return
     * @throws Exception
     */
    private Object invokeInner(InvocationContext ctx) {
        InvocationParam param = ctx.getOrInitParam();
        param.serviceAddress(remoteAddr);
        return client.invokeRpc(ctx);
    }


    public Object invoke(String serviceName, String methodName, Object[] args){
        InvocationContext ctx = InvocationContext.create(serviceName,methodName,args, AbstractMessageBodySerializer.SerializerType.KRYO);
        return invokeInner(ctx);
    }

    public Object invoke4Json(String serviceName, String methodName, Object[] args) {
        InvocationContext ctx = InvocationContext.create(serviceName,methodName,args, AbstractMessageBodySerializer.SerializerType.JSON);
        return invokeInner(ctx);
    }
}
