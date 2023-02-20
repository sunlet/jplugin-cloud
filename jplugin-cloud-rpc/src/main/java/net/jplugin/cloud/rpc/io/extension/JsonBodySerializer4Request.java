package net.jplugin.cloud.rpc.io.extension;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
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


@BindExtension(name=IMessageBodySerializer.TYPE_JSON_REQ)
public class JsonBodySerializer4Request implements IMessageBodySerializer {
    public static final String FLAG="$BODY";

    @Override
    public Object deSerialBody(ByteBufInputStream input) throws IOException {
        RpcRequest request = new RpcRequest();
        //读flag
        AssertKit.assertEqual(FLAG,input.readUTF());
        //读服务定位
        request.setUri(input.readUTF());
        request.setMethodName(input.readUTF());
        //读参数个数
        int cnt = input.readShort();

        //读取type
        request.setGenericTypes(readTypes(input));

        //读参数
        Type[] types = request.getGenericTypes();

        Object[] args = new Object[cnt];
        for (int i=0;i<args.length;i++) {
            String temp = input.readUTF();
            args[i] = JsonKit.json2Object4TypeEx(temp,types[i]);
        }

        request.setArguments(args);
        return request;
    }

    private Type[] readTypes(ByteBufInputStream input){
        try {
            ObjectInputStream ois = new ObjectInputStream(input);
            return (Type[]) ois.readObject();
        }catch(Exception e){
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    @Override
    public void serialBody(ByteBufOutputStream stream, Object body) throws IOException {
        AssertKit.assertEqual(body.getClass(),RpcRequest.class);
        RpcRequest req = (RpcRequest) body;

        //写flag
        stream.writeUTF(FLAG);

        //服务定位
        stream.writeUTF(StringKit.null2Empty(req.getUri()));
        stream.writeUTF(StringKit.null2Empty(req.getMethodName()));

        //参数个数
        stream.writeShort(req.getArguments().length);

        //写type
        writeTypes(stream,req.getGenericTypes());

        //参数
        for (Object o:req.getArguments()){
            stream.writeUTF(JsonKit.object2JsonEx(o));
        }
    }

    private void writeTypes(ByteBufOutputStream stream, Type[] genericTypes) throws IOException {
        Type[] serialAbleTypes = getSerialAbleTypes(genericTypes);
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(serialAbleTypes);
    }

    private Type[] getSerialAbleTypes(Type[] genericTypes) {
        //如果沒有汎型，不處理
        if (Arrays.stream(genericTypes).filter(o->{return !(o instanceof Serializable);}).count()==0) {
            return genericTypes;
        }

        //生成
        return Arrays.stream(genericTypes).map(o->{
            return TypeUtil.deepClone(o);
        }).toArray(Type[]::new);
    }

}
