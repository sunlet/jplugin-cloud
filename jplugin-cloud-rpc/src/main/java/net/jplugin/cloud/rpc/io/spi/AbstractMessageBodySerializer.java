package net.jplugin.cloud.rpc.io.spi;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.core.kernel.api.BindExtensionPoint;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.kernel.api.PointType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@BindExtensionPoint(type = PointType.LIST)
public abstract class AbstractMessageBodySerializer {
    public enum SerializerType{
        JSON,KRYO
    }

    public abstract SerializerType serialType();
    public abstract String bodyClass();

    public abstract Object deSerialBody(ByteBufInputStream input) throws IOException;

    public abstract void serialBody(ByteBufOutputStream byteOutputStream, Object body) throws IOException;

    static Map<SerializerType, Map<String, AbstractMessageBodySerializer>>  serializerTypeMapMap = null;
    public static AbstractMessageBodySerializer getSerializer(SerializerType st, String clazz){
        AbstractMessageBodySerializer temp = serializerTypeMapMap.get(st).get(clazz);

        if (temp==null)
            throw new RuntimeException("can't find serial type for: "+st + " "+clazz);

        return temp;
    }

    public static void init(){
        serializerTypeMapMap = createMap();
    }

    private static Map<SerializerType, Map<String, AbstractMessageBodySerializer>> createMap() {
        Map<SerializerType, Map<String, AbstractMessageBodySerializer>> map = new HashMap<>();

        AbstractMessageBodySerializer[] extensions = PluginEnvirement.INSTANCE.getExtensionObjects(AbstractMessageBodySerializer.class.getName(), AbstractMessageBodySerializer.class);
        for (int i=0;i<extensions.length;i++){
            AbstractMessageBodySerializer o = extensions[i];
            if (map.get(o.serialType())==null){
                map.put(o.serialType(), (Map<String, AbstractMessageBodySerializer>) new HashMap(3));
            }

            map.get(o.serialType()).put(o.bodyClass(), o);
        }

        return map;
    }
}
