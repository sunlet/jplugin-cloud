package net.jplugin.cloud.rpc.io.api;

import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer.SerializerType;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class InvocationContext {
    InvocationParam param;
    String serviceName;
    String methodName;
    Type[] argsType;
    Object[] args;
    SerializerType serializerType;
    /**
     * 指定调用那个地址
     */
    String designateAddress;

    public SerializerType getSerializerType() {
        return serializerType;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getMethodName() {
        return methodName;
    }

    public InvocationParam getParam() {
        return param;
    }

    /**
     * 如果param为null，则创建一个param。注意：不需要放到线程上下文了！
     * @return
     */
    public InvocationParam getOrInitParam(){
        if (param==null){
            param = InvocationParam.create();
        }
        return param;
    }

    public String getDesignateAddress() {
        return designateAddress;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Type[] getArgsType() {
        return argsType;
    }

    public void setDesignateAddress(String designateAddress) {
        this.designateAddress = designateAddress;
    }



    public static InvocationContext create(String serviceName, Method method, Object[] args, SerializerType st){
        InvocationContext o = new InvocationContext();
        o.param = ClientInvocationManager.INSTANCE.getAndClearParam();
        o.serviceName = serviceName;
        o.methodName = method.getName();
        o.argsType = method.getGenericParameterTypes();
        o.args = args;
        o.serializerType = st;
        return o;
    }

    public static InvocationContext create(String serviceName,String methodName, Object[] args, SerializerType st){
        InvocationContext o = new InvocationContext();
        o.param = ClientInvocationManager.INSTANCE.getAndClearParam();
        o.serviceName = serviceName;
        o.methodName = methodName;
        o.argsType = getTypes(args);
        o.args = args;
        o.serializerType = st;
        return o;
    }

    static Type[] getTypes(Object[] args) {
        Type[] types = new Type[args.length];

        for (int i = 0; i < types.length; i++) {
            AssertKit.assertNotNull(args[i], "arg");
            types[i] = args[i].getClass();
        }
        return types;
    }


}
