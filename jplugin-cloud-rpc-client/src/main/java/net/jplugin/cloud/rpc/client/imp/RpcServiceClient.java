package net.jplugin.cloud.rpc.client.imp;

import net.jplugin.cloud.rpc.client.kits.Util;
import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.common.kits.ObjectRef;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.RefConfig;
import net.jplugin.core.kernel.api.RefAnnotationSupport;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class RpcServiceClient extends RefAnnotationSupport {
    String targetAppCode;
    private static NettyClient[] EMPTY_ARR = new NettyClient[0];

    //保存所有
    NettyClient[] nettyClients =  EMPTY_ARR;

    //last invoke time
    AtomicLong lastExecuteTime = new AtomicLong();

    //boolean
    boolean started = false;

    public RpcServiceClient(String code) {
        targetAppCode = code;
    }

    //保存所有active的channel


    public Object invokeRpc(String serviceName, Method method, Object[] args, AbstractMessageBodySerializer.SerializerType serializerType) throws Exception {
        //设置上次时间
        lastExecuteTime.set(System.currentTimeMillis());

        //获取并清除 Param参数
        InvocationParam invocationParam = ClientInvocationManager.INSTANCE.getAndClearParam();

        //找到一个合适的client
        NettyClient client = getClient(invocationParam);

        //调用
        return client.getClientChannelHandler().invoke(serviceName, method, args,invocationParam,serializerType);
    }

//    private synchronized  void addClient(NettyClient ...toAdd) {
//        NettyClient[] temp = new NettyClient[this.nettyClients.length+toAdd.length];
//
//        for (int i=0;i<nettyClients.length;i++){
//            temp[i] = nettyClients[i];
//        }
//        for (int i=0;i<toAdd.length;i++){
//            temp[nettyClients.length+i] = toAdd[i];
//        }
//
//        nettyClients = temp;
//    }

    /**
     * 这是一个同步方法，执行需要尽量快
     * @param invocationParam
     * @return
     */
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

        return nettyClient;
    }

    int globalIndex;
    private NettyClient computeTargetClient() {
        int len = nettyClients.length;

        //最多循环所有的Client。避免死循环
        for (int cnt=0; cnt<len ; cnt ++) {
            //取余数
            int pos = ++globalIndex % len;
            if (nettyClients[pos].isConnected() ){
                return nettyClients[pos];
            }
        }
        //循环一遍，还没找到
        return null;
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
        throw new RuntimeException("not impl");
    }


    /**
     * 启动的时候直接传入host
     * @param hostAddrs
     */
    public void start(Set<String> hostAddrs) {
        this.started = true;
        updateHosts(hostAddrs);
    }

    @RefConfig(path = "rpc.per-client-work-count",defaultValue = "1")
    Integer rpcClientWorks;

    public synchronized void updateHosts(Set<String> newHosts){
        if (!started)
            throw new RuntimeException("not start");

        NettyClient[] temp = new NettyClient[newHosts.size()];

        String[] newHostsArr = newHosts.toArray(new String[newHosts.size()]);

        for (int i=0;i<newHostsArr.length;i++){
            String addr = newHostsArr[i];

            NettyClient findResult = findTargetClient(addr);
            if (findResult!=null){
                temp[i] = findResult;
            }else{

                Tuple2<String, Integer> ipPort = Util.splitAddr(addr);
                temp[i] = new NettyClient(ipPort.first,ipPort.second,rpcClientWorks);
                temp[i].bootstrap();
            }
        }
        this.nettyClients = temp;
    }




    /**
     * 测试连上了任意一个
     * @return
     */
    public boolean connectedAny() {
        for (int i=0;i<nettyClients.length;i++){
            if (nettyClients[i].isConnected()) {
                return true;
            }
        }
        return false;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(" AppCode="+this.targetAppCode);
        for(int i=0;i<nettyClients.length;i++){
            NettyClient c = nettyClients[i];
            sb.append(" ").append(c.getRemoteAddr()).append("-").append(c.isConnected());
        }
        return sb.toString();
    }

    public void maintainConnect() {
        for (int i=0;i<nettyClients.length;i++){
            NettyClient temp = nettyClients[i];
            temp.mainTainConnection();
        }
    }
}
