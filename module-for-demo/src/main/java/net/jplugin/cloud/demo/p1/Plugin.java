package net.jplugin.cloud.demo.p1;

import net.jplugin.cloud.rpc.client.annotation.RefRemoteService;
import net.jplugin.cloud.rpc.client.api.NodeContext;
import net.jplugin.cloud.rpc.client.api.RpcContextManager;
import net.jplugin.cloud.rpc.client.api.ServiceContext;
import net.jplugin.cloud.rpc.client.imp.RpcClientManager;
import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.config.api.GlobalConfigFactory;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.rclient.proxyfac.ClientProxyFactory;
import net.jplugin.core.service.api.RefService;

import java.util.List;

@PluginAnnotation
public class Plugin extends AbstractPlugin {

    @RefRemoteService
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

        //测试配置获取
        System.out.println(ConfigFactory.getStringConfig("database.url"));

        //测试全局配置
        String config = GlobalConfigFactory.getValueInDefaultGroup("DEFAULT_GROUP.aaa");
        System.out.println(config);

        try {
            Thread.sleep(2000);
        }catch(Exception e){}

        //测试服务调用
        System.out.println(" haha  ............");

        ServiceContext ctx = ctxManager.getServiceContext("app1:servicecode1");
        Object result = ctx.invoke("/svc1", "greet", new String[]{"dududu"});
        System.out.println("通过context调用结果："+result);

        //通过Context调用
        List<NodeContext> ctxList = ctxManager.getNodeContextList("app1:servicecode1");
        result = ctxList.get(0).invoke("/svc1", "greet", new Object[]{"mememe"});
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
