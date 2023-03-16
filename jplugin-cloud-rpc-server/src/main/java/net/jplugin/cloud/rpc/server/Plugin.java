package net.jplugin.cloud.rpc.server;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import net.jplugin.cloud.common.CloudEnvironment;
import net.jplugin.cloud.common.CloudPluginPriority;
import net.jplugin.cloud.rpc.server.imp.RpcServerManager;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.service.api.BindService;
import net.jplugin.core.service.api.RefService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@PluginAnnotation
public class Plugin extends AbstractPlugin {

    @RefService
    RpcServerManager server;

    @Override
    public int getPrivority() {
        return CloudPluginPriority.SERVER;
    }

    @Override
    public void init() {
        server.start();
        registerService();
    }
    
    private void registerService() {
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.USERNAME, CloudEnvironment.INSTANCE.getNacosUser());
            properties.put(PropertyKeyConst.PASSWORD, CloudEnvironment.INSTANCE.getNacosPwd());
            properties.put(PropertyKeyConst.SERVER_ADDR, CloudEnvironment.INSTANCE.getNacosUrl());
            properties.put(PropertyKeyConst.NAMESPACE, CloudEnvironment.INSTANCE.getAppCode());
            final NamingService namingService = NacosFactory.createNamingService(properties);
            Instance instance = new Instance();
            instance.setIp(getIp());
            instance.setPort(Integer.parseInt(CloudEnvironment.INSTANCE.getRpcPort()));
            instance.setHealthy(true);
            instance.setWeight(1.0);
            instance.setEphemeral(false);
//            Map<String, String> instanceMeta = new HashMap<>();
//            instanceMeta.put("site", "et2");
//            instance.setMetadata(instanceMeta);
    
            namingService.registerInstance(CloudEnvironment.INSTANCE._composeAppCode(), instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getIp() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }
}
