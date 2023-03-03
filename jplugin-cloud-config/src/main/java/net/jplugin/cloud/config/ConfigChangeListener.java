package net.jplugin.cloud.config;

import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import net.jplugin.core.config.impl.ConfigureChangeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public final class ConfigChangeListener implements Listener {
    
    private static final Logger log = LoggerFactory.getLogger(ConfigChangeListener.class);
    private final String groupid;
    
    public ConfigChangeListener(String groupid) {
        this.groupid = groupid;
    }
    
    @Override
    public Executor getExecutor() {
        return null;
    }
    
    @Override
    public void receiveConfigInfo(String configInfo) {
        if (log.isDebugEnabled()) {
            log.debug("配置发生变更，groupid = {}", this.groupid);
            log.debug("变更后值为:{}", configInfo);
        }
        NacosConfigProvidor.me().updateConfig(this.groupid, configInfo);
        ConfigureChangeManager.instance.fireConfigChange(Lists.newArrayList(this.groupid));
    }
}
