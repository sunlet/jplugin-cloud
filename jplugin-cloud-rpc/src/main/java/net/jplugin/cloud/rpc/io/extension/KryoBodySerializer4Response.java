package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.kryo.KryoSerializer;
import net.jplugin.cloud.rpc.io.message.RpcResponse;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.core.kernel.api.BindExtension;

import java.io.IOException;

@BindExtension
public class KryoBodySerializer4Response extends AbstractMessageBodySerializer {

    @Override
    public SerializerType serialType() {
        return SerializerType.KRYO;
    }

    @Override
    public String bodyClass() {
        return RpcResponse.class.getName();
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
