package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.kryo.KryoSerializer;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.core.kernel.api.BindExtension;

import java.io.IOException;


@BindExtension
public class KryoBodySerializer4Request extends AbstractMessageBodySerializer {
//    public static final String FLAG="$BODY";

    @Override
    public SerializerType serialType() {
        return SerializerType.KRYO;
    }

    @Override
    public String bodyClass() {
        return RpcRequest.class.getName();
    }

    @Override
    public Object deSerialBody(ByteBufInputStream input) throws IOException {
        return KryoSerializer.deserialize(input);
    }


    @Override
    public void serialBody(ByteBufOutputStream stream, Object body) throws IOException {
        KryoSerializer.serialize(body,stream);
    }


}
