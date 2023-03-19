package net.jplugin.cloud.rpc.client.api;

public class InvokeResult {
    Object result;
    Throwable exception;
    boolean success;

    public InvokeResult(Object ret,Throwable exc,boolean aSuccess){
        this.result = ret;
        this.exception = exc;
        this.success = aSuccess;
    }

    public Object getResult() {
        return result;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isSuccess() {
        return success;
    }
}
