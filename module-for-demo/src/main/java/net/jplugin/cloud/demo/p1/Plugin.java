package net.jplugin.cloud.demo.p1;

import net.jplugin.cloud.rpc.client.annotation.RefRemoteServiceProxy;
import net.jplugin.cloud.rpc.client.api.RpcContext;
import net.jplugin.cloud.rpc.client.api.RpcContextManager;
import net.jplugin.cloud.rpc.client.imp.RpcClientManager;
import net.jplugin.cloud.rpc.client.imp.RpcServiceClient;
import net.jplugin.common.kits.ReflactKit;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.rclient.api.Client;
import net.jplugin.core.rclient.api.ClientFactory;
import net.jplugin.core.rclient.proxyfac.ClientProxyFactory;
import net.jplugin.core.service.api.RefService;
import net.jplugin.core.service.api.ServiceFactory;

import java.util.List;

@PluginAnnotation
public class Plugin extends AbstractPlugin {

    @RefRemoteServiceProxy
    IService1 s1Field;
//
    @RefService
    RpcContextManager ctxManager;
//
    @RefService
    RpcClientManager clientManager;
    @Override
    public void init() {
//        System.out.println("sleeping seconds................");
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        System.out.println(" now to call ............");
        System.out.println(" now to call ............");
        System.out.println(" now to call ............");
        System.out.println(" haha  ............");

        RpcContext ctx = ctxManager.getServiceContext("app1:servicenode1");
        Object result = ctx.invoke("/svc1", "greet", new String[]{"dududu"});
        System.out.println("通过context调用结果："+result);

        //通过Context调用
        List<RpcContext> ctxList = ctxManager.getNodeContextList("app1:servicecode1");
        result = ctxList.get(0).invoke("/svc1", "greet", new String[]{"mememe"});
        System.out.println("通过context调用结果："+result);

        //通过proxy调用
        IService1 proxy1 = ClientProxyFactory.instance.getClientProxy(IService1.class);
        String ret = proxy1.greet("haha");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$" +ret);

//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        ret = s1Field.greet("bilibili");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$" +ret);
    }

    @Override
    public int getPrivority() {
        return 0;
    }
}
