package net.jplugin.cloud.rpc.io.api;

import net.jplugin.cloud.rpc.io.client.NettyClient;
import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer.SerializerType;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;
import net.jplugin.core.log.api.LogFactory;
import net.jplugin.core.log.api.Logger;

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
    static Logger logger = LogFactory.getLogger(InvocationContext.class);

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


    /**
     * 下面的内容为了记录日志才引入的
     */
    long startTime;
    public void doStart() {
        this.startTime = System.currentTimeMillis();

        if (logger.isInfoEnabled()){
            StringBuffer sb = new StringBuffer();
            sb.append("$$ InvokeBegin , ctx=").append(getContextString4Begin());
            logger.info(sb.toString());
        }
    }

    public void doSuccess(Object result) {
        if (logger.isInfoEnabled()){
            StringBuffer sb = new StringBuffer();
            sb.append("$$ InvokeSuccess , dural=").append(System.currentTimeMillis()-startTime).append(" , ctx=").append(getContextString()).append(" , resultDataType=").append(getResultType(result));
            logger.info(sb.toString());
        }
    }

    private String getResultType(Object result) {
        if (result==null) return "null";
        else return result.getClass().getName();
    }

    private String getContextString4Begin() {
        StringBuffer sb = new StringBuffer();
        sb.append("").append(this.serviceName).append("/").append(this.methodName).append("|").append(this.serializerType.name());
        sb.append("|").append(getParamString(param));
        return sb.toString();
    }

    private String getContextString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.remoteAddr).append("|").append(this.serviceName).append("/").append(this.methodName).append("|").append(this.serializerType.name());
        sb.append("|").append(getParamString(param));
        return sb.toString();
    }

    private String getParamString(InvocationParam param) {
        StringBuffer sb = new StringBuffer("{");
        if (param==null) return "";
        if (param.getRpcAsync()!=null) sb.append("rpcAsync-").append(param.getRpcAsync()).append(",");
        if (param.getServiceAddress()!=null) sb.append("designatNode-").append(param.getServiceAddress()).append(",");
        sb.append("timeout-").append(param.getServiceTimeOut()).append(",");
        if (param.getRpcCallback()!=null) sb.append(" callback-").append(param.getRpcCallback().getClass().getName()).append(",");
        sb.append("}");

        return sb.toString();
    }

    public void doError(Throwable th) {
        if (logger.isInfoEnabled()){
            StringBuffer sb = new StringBuffer();
            sb.append("$$ InvokeFailed  , dural=").append(System.currentTimeMillis()-startTime).append(" , ctx=").append(getContextString()).append(" , exception=").append(getResultType(th.getMessage()));
            logger.info(sb.toString());
        }
    }

    String remoteAddr;
    public void setCallerClient(NettyClient client) {
        remoteAddr = client.getRemoteAddr();
    }
}
