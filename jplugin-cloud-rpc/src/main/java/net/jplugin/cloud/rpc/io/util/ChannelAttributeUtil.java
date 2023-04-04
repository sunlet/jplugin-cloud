package net.jplugin.cloud.rpc.io.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.jplugin.cloud.rpc.io.client.ClientChannelHandler;
import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.message.RpcMessage;

public class ChannelAttributeUtil {
    //    private static AttributeKey RPC_CHANNEL_ATTR =AttributeKey.newInstance("RPC_CHANNEL_ATTR");
    private static AttributeKey CLIENT_CHANNEL_HANDLER =AttributeKey.newInstance("CLIENT_CHANNEL_HANDLER");
    private static AttributeKey CLIENT_INFO_MESSAGE =AttributeKey.newInstance("CLIENT_INFO_MESSAGE");
    private static AttributeKey SERVER_INFO_MESSAGE =AttributeKey.newInstance("SERVER_INFO_MESSAGE");
    private static AttributeKey NETTY_CLIENT =AttributeKey.newInstance("NETTY_CLIENT");

//    public static NettyChannel getOrCreateRpcChannel(Channel channel){
//        Attribute<NettyChannel> attr = channel.attr(RPC_CHANNEL_ATTR);
//        if (attr.get()==null){
//            synchronized (attr){
//                if (attr.get()==null){
//                    NettyChannel rpcChannel = new NettyChannel(channel);
//                    attr.set(rpcChannel);
//                }
//            }
//        }
//        return attr.get();
//    }


    public static ClientChannelHandler getOrCreateClientChannelHandler(Channel channel){
        Attribute<ClientChannelHandler> attr = channel.attr(CLIENT_CHANNEL_HANDLER);
        if (attr.get()==null){
            synchronized (attr){
                if (attr.get()==null){
                    ClientChannelHandler rpcChannel = new ClientChannelHandler(channel);
                    attr.set(rpcChannel);
                }
            }
        }
        return attr.get();
    }

    public static void setClientInfo(ChannelHandlerContext channel, RpcMessage msg) {
        Attribute<RpcMessage> attr = channel.channel().attr(CLIENT_INFO_MESSAGE);
        if (attr.get()!=null){
            throw new RuntimeException("Client info already received!");
        }else{
            attr.set(msg);
        }
    }
    public static void setServerInfo(ChannelHandlerContext channel, RpcMessage msg) {
        Attribute<RpcMessage> attr = channel.channel().attr(SERVER_INFO_MESSAGE);
        if (attr.get()!=null){
            throw new RuntimeException("Server info already received!");
        }else{
            attr.set(msg);
        }
    }

    public static RpcMessage getClientInfo(ChannelHandlerContext ctx) {
        Attribute<RpcMessage> attr = ctx.channel().attr(CLIENT_INFO_MESSAGE);
        return attr.get();
    }

    public static void setNettyClient(Channel channel, NettyClient nettyClient) {
        Attribute<NettyClient> attr = channel.attr(NETTY_CLIENT);
        attr.set(nettyClient);
    }

    public static NettyClient getNettyClient(Channel channel) {
        Attribute<NettyClient> attr = channel.attr(NETTY_CLIENT);
        return attr.get();
    }
}
