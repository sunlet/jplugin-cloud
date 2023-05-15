package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.kryo.KryoSerializer;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.cloud.rpc.io.util.TypeUtil;
import net.jplugin.common.kits.AssertKit;
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
        AssertKit.assertEqual(body.getClass(),RpcRequest.class);

        //解决序列化错误,RPC模式，或者把参数类型做成可序列化的，或者可以设置为空（因为用不到）
        RpcRequest req = (RpcRequest) body;
//        req.setGenericTypes( TypeUtil.getSerialAbleTypes(req.getGenericTypes()));
        req.setGenericTypes(null);

        KryoSerializer.serialize(body,stream);
    }


}
