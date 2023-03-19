package net.jplugin.cloud.rpc.client.api;

import net.jplugin.cloud.rpc.client.imp.RpcServiceClient;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RpcServiceContext {
    RpcServiceClient serviceClient;

    RpcServiceContext(RpcServiceClient sclient){
        this.serviceClient = sclient;
    }

    public Object invoke(String serviceName, String methodName, Object[] args){
        Type[] types = Util.getTypes(args);
        return serviceClient.invokeRpc(serviceName,methodName,types,args, AbstractMessageBodySerializer.SerializerType.KRYO);
    }

    public Object invoke4Json(String serviceName, String methodName, Object[] args){
        Type[] types = Util.getTypes(args);
        return serviceClient.invokeRpc(serviceName,methodName,types,args, AbstractMessageBodySerializer.SerializerType.JSON);
    }




    public List<InvokeResult> invokeAllNodes(String serviceName, String methodName, Object[] args,boolean exceptSelf){
        return invokeAllNodesInner(serviceName, methodName, args, false, exceptSelf);
    }

    public List<InvokeResult> invoke4JsonAllNodes(String serviceName, String methodName, Object[] args,boolean exceptSelf){
        return invokeAllNodesInner(serviceName, methodName, args, true, exceptSelf);
    }


    private List<InvokeResult> invokeAllNodesInner(String serviceName, String methodName, Object[] args,boolean forJson,boolean exceptSelf){
        if (exceptSelf) throw new RuntimeException("not support yet");
        List<RpcNodeContext> nodelist = serviceClient._getRpcContextList();

        InvocationParam param = ClientInvocationManager.INSTANCE.getAndClearParam();

        List<InvokeResult> result = new ArrayList<>();
        for (RpcNodeContext node:nodelist){
            InvokeResult invokeResult = getInvokeResult(serviceName, methodName, args, forJson, node,param);
            result.add(invokeResult);
        }
        return result;
    }

    private InvokeResult getInvokeResult(String serviceName, String methodName, Object[] args, boolean forJson, RpcNodeContext node, InvocationParam param) {
        Object ret = null;
        Throwable th = null;
        try{
            ClientInvocationManager.INSTANCE.setParam(param);
            if (forJson) {
                ret = node.invoke(serviceName, methodName, args);
            }else{
                ret = node.invoke4Json(serviceName, methodName, args);
            }
        }catch(Throwable t){
            th = t;
        }
        return new InvokeResult(ret, th, th == null);
    }





}
