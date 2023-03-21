package net.jplugin.cloud.demo.p1;

import net.jplugin.cloud.rpc.client.annotation.BindRemoteService;
import net.jplugin.cloud.rpc.client.annotation.Protocol;

@BindRemoteService(url = "esf://app1:servicecode1/svc1" ,protocol = Protocol.rpc_json)
public interface IService1 {
    public String greet(String name);

    public String toString();
}
