package net.jplugin.cloud.rpc.io.message;

import java.lang.reflect.Type;

public class RpcResponse {
    public static final String DEFAULT_ERROR_CODE = "0";
    String errorCode;
    String message;
    Object result;
    Type resultType;

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Type getResultType() {
        return resultType;
    }

    public void setResultType(Type resultType) {
        this.resultType = resultType;
    }

    public Object getResult() {
        return result;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
