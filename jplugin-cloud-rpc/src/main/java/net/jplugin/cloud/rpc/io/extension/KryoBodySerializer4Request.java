package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.kryo.KryoSerializer;
import net.jplugin.cloud.rpc.io.spi.IMessageBodySerializer;
import net.jplugin.cloud.rpc.io.util.TypeUtil;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.kernel.api.BindExtension;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;


@BindExtension(name=IMessageBodySerializer.TYPE_KRYO_REQ)
public class KryoBodySerializer4Request implements IMessageBodySerializer {
//    public static final String FLAG="$BODY";

    @Override
    public Object deSerialBody(ByteBufInputStream input) throws IOException {
        return KryoSerializer.deserialize(input);
    }


    @Override
    public void serialBody(ByteBufOutputStream stream, Object body) throws IOException {
        KryoSerializer.serialize(body,stream);
    }


}
