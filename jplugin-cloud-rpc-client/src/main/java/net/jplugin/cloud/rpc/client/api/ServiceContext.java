package net.jplugin.cloud.rpc.client.api;

import net.jplugin.cloud.rpc.client.imp.RpcServiceClient;
import net.jplugin.cloud.rpc.io.api.InvocationContext;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ServiceContext {
    RpcServiceClient serviceClient;

    ServiceContext(RpcServiceClient sclient){
        this.serviceClient = sclient;
    }

    public Object invoke(String serviceName, String methodName, Object[] args){
        return serviceClient.invokeRpc(InvocationContext.create(serviceName,methodName,args, AbstractMessageBodySerializer.SerializerType.KRYO));
    }

    public Object invoke4Json(String serviceName, String methodName, Object[] args){
        return serviceClient.invokeRpc(InvocationContext.create(serviceName,methodName,args, AbstractMessageBodySerializer.SerializerType.JSON));
    }


    public List<InvokeResult> invokeAllNodes(String serviceName, String methodName, Object[] args,boolean exceptSelf){
        return invokeAllNodesInner(InvocationContext.create(serviceName,methodName,args, AbstractMessageBodySerializer.SerializerType.JSON),exceptSelf);
    }

    public List<InvokeResult> invokeAllNodes4Json(String serviceName, String methodName, Object[] args,boolean exceptSelf){
        return invokeAllNodesInner(InvocationContext.create(serviceName,methodName,args, AbstractMessageBodySerializer.SerializerType.KRYO), exceptSelf);
    }


    private List<InvokeResult> invokeAllNodesInner(InvocationContext ctx,boolean exceptSelf){
        if (exceptSelf) throw new RuntimeException("not support yet");
        List<String> nodelist = serviceClient.getAddressList();

        List<InvokeResult> result = new ArrayList<>();
        for (String address:nodelist){
            InvokeResult invokeResult = getInvokeResult(ctx, address);
            result.add(invokeResult);
        }
        return result;
    }

    private InvokeResult getInvokeResult(InvocationContext ctx, String address) {
        Object ret = null;
        Throwable th = null;
        try{
            ret = serviceClient.invokeRpc(ctx);
        }catch(Throwable t){
            th = t;
        }
        return new InvokeResult(ret, th, th == null);
    }





}
