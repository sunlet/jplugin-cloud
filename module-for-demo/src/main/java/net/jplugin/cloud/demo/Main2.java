package net.jplugin.cloud.demo;

import net.jplugin.core.kernel.PluginApp;

public class Main2 {
    public static void main(String[] args) throws InterruptedException {
//        HashMap<String,String> p = new HashMap<>();
//        p.put(CloudEnvironment.NACOS_URL,"172.21.15.10");
//        p.put(CloudEnvironment.NACOS_URL,"");

//        CloudEnvironment.INSTANCE.init(p);

        PluginApp.main(null);

        Thread.currentThread().sleep(1000000);
    }
}
