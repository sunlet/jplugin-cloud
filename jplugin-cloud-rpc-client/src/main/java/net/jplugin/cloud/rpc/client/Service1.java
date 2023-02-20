package net.jplugin.cloud.rpc.client;

import net.jplugin.core.service.api.BindServiceExport;

@BindServiceExport(path = "/svc1")
public class Service1  {
    public String greet(String name){
        return "hello " + name;
    }
}
