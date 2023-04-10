package net.jplugin.cloud.rpc.server;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import net.jplugin.cloud.common.CloudPluginPriority;
import net.jplugin.cloud.rpc.server.imp.RpcServerManager;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.service.api.RefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;


@PluginAnnotation
public class Plugin extends AbstractPlugin {
    
    private static final Logger log = LoggerFactory.getLogger(Plugin.class);

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
//            instance.setEphemeral(false);
//            Map<String, String> instanceMeta = new HashMap<>();
//            instanceMeta.put("site", "et2");
//            instance.setMetadata(instanceMeta);
    
            namingService.registerInstance(CloudEnvironment.INSTANCE._composeAppCode(), instance);

            PluginEnvirement.INSTANCE.getStartLogger().log("服务向Nacos注册完毕:"+CloudEnvironment.INSTANCE._composeAppCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getIp() throws UnknownHostException {
//        return IpKit.getLocalIp();
        String ip = InetAddress.getLocalHost().getHostAddress();
        if (log.isInfoEnabled()) {
            log.info("获取的本机ip:" + ip);
        }
        return ip;
    }
}
