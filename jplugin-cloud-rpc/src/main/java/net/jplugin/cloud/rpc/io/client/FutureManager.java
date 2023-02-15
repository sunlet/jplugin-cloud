package net.jplugin.cloud.rpc.io.client;

import net.jplugin.cloud.rpc.io.future.CallFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FutureManager {
    private final Map<String, CallFuture<?>> futureMap = new ConcurrentHashMap<String, CallFuture<?>>();

    public void addFuture(String reqId,CallFuture f){
        this.futureMap.put(reqId,f);
    }

    public CallFuture<?> removeFuture(String reqId){
        return this.futureMap.remove(reqId);
    }
}
