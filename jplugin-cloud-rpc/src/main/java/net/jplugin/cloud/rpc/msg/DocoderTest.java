package net.jplugin.cloud.rpc.msg;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class DocoderTest extends LengthFieldBasedFrameDecoder {
    public DocoderTest(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }



}
