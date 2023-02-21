package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.kryo.KryoSerializer;
import net.jplugin.cloud.rpc.io.spi.IMessageBodySerializer;
import net.jplugin.core.kernel.api.BindExtension;

import java.io.IOException;

@BindExtension(name= IMessageBodySerializer.TYPE_KRYO_RES)
public class KryoBodySerializer4Response implements IMessageBodySerializer {

    @Override
    public Object deSerialBody(ByteBufInputStream input) throws IOException {
        return KryoSerializer.deserialize(input);
    }


    @Override
    public void serialBody(ByteBufOutputStream stream, Object body) throws IOException {
        KryoSerializer.serialize(body,stream);
    }


}
