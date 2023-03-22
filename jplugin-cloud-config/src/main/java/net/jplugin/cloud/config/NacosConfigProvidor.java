package net.jplugin.cloud.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.CloudEnvironment;
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
 * 基于nacos的应用级配置器
 *
 * @author peiyu
 */
public final class NacosConfigProvidor implements IConfigProvidor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String APP_CONFIG = "APP-CONFIG";

    private final ConfigProcessor processor = ConfigProcessor.me();

    private final ConfigService configService;

    private final String appcode;

    private final String serviceCode;

    private final ConcurrentMap<String, Properties> appPropertiesCache;

    private final ConcurrentMap<String, String> appCache;

    private final ConcurrentMap<String, Properties> propertiesCache;

    private final ConcurrentMap<String, String> cache;


    public static NacosConfigProvidor me() {
        return NacosConfigProvidorHandler.ME;
    }

    private NacosConfigProvidor() {
        this.appcode = CloudEnvironment.INSTANCE.getAppCode();
        this.serviceCode = CloudEnvironment.INSTANCE.getModuleCode();
        this.propertiesCache = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
        this.appPropertiesCache = new ConcurrentHashMap<>();
        this.appCache = new ConcurrentHashMap<>();
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
            //            //模拟登录
            //            this.processor.login();

            //获取本服务的配置
            Tuple2<Map<String, Properties>, Map<String, String>> myConfigData = this.processor.initConifgData(
                    CloudEnvironment.INSTANCE.getAppCode(), CloudEnvironment.INSTANCE.getModuleCode());
            //获取appcode级配置
            Tuple2<Map<String, Properties>, Map<String, String>> appConfigData = this.processor.initConifgData(
                    CloudEnvironment.INSTANCE.getAppCode(), APP_CONFIG);

            //存入配置
            this.appPropertiesCache.putAll(appConfigData.first);
            this.appCache.putAll(appConfigData.second);
            this.propertiesCache.putAll(myConfigData.first);
            this.cache.putAll(myConfigData.second);

            //增加服务级配置监听
            this.propertiesCache.keySet().forEach(k -> {
                try {
                    this.configService.addListener(CloudEnvironment.INSTANCE.getModuleCode(), k,
                            new ConfigChangeListener(CloudEnvironment.INSTANCE.getModuleCode(), k));
                } catch (NacosException e) {
                    log.error("增加监听器异常", e);
                    throw new RuntimeException(e);
                }
            });
            this.cache.keySet().forEach(k -> {
                try {
                    this.configService.addListener(CloudEnvironment.INSTANCE.getModuleCode(), k,
                            new ConfigChangeListener(CloudEnvironment.INSTANCE.getModuleCode(), k));
                } catch (NacosException e) {
                    log.error("增加监听器异常", e);
                    throw new RuntimeException(e);
                }
            });

            //增加应用级配置监听
            this.appPropertiesCache.keySet().forEach(k -> {
                try {
                    this.configService.addListener(APP_CONFIG, k,
                            new ConfigChangeListener(APP_CONFIG, k));
                } catch (NacosException e) {
                    log.error("增加监听器异常", e);
                    throw new RuntimeException(e);
                }
            });
            this.appCache.keySet().forEach(k -> {
                try {
                    this.configService.addListener(APP_CONFIG, k,
                            new ConfigChangeListener(APP_CONFIG, k));
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
        //服务级配置优先 start
        Properties properties = this.propertiesCache.get(group);
        if (null != properties) {
            String subKey = StringUtils.substringAfter(key, ".");
            return (String) properties.get(subKey);
        }
        String value = this.cache.get(group);
        if (null != value) {
            return value;
        }
        //服务级配置优先 end

        //应用级
        Properties appProperties = this.appPropertiesCache.get(group);
        if (null != appProperties) {
            String subKey = StringUtils.substringAfter(key, ".");
            return (String) appProperties.get(subKey);
        }
        return this.appCache.get(group);
    }

    @Override
    public boolean containsConfig(String key) {
        String group = StringUtils.substringBefore(key, ".");
        //服务级配置优先 start
        Properties properties = this.propertiesCache.get(group);
        if (null != properties) {
            String subKey = StringUtils.substringAfter(key, ".");
            return properties.containsKey(subKey);
        }
        boolean containsKey = this.cache.containsKey(group);
        if (containsKey) {
            return true;
        }
        //服务级配置优先 end

        //应用级
        Properties appProperties = this.appPropertiesCache.get(group);
        if (null != appProperties) {
            String subKey = StringUtils.substringAfter(key, ".");
            return appProperties.containsKey(subKey);
        }
        return this.appCache.containsKey(group);
    }

    @Override
    public Map<String, String> getStringConfigInGroup(String key) {
        String group = StringUtils.substringBefore(key, ".");
        //服务级配置优先 start
        Properties properties = this.propertiesCache.get(group);
        if (null != properties) {
            return Maps.fromProperties(properties);
        }
        //服务级配置优先 end

        //应用级
        Properties appProperties = this.appPropertiesCache.get(group);
        if (null != appProperties) {
            return Maps.fromProperties(appProperties);
        }
        return null;
    }

    @Override
    public Set<String> getGroups() {
        Set<String> result = new HashSet<>();
        result.addAll(this.appPropertiesCache.keySet());
        result.addAll(this.appCache.keySet());
        result.addAll(this.propertiesCache.keySet());
        result.addAll(this.cache.keySet());
        return result;
    }

    public void updateConfig(String dataId, String groupId, String data) {
        try {
            if (dataId.equals(APP_CONFIG)) {
                if (this.appPropertiesCache.containsKey(groupId)) {
                    Properties properties = new Properties();
                    properties.load(new StringReader(data));
                    this.appPropertiesCache.put(groupId, properties);
                } else {
                    this.appCache.put(groupId, data);
                }
            } else {
                if (this.propertiesCache.containsKey(groupId)) {
                    Properties properties = new Properties();
                    properties.load(new StringReader(data));
                    this.propertiesCache.put(groupId, properties);
                } else {
                    this.cache.put(groupId, data);
                }
            }
        } catch (Exception e) {
            log.error("更新配置异常，dataId：" + dataId + "，配置组:" + groupId, e);
        }
    }


    private static final class NacosConfigProvidorHandler {
        private static final NacosConfigProvidor ME = new NacosConfigProvidor();
    }
}
