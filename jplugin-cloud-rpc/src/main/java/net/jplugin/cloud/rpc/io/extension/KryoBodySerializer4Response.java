package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.kryo.KryoSerializer;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.cloud.rpc.io.message.RpcResponse;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.cloud.rpc.io.util.TypeUtil;
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

        //解决序列化错误，暂时把type清理掉,rpc模式不需要传递type
        RpcResponse res = (RpcResponse) body;
//        res.setResultType(TypeUtil.deepClone(res.getResultType()));
        res.setResultType(null);

        KryoSerializer.serialize(body,stream);
    }


}
