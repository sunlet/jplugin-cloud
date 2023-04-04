package net.jplugin.cloud.rpc.io.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jplugin.cloud.rpc.io.client.ClientChannelHandler;
import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.future.CallFuture;
//import net.jplugin.cloud.rpc.io.util.ClientContextUtil;
import net.jplugin.cloud.rpc.io.util.ChannelAttributeUtil;
import net.jplugin.cloud.rpc.io.util.MessageUtil;
import net.jplugin.cloud.rpc.io.util.ThreadPoolManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.message.RpcResponse;
import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.log.api.LogFactory;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.rclient.api.RemoteExecuteException;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcClientMessageHandler extends ChannelInboundHandlerAdapter {

    private ThreadPoolExecutor clientWorks;
    private ThreadPoolExecutor  sendHeartWorkers;

    private static final Logger logger = LogFactory.getLogger(RpcClientMessageHandler.class);


    public RpcClientMessageHandler() {
        sendHeartWorkers=ThreadPoolManager.INSTANCE.getSendHeartWorkers();
        clientWorks = ThreadPoolManager.INSTANCE.getClientWorks();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcMessage message = (RpcMessage) msg;

        switch(message.getMsgType()){
            case RpcMessage.TYPE_SERVER_INFO:
                processServerInfo(message,ctx);
                break;
            case RpcMessage.TYPE_SERVER_RES:
                clientWorks.execute(()->processServerResponse(ctx,message));
                break;
            case RpcMessage.TYPE_SERVER_HEART_BEAT:
                processServerHeartBeat(message,ctx);
                break;
            default:
                throw new RuntimeException("Unsupport Message Type");
        }
    }

    private void processServerHeartBeat(RpcMessage message, ChannelHandlerContext ctx) {
        throw new RuntimeException("not impl");
    }

    private void processServerInfo(RpcMessage message, ChannelHandlerContext ctx) {
        ChannelAttributeUtil.setServerInfo(ctx, message);
        if (logger.isInfoEnabled()){
            logger.info("Recept Server info:"+ getServerInfoString(message));
        }

        //initChannel
        NettyClient nc = ChannelAttributeUtil.getNettyClient(ctx.channel());
        if (nc==null) {
            throw new RuntimeException("Can't find nettyClient in attrs.");
        }else{
            nc.initChannel(ctx.channel());
        }

    }

    private String getServerInfoString(RpcMessage message) {
        return JsonKit.object2JsonEx(message.getHeader());
    }

    private void processServerResponse(ChannelHandlerContext ctx, RpcMessage message) {
        RpcResponse resBody = (RpcResponse) message.getBody();

        String reqId = (String) message.getHeader().get(RpcMessage.HEADER_REQ_ID);
        if (StringKit.isNull(reqId)){
            throw new RuntimeException("reqId is null");
        }

        Channel channel = ctx.channel();
        ClientChannelHandler clientChannelHandler = ChannelAttributeUtil.getOrCreateClientChannelHandler(channel);
        if (clientChannelHandler==null){
            throw new RuntimeException("client channel handler is null");
        }

        CallFuture future = clientChannelHandler.removeFuture(reqId);
        if (future==null){
            System.out.println("$$$$$$$$$$$$$$$$ Can't find the future ,perhaps time out");
        }
        Type resType = resBody.getResultType();

        Object result=null;
        if (RpcResponse.DEFAULT_ERROR_CODE.equals(resBody.getErrorCode())) {
            if (!void.class.equals(resType)){
                result = resBody.getResult();
            }
        }else {
            String errCode = resBody.getErrorCode();
            String errMsg = resBody.getMessage();
            RemoteExecuteException ex = new RemoteExecuteException(Integer.parseInt(errCode),errMsg);
            if (future!=null) {
                future.setException(ex, channel.remoteAddress());
            }
        }

        if (future!=null){
            future.setVal(result);
        }


        if (logger.isDebugEnabled()) {
            logger.debug("cid=" + reqId + ",Channel=[" + channel + "], cost(ms) : "
                    + (System.currentTimeMillis() - future.getStartTime()));
        }
    }


}
