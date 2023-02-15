package net.jplugin.cloud.rpc.io.handler;

import io.netty.channel.*;
import net.jplugin.cloud.rpc.common.util.ExceptionUtils;
//import net.jplugin.cloud.rpc.io.util.ClientContextUtil;
import net.jplugin.cloud.rpc.io.util.ThreadPoolManager;

import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.cloud.rpc.io.message.RpcResponse;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.rclient.api.RemoteExecuteException;
import net.jplugin.core.service.impl.esf.ESFHelper2;
import net.jplugin.core.service.impl.esf.ESFRPCContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcServerMessageHandler extends ChannelInboundHandlerAdapter {

    private ThreadPoolExecutor serverWorkers;
    private ThreadPoolExecutor  heartWorkers;


    private static final Logger logger = LoggerFactory.getLogger(RpcServerMessageHandler.class.getName());


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
            case RpcMessage.TYPE_CLIENT_HEART_BEAT:
                processClientHeartBeat(message,ctx);
            case RpcMessage.TYPE_CLIENT_REQ:
                processClientReq(message,ctx);
            default:
                throw new RuntimeException("Unsupport Message Type");
        }
    }

    private void processClientInfo(ChannelHandlerContext ctx, Object msg) {
        throw new RuntimeException("not impl");
    }

    private void processClientReq(RpcMessage message, ChannelHandlerContext ctx) {
        final long acceptTime = System.currentTimeMillis();

        // 服务请求，保存客户端的基本信息与连接
        if (logger.isDebugEnabled()) {
            logger.debug("收到服务请求 act=" + acceptTime + ",cid=" + message.getHeader().get(RpcMessage.HEADER_REQ_ID));
        }

        serverWorkers.execute(() -> handleServerMethod(ctx, message,acceptTime));
    }

    private void handleServerMethod(ChannelHandlerContext ctx, RpcMessage message, long acceptTime) {
        RpcResponse response = new RpcResponse();
        try{
            RpcMessage<RpcResponse> resp = RpcMessage.create(RpcMessage.TYPE_SERVER_RES);

            String reqid = (String) message.getHeader().get(RpcMessage.HEADER_REQ_ID);
            if (StringKit.isNotNull(reqid))
                resp.header(RpcMessage.HEADER_REQ_ID, reqid);
            String serialAlgm = (String) message.getHeader().get(RpcMessage.HEADER_SERIAL_TYPE);
            if (StringKit.isNotNull(serialAlgm))
                resp.header(RpcMessage.HEADER_SERIAL_TYPE,serialAlgm);

            Tuple2<Type,Object>  ret = callServerMethod(ctx,message,acceptTime);
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
            logInvokeError(e,ctx,message,acceptTime);
        }

        Channel channel = ctx.channel();
        if (channel == null || !channel.isActive()) {
            if (logger.isWarnEnabled()) {
                logChannelInactive(ctx,message,acceptTime);
            }
        } else {
            channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

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


    private Tuple2<Type,Object> callServerMethod(ChannelHandlerContext ctx, RpcMessage msg, long acceptTime) throws Throwable {
        RpcRequest req = (RpcRequest) msg.getBody();

        String uri = req.getUri();
        String methodName = req.getMethodName();

        Object obj = ESFHelper2.getObject(uri);
        if (obj==null){
            throw new RuntimeException("uri error, "+uri);
        }

        Method method = Util.getMethod(obj.getClass(), methodName);

        Object[] args = req.getArguments();
        
        ESFRPCContext esfRpcCtx = getTheESFRpcContext(ctx,msg);
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

    private void logInvokeError(Throwable e, ChannelHandlerContext ctx, RpcMessage message, long acceptTime) {
        logger.error("invoke error. ");
    }


}
