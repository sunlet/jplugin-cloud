package net.jplugin.cloud.rpc.client.imp;

import io.netty.channel.group.ChannelGroup;
import net.jplugin.cloud.rpc.io.bootstrap.impl.rpc.NettyClient;
import net.jplugin.cloud.rpc.msg.RpcMessage;

import java.util.List;

public class ServiceClient {
    //保存所有
    List<NettyClient> nettyClients;

    //保存所有active的channel
    ChannelGroup channelGroup;


    public void send(RpcMessage msg){
//        channelGroup.toArray()
    }
}
