package net.jplugin.cloud.rpc.io.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.cloud.rpc.io.spi.IMessageBodySerializer;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.kernel.api.PluginEnvirement;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * <pre>
 * 整体格式序列化格式：长度 + 类型 + Header长度 + Header + Body
 * 说明如下：
 *      长度为除了“长度”所占字节之外的后续所有长度
 *      Body长度没有存储，因为不需要：如果需要也可以计算出来 Body长度= 长度-Header长度-4
 *      最短长度为 8 （header和body都没有）
 *
 *      如果header中包含了key：HEADER_SERIAL_TYPE，则Body用指定的序列化方式。否则：默认用json序列化。(暂未实现）
 * </pre>
 */
public final class RpcMessage<T> {

    public final static short TYPE_CLIENT_INFO = (short) 0X0001;
    public final static short TYPE_SERVER_INFO = (short) 0X0002;
    public final static short TYPE_CLIENT_REQ = (short) 0x0003;
    public final static short TYPE_SERVER_RES = (short) 0x0004;
    public final static short TYPE_CLIENT_HEART_BEAT = (short) 0x000E;
    public final static short TYPE_SERVER_HEART_BEAT = (short) 0x000F;

    public final static String HEADER_CLIENT_APPCODE = "client-app-code";
    public final static String HEADER_CLIENT_SERVICECODE = "client-service-code";
    public final static String HEADER_SERIAL_TYPE = "serial-type";
    public final static String HEADER_REQ_ID="req-id";
    public static final String HEADER_TENANT_ID = "tenant-id";
    public static final String HEADER_GLOBAL_REQ_ID ="g-req-id" ;

    private static final String DEFAULT_SERIALIZER_HANDLER = "json";



    private short msgType;
    private Hashtable<String,String> header;
    private  T body;

    public short getMsgType() {
        return msgType;
    }

    public Map<String,String> getHeader() {
        return header;
    }

    public T getBody() {
        return body;
    }

    protected RpcMessage<T> type(short msgType) {
        this.msgType = msgType;
        return this;
    }

    public RpcMessage<T> header(String key, String val){
        if (this.header==null) this.header = new Hashtable<>();
        this.header.put(key,val);
        return this;
    }

    public RpcMessage<T> headers(Map<String,String> map){
        if (map==null)
            return this;
        if (this.header==null) this.header = new Hashtable<>();
        header.putAll(map);
        return this;
    }

    public RpcMessage<T> body(T body){
        this.body = body;
        return this;
    }


    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("type=").append(msgType).append(" ");
        if (this.header!=null){
            sb.append("\nheaders:");
            for (Map.Entry en:header.entrySet()){
                sb.append(en.getKey()).append("=").append(en.getValue()).append("   ");
            }
        }else sb.append("\nheaders: null");

        sb.append("\nbody=").append(body==null? "null":JsonKit.object2JsonEx(this.body));
        return sb.toString();
    }

    private RpcMessage(){}

    public static RpcMessage create(Short type,Map<String,String> header,Object body){
        RpcMessage o = new RpcMessage();
        return o.type(type).headers(header).body(body);
    }

    public static RpcMessage create(Short type){
        return new RpcMessage().type(type);
    }




    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
    private static final byte[] HEADER_LEN_PLACEHOLDER = new byte[2];

    public static RpcMessage deSerialize(ByteBuf byteBuf){
        try {
            //不包含長度字段了
            ByteBufInputStream input = new ByteBufInputStream(byteBuf);
            RpcMessage msg = new RpcMessage();

            //读取类型
            msg.type(input.readShort());
            short headLen = input.readShort();

            int headStart = byteBuf.readerIndex();
            //如果有header
            if (headLen > 0) {
                msg.headers(new HashMap<>());
                while (byteBuf.readerIndex() - headStart < headLen) {
                    msg.getHeader().put(input.readUTF(), input.readUTF());
                }
            }

            //如果有body
            if (input.available() > 0) {
                msg.body(deSerialBody(input,msg.getHeader()));
            }

            //确定结束了
            AssertKit.assertEqual(input.available(), 0);
            return msg;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void serializeWithLength(ByteBuf byteBuf,RpcMessage msg) {
        int startIdx = byteBuf.writerIndex();

        //强调：看了源代码  ByteBufOutputStream 不需要flush,每一次修改都会写到ByteBuf
        ByteBufOutputStream byteOutputStream = new ByteBufOutputStream(byteBuf);
        try {
            //4 byte
            byteOutputStream.write(LENGTH_PLACEHOLDER);
            //2 byte
            byteOutputStream.writeShort(msg.msgType);

            //headerStartIndex
            int headerStartIndex = byteBuf.writerIndex();
            //2 byte
            byteOutputStream.write(HEADER_LEN_PLACEHOLDER);

            // write header
            Map<String,String> header = msg.getHeader();
            if (msg.getHeader()!=null) {
                for (Map.Entry<String, String> en:header.entrySet()){
                    byteOutputStream.writeUTF(en.getKey());
                    byteOutputStream.writeUTF(en.getValue());
                }
            }

            //get headEndIdx
            int headerEndIdx = byteBuf.writerIndex();

            //write body
            Object body = msg.getBody();
            if (msg.getBody()!=null){
                //分别写类名和JSON序列化内容
                serialBody(byteOutputStream, body,msg.getHeader());
            }

            //get endIdx
            int endIdx = byteBuf.writerIndex();

            //总长度.
            byteBuf.setInt(startIdx, endIdx - startIdx - 4) ;
            //HEAD长度.
            byteBuf.setShort(headerStartIndex, headerEndIdx - headerStartIndex  - 2 );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object deSerialBody(ByteBufInputStream input ,Map<String,String> header) throws ClassNotFoundException, IOException {
        IMessageBodySerializer serializer = getMessageBodySerializer(header);
        return serializer.deSerialBody(input);
    }

    private static void serialBody(ByteBufOutputStream byteOutputStream, Object body,Map<String,String> header) throws IOException {
        IMessageBodySerializer serializer = getMessageBodySerializer(header);
        serializer.serialBody(byteOutputStream, body);
    }

    private static IMessageBodySerializer getMessageBodySerializer(Map<String, String> header) {
        String serialHandler = DEFAULT_SERIALIZER_HANDLER;
        if (header!=null){
            String temp = header.get(HEADER_SERIAL_TYPE);
            if (StringKit.isNotNull(temp)){
                serialHandler = temp;
            }
        }
        return (IMessageBodySerializer) PluginEnvirement.INSTANCE.getExtensionMap(IMessageBodySerializer.class.getName()).get(serialHandler);
    }
}
