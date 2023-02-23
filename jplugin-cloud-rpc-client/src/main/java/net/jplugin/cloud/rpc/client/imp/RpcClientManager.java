package net.jplugin.cloud.rpc.client.imp;

import net.jplugin.cloud.rpc.client.kits.RpcUrlKit;
import net.jplugin.cloud.rpc.client.spi.IClientSubscribeService;
import net.jplugin.common.kits.ThreadFactoryBuilder;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.kernel.api.RefExtension;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;
import net.jplugin.core.rclient.proxyfac.ClientProxyDefinition;
import net.jplugin.core.service.api.BindService;

import java.util.*;
import java.util.concurrent.*;

@BindService
public class RpcClientManager {

    @RefLogger
    static Logger logger;

    Map<String,RpcServiceClient> serviceClientMap = new ConcurrentHashMap<>();

    @RefExtension
    IClientSubscribeService clientSubscribeService;

    private ScheduledExecutorService connectMaintainer = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ESFClientConnectMaintainer-%d").build());
    private ConnectionMaintainer maintainer = new ConnectionMaintainer();

    public void start() {
        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC ClientManager starting!");

        //获取订阅的appcode列表
        Set<String> appcodeList = getSubscribeAppCodeList();

        if (!appcodeList.isEmpty()) {
            //初始化订阅服务
            clientSubscribeService.initSubscribCodeList(Collections.unmodifiableSet(appcodeList));

            //逐个启动
            appcodeList.forEach(o->{
                RpcServiceClient serviceClient = new RpcServiceClient(o);
                Set<String> hostAddrs = clientSubscribeService.getServiceNodesList(o);
                serviceClient.start(hostAddrs);
                serviceClientMap.put(o,serviceClient);
            });

            //连接维护
            connectMaintainer.scheduleWithFixedDelay(maintainer, 5000, 5000, TimeUnit.MILLISECONDS);

            //等待连接完成
            waitTillConnectedOrTimeout();

            PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC ClientManager started!" + appcodeList.size()+" apps subscrib.");
        }else{
            PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC Client not start ,because no Subscribs !");
        }
    }

    class ConnectionMaintainer implements  Runnable{
        @Override
        public void run() {
            try{
                if (logger.isDebugEnabled()){
                    logger.debug("now to maintain connection.");
                }

                serviceClientMap.forEach((code,client)->{
                    client.maintainConnect();
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private Set<String> getSubscribeAppCodeList() {

        Set<String> appCodeList = new HashSet<>();

        // ESF协议格式:esf://appcode/Servicename
        Map<String, ClientProxyDefinition> javaExtension = PluginEnvirement.getInstance()
                .getExtensionMap(net.jplugin.core.rclient.Plugin.EP_CLIENT_PROXY, ClientProxyDefinition.class);

        if ((javaExtension != null && !javaExtension.isEmpty()) ) {
            javaExtension.values().forEach(o->{
                String url = o.getUrl();
                Tuple2<String, String> urlInfo = RpcUrlKit.parseEsfUrlInfo(url);
                appCodeList.add(urlInfo.first);
            });
        }
        return appCodeList;
    }

    private void waitTillConnectedOrTimeout() {
        System.out.println("ESF client starting ");
        //最长等15秒
        for (int i=0;i<30;i++) {
            try {
                Thread.sleep(500);
//                System.out.print(". ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int successNum = 0;
            int failedNum = 0;
            Set<String> codes = serviceClientMap.keySet();
            for (String code : codes) {
                RpcServiceClient client = serviceClientMap.get(code);
                if (client.connectedAny()) {
                    successNum++;
                } else {
                    failedNum++;
                }
            }

            if (failedNum ==0 ) {
                PluginEnvirement.INSTANCE.getStartLogger().log("\nESF Client start success. subscrib status:"+getClientStatus());
                return;
            }
        }

        throw new RuntimeException("ESF client start failed! "+getClientStatus());
    }

    private String getClientStatus() {
        StringBuffer sb = new StringBuffer();

        this.serviceClientMap.forEach((code,client) ->{
            sb.append("\n\t").append(client.toString());
        });
        sb.append("\n");

        return sb.toString();
    }


    public RpcServiceClient getServiceClient(String appcode){
        return serviceClientMap.get(appcode);
    }
}
