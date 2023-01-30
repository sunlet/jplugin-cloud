package net.jplugin.cloud.demo;

import net.jplugin.cloud.common.CloudEnvironment;
import net.jplugin.core.kernel.PluginApp;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<String,String> p = new HashMap<>();
        p.put(CloudEnvironment.NACOS_URL,"172.21.15.10");
        p.put(CloudEnvironment.NACOS_URL,"");

//        CloudEnvironment.INSTANCE.init(p);

        PluginApp.main(null);
    }
}
