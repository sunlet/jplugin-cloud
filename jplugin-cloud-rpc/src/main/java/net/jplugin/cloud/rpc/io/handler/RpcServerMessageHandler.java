package net.jplugin.cloud.rpc.io.handler;

import io.netty.channel.*;
import net.jplugin.cloud.rpc.common.util.ExceptionUtils;
//import net.jplugin.cloud.rpc.io.util.ClientContextUtil;
import net.jplugin.cloud.rpc.io.util.ChannelAttributeUtil;
import net.jplugin.cloud.rpc.io.util.MessageUtil;
import net.jplugin.cloud.rpc.io.util.ThreadPoolManager;

import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.cloud.rpc.io.message.RpcResponse;
import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.log.api.LogFactory;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.rclient.api.RemoteExecuteException;
import net.jplugin.core.service.impl.esf.ESFHelper2;
import net.jplugin.core.service.impl.esf.ESFRPCContext;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcServerMessageHandler extends ChannelInboundHandlerAdapter {

    private ThreadPoolExecutor serverWorkers;
    private ThreadPoolExecutor  heartWorkers;


    private static final Logger logger = LogFactory.getLogger(RpcServerMessageHandler.class);


    public RpcServerMessageHandler() {
        serverWorkers = ThreadPoolManager.INSTANCE.getServerWorkers();
        heartWorkers=ThreadPoolManager.INSTANCE.getSendHeartWorkers();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcMessage message = (RpcMessage) msg;

        switch(message.getMsgType()){
            case RpcMessage.TYPE_CLIENT_INFO:
                processClientInfo(ctx,msg);
                break;
            case RpcMessage.TYPE_CLIENT_HEART_BEAT:
                processClientHeartBeat(message,ctx);
                break;
            case RpcMessage.TYPE_CLIENT_REQ:
                processClientReq(message,ctx);
                break;
            default:
                throw new RuntimeException("Unsupport Message Type."+message.getMsgType());
        }
    }

    private void processClientInfo(ChannelHandlerContext ctx, Object msg) {
        ChannelAttributeUtil.setClientInfo(ctx, (RpcMessage) msg);
        if (logger.isInfoEnabled()){
            logger.info("Recept client info:"+getClientInfoString((RpcMessage)msg));
        }
        //SEND MESSAGE
        RpcMessage serverInfoMessage = MessageUtil.getServerInfoMessage();
        ctx.writeAndFlush(serverInfoMessage);
    }

    private String getClientInfoString(RpcMessage msg) {
        return JsonKit.object2JsonEx(msg.getHeader());
    }


    private void processClientReq(RpcMessage message, ChannelHandlerContext ctx) {
        final long acceptTime = System.currentTimeMillis();

        // 服务请求，保存客户端的基本信息与连接
        if (logger.isDebugEnabled()) {
            logger.debug("收到服务请求 act=" + acceptTime + ",cid=" + message.getHeader().get(RpcMessage.HEADER_REQ_ID));
        }

        //判断clientrinfo是否已经收到
        if (ChannelAttributeUtil.getClientInfo(ctx)==null){
            throw new RuntimeException("client info is null");
        }

        serverWorkers.execute(() -> handleServerMethod(ctx, message,acceptTime));
    }

    private void handleServerMethod(ChannelHandlerContext ctx, RpcMessage message, long acceptTime) {
        ESFRPCContext esfRpcContext = getTheESFRpcContext(ctx, message);

        //生产返回消息
        RpcMessage<RpcResponse> resp = getRpcResponseRpcMessage(esfRpcContext, message, acceptTime);

        //写回返回消息
        writeResponseMessage(ctx, message, acceptTime, resp);
    }

    private RpcMessage<RpcResponse> getRpcResponseRpcMessage(ESFRPCContext esfCtx, RpcMessage message, long acceptTime) {
        RpcMessage<RpcResponse> resp = RpcMessage.create(RpcMessage.TYPE_SERVER_RES);
        RpcResponse response = new RpcResponse();
        resp.body(response);

        try{
            String reqid = (String) message.getHeader().get(RpcMessage.HEADER_REQ_ID);
            if (StringKit.isNotNull(reqid))
                resp.header(RpcMessage.HEADER_REQ_ID, reqid);
//            resp.header(RpcMessage.HEADER_SERIAL_TYPE, IMessageBodySerializer.TYPE_JSON_RES);

            String serialAlgm = (String) message.getHeader().get(RpcMessage.HEADER_SERIAL_TYPE);
            if (StringKit.isNotNull(serialAlgm))
                resp.header(RpcMessage.HEADER_SERIAL_TYPE,serialAlgm);

            Tuple2<Type,Object> ret = callServerMethod(esfCtx,message,acceptTime);
            response.setResult(ret.second);
            response.setResultType(ret.first);
            response.setErrorCode("0");

        }catch(Throwable te){
            Throwable e = te;
            if (te != null && !(te instanceof RemoteExecuteException) && (te instanceof InvocationTargetException
                    || te.getCause() instanceof InvocationTargetException)) {
                e = ExceptionUtils.unwrapThrowable(te.getCause());
            }
            if (e instanceof RemoteExecuteException) {
                RemoteExecuteException re = (RemoteExecuteException) e;
                response.setErrorCode(re.getCode());
                response.setMessage(re.getMessage());
            } else {
                response.setErrorCode("-1");
                response.setMessage(e.getMessage());
            }
            logInvokeError(e,esfCtx,message,acceptTime);
        }
        return resp;
    }

    private void writeResponseMessage(ChannelHandlerContext ctx, RpcMessage message, long acceptTime, RpcMessage<RpcResponse> resp) {
        Channel channel = ctx.channel();
        if (channel == null || !channel.isActive()) {
            if (logger.isInfoEnabled()) {
                logChannelInactive(ctx,message,acceptTime);
            }
        } else {
            channel.writeAndFlush(resp).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        logWriteRespError(future.cause(),ctx,message,acceptTime);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logWriteSuccess(ctx,message,acceptTime);
                        }
                    }
                }
            });
        }
    }

    private Tuple2<Type,Object> callServerMethod(ESFRPCContext esfRpcCtx, RpcMessage msg, long acceptTime) throws Throwable {
        RpcRequest req = (RpcRequest) msg.getBody();

        String uri = req.getUri();
        String methodName = req.getMethodName();

        Object obj = ESFHelper2.getObject(uri);
        if (obj==null){
            throw new RuntimeException("uri error, "+uri);
        }

        Method method = Util.getMethod(obj.getClass(), methodName);

        Object[] args = req.getArguments();
        
//        ESFRPCContext esfRpcCtx = getTheESFRpcContext(ctx,msg);
        esfRpcCtx.setMsgReceiveTime(acceptTime);
        esfRpcCtx.setRequestUrl(Util.convertURL(uri,methodName,args));

        Object result =  ESFHelper2.invokeRPC(esfRpcCtx,uri,obj,method,args);
        return Tuple2.with(method.getGenericReturnType(),result);
    }

    private ESFRPCContext getTheESFRpcContext(ChannelHandlerContext ctx, RpcMessage msg) {
        ESFRPCContext rcx = new ESFRPCContext();
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        rcx.setCallerIpAddress(remoteAddress.getAddress().getHostAddress());
        rcx.setClientAppCode((String) msg.getHeader().get(RpcMessage.HEADER_CLIENT_APPCODE));
//        rcx.setClientAppToken(_atk);
//        rcx.setOperatorId(_oid);
//        rcx.setOperatorToken(_otk);
        rcx.setTenantId((String) msg.getHeader().get(RpcMessage.HEADER_TENANT_ID));
        rcx.setGlobalReqId((String) msg.getHeader().get(RpcMessage.HEADER_GLOBAL_REQ_ID));
        return rcx;
    }


    private void processClientHeartBeat(RpcMessage message, ChannelHandlerContext ctx) {
        throw new RuntimeException("not impl");
    }

    private void logWriteSuccess(ChannelHandlerContext ctx, RpcMessage message, long acceptTime) {
        logger.info("call success. ");
    }

    private void logWriteRespError(Throwable cause, ChannelHandlerContext ctx, RpcMessage message, long acceptTime) {
        logger.error("write error. ");
    }


    private void logChannelInactive(ChannelHandlerContext ctx, RpcMessage message, long acceptTime) {
        logger.error("channel error. ");
    }

    private void logInvokeError(Throwable e, ESFRPCContext ctx, RpcMessage message, long acceptTime) {
        logger.error("invoke error. ",e);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
