package net.jplugin.cloud.rpc.client.extension;

import net.jplugin.cloud.rpc.io.spi.AbstractMessageBodySerializer;
import net.jplugin.core.rclient.api.Client;

import java.lang.reflect.Method;

public class RpcJsonClientHandler extends AbstractClientHandler{
    @Override
    public Object invoke(Client client, Object proxy, Method method, Object[] args) throws Throwable {
        return super.invoke(client, proxy, method, args, AbstractMessageBodySerializer.SerializerType.JSON);
    }
}
