package net.jplugin.cloud.rpc.client.api;

import net.jplugin.cloud.rpc.client.imp.RpcServiceClient;
import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;

import java.lang.reflect.Type;

public class RpcNodeContext {
    private final RpcServiceClient client;
    private final String remoteAddr;

    public RpcNodeContext(RpcServiceClient aClient, String aRemoteAddr){
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
     * @param serviceName
     * @param methodName
     * @param args
     * @return
     * @throws Exception
     */
    private Object invokeInner(String serviceName, String methodName, Type[] argsType, Object[] args , AbstractMessageBodySerializer.SerializerType st) {
        InvocationParam param = ClientInvocationManager.INSTANCE.getParam();
        if (param==null){
            param = InvocationParam.create();
            ClientInvocationManager.INSTANCE.setParam(param);
        }
        param.serviceAddress(remoteAddr);

        return client.invokeRpc(serviceName, methodName, argsType,args, st);
    }


    public Object invoke(String serviceName, String methodName, Object[] args){
        Type[] types = Util.getTypes(args);
        return invokeInner(serviceName,methodName,types,args, AbstractMessageBodySerializer.SerializerType.KRYO);
    }

    public Object invoke4Json(String serviceName, String methodName, Object[] args) {
        Type[] types = Util.getTypes(args);
        return invokeInner(serviceName,methodName,types,args, AbstractMessageBodySerializer.SerializerType.JSON);
    }
}
