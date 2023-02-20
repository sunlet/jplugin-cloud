package net.jplugin.cloud.rpc.client.imp;

import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.kernel.api.Initializable;
import net.jplugin.core.kernel.api.RefAnnotationSupport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RpcServiceClient  {
    String targetAppCode;
    private static NettyClient[] EMPTY_ARR = new NettyClient[0];

    //保存所有
    NettyClient[] nettyClients =  EMPTY_ARR;

    //保存所有active的channel


    public Object invoke(String serviceName, Method method, Object[] args) throws Exception {


//		String serverIp = null;
//		String serverPort = null;
//		boolean async = false;
//		ICallback callback = null;
//		InvocationParam invocationParam = MethodUtil.getAndClearParam();
//		if (invocationParam != null) {
//			String serviceAddress = invocationParam.getServiceAddress();// IP:PORT格式
//			async = (invocationParam.getRpcAsync() == null ? false : invocationParam.getRpcAsync());
//			callback = invocationParam.getRpcCallback();
//			if (!StringKit.isEmpty(serviceAddress)) {
//				String[] ipAndport = serviceAddress.split(":");
//				if (ipAndport.length == 1) {
//					serverIp = ipAndport[0];
//				} else if (ipAndport.length >= 2) {
//					serverIp = ipAndport[0];
//					serverPort = ipAndport[1];
//				}
//			}
//		}

        //获取并清除 Param参数
        InvocationParam invocationParam = ClientInvocationManager.INSTANCE.getAndClearParam();

        //找到一个合适的client
        NettyClient client = getClient(invocationParam);

        //调用
        return client.getClientChannelHandler().invoke4Json(serviceName, method, args,invocationParam);

    }

    private  synchronized  NettyClient getClient(InvocationParam invocationParam) {
        String designateAddress = null;
        if (invocationParam!=null){
            designateAddress = invocationParam.getServiceAddress();
        }
        NettyClient nettyClient ;
        if (StringKit.isNull(designateAddress)){
            nettyClient = computeTargetClient();
            if (nettyClient==null){
                throw new RuntimeException("can't find a proper target client");
            }
        }else{
            nettyClient = findTargetClient(designateAddress);
            if (nettyClient==null){
                throw new RuntimeException("target client not found or not active:"+designateAddress);
            }
        }
    }

    int index;
    private NettyClient computeTargetClient() {
        int pos = ++index % nettyClients.length;

        while （!nettyClients[pos].isActive()) pos++;

    }

    private NettyClient findTargetClient(String designateAddress) {
        for (int i=0;i<nettyClients.length;i++){
            if (designateAddress.equals(nettyClients[i].getRemoteAddr())){
                return nettyClients[i];
            }
        }
        return null;
    }


    public void send(RpcMessage msg){
//        channelGroup.toArray()
    }



    public void start() {
        NettyClient client = new NettyClient("127.0.0.1", 9090, 1);
        nettyClients.add(client);
        client.bootstrap(true);

    }
}
