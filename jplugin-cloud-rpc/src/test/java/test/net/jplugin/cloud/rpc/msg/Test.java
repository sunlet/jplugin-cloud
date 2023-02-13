package test.net.jplugin.cloud.rpc.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import net.jplugin.cloud.rpc.msg.RpcMessage;

import java.util.HashMap;

public class Test {

    public static class Pojo{
        String name;
        int age;

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
    }


    private static void test4() {

        RpcMessage<Pojo> msg = RpcMessage.create(RpcMessage.TYPE_HEART_BEAT, null, new Pojo());

        System.out.println(msg.toString());

        ByteBuf buf = Unpooled.buffer();

        RpcMessage.serializeWithLength(buf,msg);

        System.out.println(buf.readerIndex());
        System.out.println("len="+buf.readableBytes());

        buf.readerIndex(4);
        RpcMessage obj = RpcMessage.deSerialize(buf);
        System.out.println(obj.toString());

        //print
        buf.readerIndex(0);

        System.out.println();
        while(buf.isReadable()) {
            System.out.print((char) buf.readByte());
            System.out.print(" ");
        }

    }

    private static void test3() {

        HashMap<String,String >  map = new HashMap<>();
        map.put("a", "av");
        map.put("bb", "bbv");
        map.put("cc", "c");

        RpcMessage<Pojo> msg = RpcMessage.create(RpcMessage.TYPE_HEART_BEAT, map, new Pojo());

        System.out.println(msg.toString());

        ByteBuf buf = Unpooled.buffer();

        RpcMessage.serializeWithLength(buf,msg);

        System.out.println(buf.readerIndex());
        System.out.println("len="+buf.readableBytes());

        buf.readerIndex(4);
        RpcMessage obj = RpcMessage.deSerialize(buf);
        System.out.println(obj.toString());

        System.out.println();
        buf.readerIndex(0);
        while(buf.isReadable()) {
            System.out.print((char) buf.readByte());
            System.out.print(" ");
        }
    }

    private static void test2() {

        HashMap<String,String >  map = new HashMap<>();
        map.put("a", "av");
        map.put("bb", "bbv");
        map.put("cc", "c");

        RpcMessage<Pojo> msg = RpcMessage.create(RpcMessage.TYPE_HEART_BEAT, map, null);

        System.out.println(msg.toString());

        ByteBuf buf = Unpooled.buffer();

        RpcMessage.serializeWithLength(buf,msg);

        System.out.println(buf.readerIndex());
        System.out.println("len="+buf.readableBytes());

        buf.readerIndex(4);
        RpcMessage obj = RpcMessage.deSerialize(buf);
        System.out.println(obj.toString());

    }

    private static void test1() {
        RpcMessage<Pojo> msg = RpcMessage.create(RpcMessage.TYPE_HEART_BEAT, null, null);
        System.out.println(msg.toString());

        ByteBuf buf = Unpooled.buffer();

        RpcMessage.serializeWithLength(buf,msg);

        System.out.println(buf.readerIndex());
        System.out.println("len="+buf.readableBytes());

        buf.readerIndex(4);
        RpcMessage obj = RpcMessage.deSerialize(buf);
        System.out.println(obj.toString());

    }

}