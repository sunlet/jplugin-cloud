package net.jplugin.cloud.rpc.io.server;

import net.jplugin.cloud.rpc.io.message.RpcMessage;

public class ServerHandlerContext {
    RpcMessage reqMessage;
    long acceptTime;

    public ServerHandlerContext(RpcMessage o){
        this.reqMessage = o;
        this.acceptTime = System.currentTimeMillis();
    }

    public RpcMessage getReqMessage() {
        return reqMessage;
    }

    public long getAcceptTime() {
        return acceptTime;
    }
}
