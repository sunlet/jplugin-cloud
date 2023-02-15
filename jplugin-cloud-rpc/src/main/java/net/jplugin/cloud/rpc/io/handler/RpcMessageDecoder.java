package net.jplugin.cloud.rpc.io.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.jplugin.cloud.rpc.io.message.RpcMessage;

public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final int max_length = 5*1024*1024;

    public RpcMessageDecoder() {
        super(max_length, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        return RpcMessage.deSerialize(frame);
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length);
    }
}