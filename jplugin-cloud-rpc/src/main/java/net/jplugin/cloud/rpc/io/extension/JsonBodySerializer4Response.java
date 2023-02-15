package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.spi.IMessageBodySerializer;
import net.jplugin.cloud.rpc.io.util.TypeUtil;
import net.jplugin.cloud.rpc.io.message.RpcResponse;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.kernel.api.BindExtension;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;


@BindExtension(name="json")
public class JsonBodySerializer4Response implements IMessageBodySerializer {
    public static final String FLAG="$BODY";

    @Override
    public Object deSerialBody(ByteBufInputStream input) throws IOException {
        RpcResponse response = new RpcResponse();
        //读flag
        AssertKit.assertEqual(FLAG,input.readUTF());
        //读response基础信息
        response.setErrorCode(input.readUTF());
        response.setMessage(input.readUTF());

        //READ type
        response.setResultType(readResultType(input));

        //返回值
        if (!response.getResultType().equals(void.class)){
            String temp = input.readUTF();
            response.setResult(JsonKit.json2Object4TypeEx(temp, response.getResultType()));
        }

        return response;

    }

    private Type readResultType(ByteBufInputStream input) {
        try {
            ObjectInputStream ois = new ObjectInputStream(input);
            return (Type) ois.readObject();
        }catch(Exception e){
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    @Override
    public void serialBody(ByteBufOutputStream stream, Object body) throws IOException {
        AssertKit.assertEqual(body.getClass(),RpcResponse.class);
        RpcResponse req = (RpcResponse) body;

        //写flag
        stream.writeUTF(FLAG);

        //服务定位
        stream.writeUTF(StringKit.null2Empty(req.getErrorCode()));
        stream.writeUTF(StringKit.null2Empty(req.getMessage()));

        //返回類型
        writeReturnType(stream,req.getResultType());

        //返回值
        if (!void.class.equals(req.getResultType())){
            stream.writeUTF(JsonKit.object2JsonEx(req.getResult()));
        }

    }

    private void writeReturnType(ByteBufOutputStream stream, Type resultType) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(TypeUtil.deepClone(resultType));
    }
}
