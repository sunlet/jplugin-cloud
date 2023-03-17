package net.jplugin.cloud.rpc.client.imp;

import net.jplugin.cloud.rpc.client.api.RpcContext;
import net.jplugin.cloud.rpc.client.kits.Util;
import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.RefConfig;
import net.jplugin.core.kernel.api.RefAnnotationSupport;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RpcServiceClient extends RefAnnotationSupport {
    String targetAppCode;
    private static NettyClient[] EMPTY_ARR = new NettyClient[0];

    //保存所有
    NettyClient[] nettyClients =  EMPTY_ARR;

    //last invoke time
    AtomicLong lastExecuteTime = new AtomicLong();

    //boolean
    private boolean closed = true;

    public RpcServiceClient(String code) {
        targetAppCode = code;
    }

    //保存所有active的channel

    @RefLogger
    Logger logger;


    public List<RpcContext> _getRpcContextList(){
        return Arrays.stream(nettyClients).map(nc->{return new RpcContext(this,nc.getRemoteAddr());}).collect(Collectors.toList());
    }

    public RpcContext _getRpcContext(String ip){
        for (int i=0;i<nettyClients.length;i++){
            NettyClient nc = nettyClients[i];
            if (ip.equals(nc.getRemoteHostIp())){
                return new RpcContext(this, nc.getRemoteAddr());
            }
        }
        return null;
    }

    public RpcContext _getRpcContext(String ip, int port){
        for (int i=0;i<nettyClients.length;i++){
            NettyClient nc = nettyClients[i];
            if (ip.equals(nc.getRemoteHostIp()) && port==nc.getRemoteHostPort()){
                return new RpcContext(this, nc.getRemoteAddr());
            }
        }
        return null;
    }


    public Object invokeRpc(String serviceName, Method method, Object[] args, AbstractMessageBodySerializer.SerializerType serializerType) throws Exception {
        return invokeRpc(serviceName, method.getName(), method.getGenericParameterTypes(), args, serializerType);
    }

    public Object invokeRpc(String serviceName, String methodName, Type[] argsType, Object[] args, AbstractMessageBodySerializer.SerializerType serializerType){
        //获取并清除 Param参数
        InvocationParam invocationParam = ClientInvocationManager.INSTANCE.getAndClearParam();

        //检查状态，如果必要open
        checkStateAndOpen(invocationParam==null? null:invocationParam.getServiceAddress());

        //设置上次时间
        lastExecuteTime.set(System.currentTimeMillis());

        //找到一个合适的client
        NettyClient client = getClient(invocationParam);

        //调用
        return client.getClientChannelHandler().invoke(serviceName, methodName, argsType,args,invocationParam,serializerType);
    }



    /**
     * 这里在需要时启动，注意并发的场景
     * @param serviceAddress
     */
    private void checkStateAndOpen(String serviceAddress) {
        if (closed){
            synchronized (this){
                if (closed){
                    logger.warn("Now to reopen service client:"+this.targetAppCode);
                    this.start();

                    int i=0;
                    for (;i<10;i++){
                        try {
                            Thread.sleep(200);
                            if (serviceAddress==null) {
                                if (this.connectedAny()) {
                                    logger.warn("start ok after " + (i + 1) + " test.");
                                    break;
                                }
                            }else{
                                if (this.connectedSpecifal(serviceAddress)){
                                    logger.warn("start specifal ok after " + (i + 1) + " test.");
                                    break;
                                }
                            }
                        }catch(Exception e){
                            //发生意外
                            throw new RuntimeException(e);
                        }
                    }

                    //判断是否超时没连好
                    if (i==10){
                        throw new RuntimeException("Service client not connect in limit time!");
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
        System.out.println("$$$$$$$$$$$$$ ServiceClient started1:"+this.toString());

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

        System.out.println("$$$$$$$$$$$$$ ServiceClient started2:"+this.toString());
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
        NettyClient[] oldClients = this.nettyClients;
        this.nettyClients = temp;

        //关闭oldclients中不在新的clients的
        for (int i=0;i<oldClients.length;i++){
            if (!contain(temp,oldClients[i])){
                try {
                    oldClients[i].closeClient();
                }catch(Exception e){
                    logger.error("error to close "+oldClients[i].getRemoteAddr() +" "+this.targetAppCode,e);
                }
            }
        }
    }

    private boolean contain(NettyClient[] arr, NettyClient client) {
        for (int i=0;i<arr.length;i++){
            if (arr[i]==client) {
                return true;
            }
        }
        return false;
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

    /**
     * 指定的target是否连上
     * @param address
     * @return
     */
    public boolean connectedSpecifal(String address) {
        for (int i=0;i<nettyClients.length;i++){
            if (address.equals(nettyClients[i].getRemoteAddr()) && nettyClients[i].isConnected()) {
                return true;
            }
        }
        return false;
    }



    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(" appCode="+this.targetAppCode).append(" closed=").append(this.closed).append(" [");
        for(int i=0;i<nettyClients.length;i++){
            NettyClient c = nettyClients[i];
            sb.append(" ").append(c.getRemoteAddr()).append("- connected:").append(c.isConnected());
        }
        sb.append("]");
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
