package net.jplugin.cloud.rpc.client.api;

import net.jplugin.cloud.rpc.client.imp.RpcClientManager;
import net.jplugin.cloud.rpc.client.imp.RpcServiceClient;
import net.jplugin.core.service.api.BindService;
import net.jplugin.core.service.api.RefService;

import java.util.List;

@BindService
public class RpcContextManager {

    @RefService
    RpcClientManager clientManager;

    public ServiceContext getServiceContext(String appcode){
        RpcServiceClient serviceClient = clientManager.getServiceClient(appcode);
        if (serviceClient==null){
            throw new RuntimeException("appcode "+appcode +" is not subscribed");
        }
        return new ServiceContext(serviceClient);
    }

    public  List<NodeContext> getNodeContextList(String appcode){
        RpcServiceClient serviceClient = clientManager.getServiceClient(appcode);
        if (serviceClient==null){
            throw new RuntimeException("appcode "+appcode +" is not subscribed");
        }
        return serviceClient._getRpcContextList();
    }

    public NodeContext getNodeContext(String appcode, String ip, int port){
        RpcServiceClient serviceClient = clientManager.getServiceClient(appcode);
        if (serviceClient==null){
            throw new RuntimeException("appcode "+appcode +" is not subscribed");
        }
        return serviceClient._getRpcContext(ip, port);
    }

    public NodeContext getNodeContext(String appcode, String ip){
        RpcServiceClient serviceClient = clientManager.getServiceClient(appcode);
        if (serviceClient==null){
            throw new RuntimeException("appcode "+appcode +" is not subscribed");
        }
        return serviceClient._getRpcContext(ip);
    }


}
