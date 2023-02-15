package net.jplugin.cloud.rpc.io.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.jplugin.cloud.rpc.io.message.RpcMessage;

public class RpcMessageEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        RpcMessage.serializeWithLength(out, (RpcMessage) msg);

    }
}