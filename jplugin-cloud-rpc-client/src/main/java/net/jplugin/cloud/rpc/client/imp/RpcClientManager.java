package net.jplugin.cloud.rpc.client.imp;

import net.jplugin.common.kits.ThreadFactoryBuilder;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;
import net.jplugin.core.service.api.BindService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@BindService
public class RpcClientManager {

    @RefLogger
    static Logger logger;

    Map<String,RpcServiceClient> serviceClientMap = new ConcurrentHashMap<>();

    private ScheduledExecutorService connectMaintainer = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ESFClientConnectMaintainer-%d").build());
    private ConnectionMaintainer maintainer = new ConnectionMaintainer();

    public void start() {
        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC ClientManager starting!");

        List<String> appcodeList = getSubscribeAppCodeList();

        appcodeList.forEach(code -> {
            RpcServiceClient client = new RpcServiceClient(code);
            serviceClientMap.put(code,client);
            client.start();
        });

        //连接维护
        connectMaintainer.scheduleWithFixedDelay(maintainer,5000,5000, TimeUnit.MILLISECONDS);

        //等待连接完成
        waitTillConnectedOrTimeout();

        PluginEnvirement.INSTANCE.getStartLogger().log("$$$ RPC ClientManager started!");
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

    private List<String> getSubscribeAppCodeList() {
        List<String> list = new ArrayList<>();
        list.add("app1");
        return list;
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
