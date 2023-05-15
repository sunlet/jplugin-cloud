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

        RpcMessage msg;
        try{
            msg = RpcMessage.deSerialize(frame);
        }catch(Throwable th){
            msg = RpcMessage.create(RpcMessage.TYPE_MSG_DOCODE_ERROR)
                    .header(RpcMessage.HEADER_ERROR_INFO, th.getMessage())
                    .header(RpcMessage.HEADER_DECODE_ERROR_MSG_FROM, "server-decode");
        }
        return msg;
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length);
    }
}