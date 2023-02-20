package net.jplugin.cloud.rpc.io.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import net.jplugin.cloud.rpc.io.future.CallFuture;
import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.cloud.rpc.io.spi.IMessageBodySerializer;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.CalenderKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.client.ICallback;
import net.jplugin.common.kits.client.InvocationParam;
import net.jplugin.core.kernel.api.RefAnnotationSupport;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;


import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static net.jplugin.cloud.rpc.io.client.RpcClientContext.invokeExecute;

public class ClientChannelHandler extends RefAnnotationSupport {

    @RefLogger
    private static Logger logger;

//	private static final Map<String, NettyChannel> channels = new ConcurrentHashMap<>();

    private Channel channel;

    private String channelId;

    private long initTime = System.currentTimeMillis();

    FutureManager futureManager = new FutureManager();


    public ClientChannelHandler(Channel aChannel) {
        if (aChannel == null || !aChannel.isActive()) {
            throw new IllegalArgumentException("netty channel is invalid");
        }
		channelId = aChannel.id().asLongText();

        this.channel = aChannel;

//		NettyChannel innerChannel = channels.get(channelId);
//		if (!IoUtils.isValidChannel(innerChannel)) {
//			this.channel = channel;
//			closed = false;
//			channels.put(channelId, this);
//		}
    }

    public Object invoke4Kryo(String serviceName,Method method, Object[] args) throws Exception {
        return RpcClientContext.invokeExecute(this,serviceName, method, args, IMessageBodySerializer.TYPE_KRYO_REQ);
    }

    public Object invoke4Json(String serviceName, Method method, Object[] args, InvocationParam invocationParam) throws Exception {
        return RpcClientContext.invokeExecute(this,serviceName, method, args, IMessageBodySerializer.TYPE_JSON_REQ);
    }



    public CallFuture removeFuture(String reqid){
        return futureManager.removeFuture(reqid);
    }

    public String channelId() {
        return this.channelId;
    }

    public SocketAddress localAddress() {
        return this.channel == null ? null : this.channel.localAddress();
    }

    public SocketAddress remoteAddress() {
        return this.channel == null ? null : this.channel.remoteAddress();
    }

    public boolean isConnected() {
        return this.channel != null  && this.channel.isActive();
    }

    public Object syncSend(RpcMessage<RpcRequest> request, long timeout) throws Exception {
        CallFuture<?> cf = null;
        try {
            cf = this.asyncSend(request, false, null);
            cf.setTimeout(timeout);
            return cf.getVal();
        } finally {
            if (cf != null) {
                futureManager.removeFuture(cf.getContextId());
            }
        }
    }


    private ChannelFuture writeAndFlush(Object obj) {
        return this.channel.writeAndFlush(obj);
    }

    public long getInitTime() {
        return this.initTime;
    }

    public CallFuture<?> asyncSend(RpcMessage<RpcRequest> request, boolean async, ICallback callback) {
        Objects.requireNonNull(request, "request null!");

        AssertKit.assertTrue(this.channel!=null && this.channel.isActive());

//        String contentextId = makeUniqueRequestId();
        AssertKit.assertStringNull(request.getHeader().get(RpcMessage.HEADER_REQ_ID),"reqid");
        request.header(RpcMessage.HEADER_REQ_ID, getNextReqId());
        String contentextId  = request.getHeader().get(RpcMessage.HEADER_REQ_ID);

        CallFuture<?> callFuture = new CallFuture<>(remoteAddress());
        callFuture.setContextId(contentextId);
        callFuture.setAsync(async);
        callFuture.setCallback(callback);
//        callFuture.setRtnclz(request.getRtnclz());

        // 将请求中的返回结果类型置空
//        request.setRtnclz(null);

//        FutureUtils.addFuture(callFuture);
        futureManager.addFuture(contentextId, callFuture);

        this.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if (!cf.isSuccess()) {
                    logger.error("Request=[" + request + "]异常：" + cf.cause().getMessage(), cf.cause());
                    CallFuture<?> cacheFuture = futureManager.removeFuture(contentextId);
                    if (cacheFuture == null) {
                        return;
                    }
                    cacheFuture.setException(cf.cause(), cf.channel().remoteAddress());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("请求发送成功，contentextId=" + contentextId + ",currTimestamp="
                                + System.currentTimeMillis() + ",channel=>" + cf.channel());
                    }
                }
            }
        });
        return callFuture;
    }

    static String startTime = CalenderKit.getCurrentTimeString();
    static AtomicLong index = new AtomicLong(1);
    private static String getNextReqId() {
        return startTime+"-"+index.addAndGet(1);
    }

//	public static void removeChannel(String channelId) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("channelsize=" + channels.keySet().size() + ",channels=" + channels);
//		}
//		NettyChannel innerChannel = channels.remove(channelId);
//		if (innerChannel != null) {
//			innerChannel.close();
//			innerChannel = null;
//		}
////		ClientContextUtil.removeClient(channelId);
//	}
//
//	private static IChannel getChannel(String channelId) {
//		return channels.get(channelId);
//	}

    public String toString() {
        return "NettyChannel[ChannelId=" + this.channelId + ",LocalAddress=" + localAddress() + ",RemoteAddress="
                + remoteAddress() + ",Connected=" + isConnected()  + ",InitTime(ms)=" + initTime
                + "]";
    }

//	public static void removeChannelIfInactive(Channel ch) {
//		if (ch != null && !ch.isActive()) {
//			removeChannel(ch.id().asLongText());
//		}
//	}

}
