package net.jplugin.cloud.rpc.io.util;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.jplugin.cloud.rpc.io.client.ClientChannelHandler;

public class ChannelAttributeUtil {
//    private static AttributeKey RPC_CHANNEL_ATTR =AttributeKey.newInstance("RPC_CHANNEL_ATTR");
    private static AttributeKey CLIENT_CHANNEL_HANDLER =AttributeKey.newInstance("CLIENT_CHANNEL_HANDLER");

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
}
