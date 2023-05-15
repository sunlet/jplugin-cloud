package net.jplugin.cloud.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.config.api.IConfigProvidor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于nacos的全局配置器
 *
 * @author peiyu
 */
public final class NacosGlobalConfigProvidor implements IConfigProvidor {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String GLOBAL_CONFIG = "GLOBAL-CONFIG";
    
    private ConfigProcessor processor = ConfigProcessor.me();
    
    private final ConfigService configService;
    
    private final ConcurrentMap<String, Properties> propertiesCache;
    
    
    public static NacosGlobalConfigProvidor me() {
        return NacosGlobalConfigProvidorHolder.ME;
    }
    
    private NacosGlobalConfigProvidor() {
        this.propertiesCache = new ConcurrentHashMap<>();
        try {
            //init nacos config
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.USERNAME, CloudEnvironment.INSTANCE.getNacosUser());
            properties.put(PropertyKeyConst.PASSWORD, CloudEnvironment.INSTANCE.getNacosPwd());
            properties.put(PropertyKeyConst.SERVER_ADDR, CloudEnvironment.INSTANCE.getNacosUrl());
            properties.put(PropertyKeyConst.NAMESPACE, CloudEnvironment.INSTANCE.getAppCode());
            this.configService = NacosFactory.createConfigService(properties);
            initConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void initConfig() {
        try {
            //获取公共配置
            Tuple2<Map<String, Properties>, Map<String, String>> publicConfigData = this.processor.initConifgData("", GLOBAL_CONFIG);
            this.propertiesCache.putAll(publicConfigData.first);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public String getConfigValue(String key) {
        String group = StringUtils.substringBefore(key, ".");
        Properties properties = this.propertiesCache.get(group);
        if (null != properties) {
            String subKey = StringUtils.substringAfter(key, ".");
            return (String) properties.get(subKey);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean containsConfig(String key) {
        String group = StringUtils.substringBefore(key, ".");
        Properties properties = this.propertiesCache.get(group);
        if (null != properties) {
            String subKey = StringUtils.substringAfter(key, ".");
            return properties.containsKey(subKey);
        } else {
            return false;
        }
    }
    
    @Override
    public Map<String, String> getStringConfigInGroup(String key) {
        String group = StringUtils.substringBefore(key, ".");
        Properties properties = this.propertiesCache.get(group);
        if (null != properties) {
            return Maps.fromProperties(properties);
        }
        return null;
    }
    
    @Override
    public Set<String> getGroups() {
        return new HashSet<>(this.propertiesCache.keySet());
    }
    
    private static final class NacosGlobalConfigProvidorHolder {
        
        private static final NacosGlobalConfigProvidor ME = new NacosGlobalConfigProvidor();
    }
}
