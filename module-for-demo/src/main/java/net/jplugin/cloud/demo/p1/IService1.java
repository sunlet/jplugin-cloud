package net.jplugin.cloud.demo.p1;

import net.jplugin.cloud.rpc.client.annotation.BindRemoteServiceProxy;

@BindRemoteServiceProxy(url = "esf://app1/svc1" ,protocol = BindRemoteServiceProxy.ProxyProtocol.rpc_json)
public interface IService1  {
    public String greet(String name);
}
