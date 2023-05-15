package net.jplugin.cloud.rpc.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.jplugin.cloud.rpc.io.extension.KryoBodySerializer4Request;
import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.common.kits.ReflactKit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class SerializerTest {
    public static void main(String[] args) throws IOException {

        Method method = ReflactKit.findSingeMethodExactly(SerializerTest.class, "simpleMethod");

        testReq(method);

        method = ReflactKit.findSingeMethodExactly(SerializerTest.class, "complexMethod");
        testReq(method);

        method = ReflactKit.findSingeMethodExactly(SerializerTest.class, "complexMethod2");
        testReq(method);
    }

    private static void testReq(Method method) throws IOException {
        KryoBodySerializer4Request serializer4Request = new KryoBodySerializer4Request();

        ByteBuf buf = Unpooled.buffer();
        ByteBufOutputStream byteOutputStream = new ByteBufOutputStream(buf);

        RpcRequest requestBody = new RpcRequest();
        fillRequest(requestBody,method);
        printRequest(requestBody);

        serializer4Request.serialBody(byteOutputStream, requestBody);

        ByteBufInputStream byteBufInputStream = new ByteBufInputStream(buf);
        RpcRequest result = (RpcRequest) serializer4Request.deSerialBody(byteBufInputStream);
        printRequest(result);
    }

    private static void printRequest(RpcRequest result) {
        System.out.println(result.toString());
    }

    private static void fillRequest(RpcRequest req, Method m) {
        req.setMethodName(m.getName());
        req.setUri("uri");
        req.setGenericTypes(m.getGenericParameterTypes());
        req.setArguments(new String[]{"a","b","c"});
    }


    public  String  simpleMethod(int a){
        return "a";
    }

    public Map<String,String> complexMethod(String s, List<String> list){
        return null;
    }
    public Map<String,String> complexMethod2(String s, Map<String,String> list){
        return null;
    }

}
