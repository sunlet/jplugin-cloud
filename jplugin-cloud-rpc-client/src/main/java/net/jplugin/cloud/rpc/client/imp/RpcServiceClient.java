package net.jplugin.cloud.rpc.client.imp;

import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.message.RpcMessage;

import java.util.List;

public class RpcServiceClient {
    //保存所有
    List<NettyClient> nettyClients;

    //保存所有active的channel



    public void send(RpcMessage msg){
//        channelGroup.toArray()
    }
}
