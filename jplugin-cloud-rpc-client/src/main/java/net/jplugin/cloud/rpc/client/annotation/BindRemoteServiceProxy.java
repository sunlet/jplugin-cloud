package net.jplugin.cloud.rpc.client.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface BindRemoteServiceProxy {

    public String url();

    public ProxyProtocol protocol();

    public enum ProxyProtocol {
//        rest,
        rpc
//        , rpc_json;
    }

}
