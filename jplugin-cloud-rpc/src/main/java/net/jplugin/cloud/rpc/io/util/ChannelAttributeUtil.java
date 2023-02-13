package net.jplugin.cloud.rpc.io.util;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.jplugin.cloud.rpc.io.channel.NettyChannel;

public class ChannelAttributeUtil {
    private static AttributeKey RPC_CHANNEL_ATTR =AttributeKey.newInstance("RPC_CHANNEL_ATTR");

    public static NettyChannel getOrCreateRpcChannel(Channel channel){
        Attribute<NettyChannel> attr = channel.attr(RPC_CHANNEL_ATTR);
        if (attr.get()==null){
            synchronized (attr){
                if (attr.get()==null){
                    NettyChannel rpcChannel = new NettyChannel(channel);
                    attr.set(rpcChannel);
                }
            }
        }
        return attr.get();
    }
}
