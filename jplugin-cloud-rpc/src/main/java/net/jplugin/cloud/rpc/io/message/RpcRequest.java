package net.jplugin.cloud.rpc.io.message;

import java.lang.reflect.Type;

public class RpcRequest {
    String uri;
    String methodName;
    Object[] arguments;
    Type[] genericTypes;

    public Type[] getGenericTypes() {
        return genericTypes;
    }

    public void setGenericTypes(Type[] genericTypes) {
        this.genericTypes = genericTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUri() {
        return uri;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
