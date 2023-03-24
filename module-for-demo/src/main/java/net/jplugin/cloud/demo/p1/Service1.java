package net.jplugin.cloud.demo.p1;

import net.jplugin.core.service.api.BindServiceExport;

@BindServiceExport(path = "/svc1")
public class Service1  implements  IService1{
    @Override
    public String greet(String name){
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        
        return "hello " + name;
    }
}
