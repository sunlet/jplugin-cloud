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
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;

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
    boolean closed = true;

    public RpcServiceClient(String code) {
        targetAppCode = code;
    }

    //保存所有active的channel

    @RefLogger
    Logger logger;



    public Object invokeRpc(String serviceName, Method method, Object[] args, AbstractMessageBodySerializer.SerializerType serializerType) throws Exception {
        //检查状态，如果必要open
        checkStateAndOpen();

        //设置上次时间
        lastExecuteTime.set(System.currentTimeMillis());

        //获取并清除 Param参数
        InvocationParam invocationParam = ClientInvocationManager.INSTANCE.getAndClearParam();

        //找到一个合适的client
        NettyClient client = getClient(invocationParam);

        //调用
        return client.getClientChannelHandler().invoke(serviceName, method, args,invocationParam,serializerType);
    }

    /**
     * 这里在需要时启动，注意并发的场景
     */
    private void checkStateAndOpen() {
        if (closed){
            synchronized (this){
                if (closed){
                    logger.warn("Now to reopen service client:"+this.targetAppCode);
                    this.start();

                    for (int i=0;i<10;i++){
                        try {
                            Thread.sleep(200);
                            if (this.connectedAny()) {
                                logger.warn("start ok after "+(i+1)+" test.");
                                break;
                            }
                            logger.error("Service client not connect in limit time!");
                        }catch(Exception e){}
                    }
                }
            }
        }
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
     */
    public synchronized void start() {
        this.closed = false;

        for (int i=0;i<this.nettyClients.length;i++){
            NettyClient nc = nettyClients[i];
            if (nc.isClientClosed()){
                logger.info("Not to bootstrap client ,"+nc.getRemoteAddr());
                nc.bootstrap();
            }else{
                logger.warn("The client shoud be closed,but active!,"+nc.getRemoteAddr());
            }
        }
    }

    @RefConfig(path = "cloud-rpc.client-workers-for-channel",defaultValue = "1")
    Integer rpcClientWorks;

    public synchronized void updateHosts(Set<String> newHosts){
//        if (closed)
//            throw new RuntimeException("not start");

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
                if (!closed) {
                    temp[i].bootstrap();
                    logger.info("AddHost and bootstrap. "+ipPort);
                }else{
                    logger.info("AddHost ,not bootstrap."+ipPort);
                }
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

    public synchronized void maintainConnect(int keepMillSeconds) {
        if (keepMillSeconds>0){
            if (System.currentTimeMillis() - this.lastExecuteTime.get()>=keepMillSeconds){
                if (!this.closed) {
                    System.out.println("Connection idle time out, close it. " + this.targetAppCode);
                    close();
                }
            }
        }
        for (int i=0;i<nettyClients.length;i++){
            NettyClient temp = nettyClients[i];
            temp.mainTainConnection();
        }
    }

    public synchronized  void close(){
        this.closed = true;
        for (int i=0;i<nettyClients.length;i++){
            NettyClient temp = nettyClients[i];
            temp.closeClient();
        }
    }
}
