package net.jplugin.cloud.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import net.jplugin.cloud.common.CloudEnvironment;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.IConfigProvidor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于nacos的配置器
 * @author peiyu
 */
public final class NacosConfigProvidor implements IConfigProvidor {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ConfigService                 configService;
    private final String                        appcode;
    private final String                        serviceCode;
    private final ConcurrentMap<String, Properties> propertiesCache;
    private final ConcurrentMap<String, String> cache;
    private final ConfigProcessor processor;
    
    public static NacosConfigProvidor me() {
        return NacosConfigProvidorHandler.ME;
    }
    
    private NacosConfigProvidor() {
        this.appcode = CloudEnvironment.INSTANCE.getAppCode();
        this.serviceCode = CloudEnvironment.INSTANCE.getServiceCode();
        this.propertiesCache = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
        this.processor = new ConfigProcessor();
        try {
            //init nacos config
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.USERNAME, CloudEnvironment.INSTANCE.getNacosUser());
            properties.put(PropertyKeyConst.PASSWORD, CloudEnvironment.INSTANCE.getNacosPwd());
            properties.put(PropertyKeyConst.SERVER_ADDR, CloudEnvironment.INSTANCE.getNacosUrl());
            this.configService = NacosFactory.createConfigService(properties);
            initConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initConfig() {
        try {
            //模拟登录
            this.processor.login();
            //获取公共配置
            Tuple2<Map<String, Properties>, Map<String, String>> publicConfigData = this.processor.initConifgData("");
            //获取本项目的配置
            Tuple2<Map<String, Properties>, Map<String, String>> myConfigData = this.processor.initConifgData(CloudEnvironment.INSTANCE.getAppCode());
            this.propertiesCache.putAll(publicConfigData.first);
            this.propertiesCache.putAll(myConfigData.first);
            this.cache.putAll(publicConfigData.second);
            this.cache.putAll(myConfigData.second);
    
            this.propertiesCache.keySet().forEach(k -> {
                try {
                    this.configService.addListener(CloudEnvironment.INSTANCE.getServiceCode(), k, new ConfigChangeListener(k));
                } catch (NacosException e) {
                    log.error("增加监听器异常", e);
                    throw new RuntimeException(e);
                }
            });
            this.cache.keySet().forEach(k -> {
                try {
                    this.configService.addListener(CloudEnvironment.INSTANCE.getServiceCode(), k, new ConfigChangeListener(k));
                } catch (NacosException e) {
                    log.error("增加监听器异常", e);
                    throw new RuntimeException(e);
                }
            });
            
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
            return this.cache.get(key);
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
            return this.cache.containsKey(key);
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
        Set<String> result = new HashSet<>();
        result.addAll(this.propertiesCache.keySet());
        result.addAll(this.cache.keySet());
        return result;
    }
    
    public void updateConfig(String groupId, String data) {
        try {
            if (this.propertiesCache.containsKey(groupId)) {
                Properties properties = new Properties();
                properties.load(new StringReader(data));
                this.propertiesCache.put(groupId, properties);
            } else {
                this.cache.put(groupId, data);
            }
        } catch (Exception e) {
            log.error("更新配置异常，配置组:" + groupId, e);
        }
    }
    
    
    private static final class NacosConfigProvidorHandler {
        private static final NacosConfigProvidor ME = new NacosConfigProvidor();
    }
}
