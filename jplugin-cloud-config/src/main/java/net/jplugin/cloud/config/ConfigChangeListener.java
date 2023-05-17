package net.jplugin.cloud.config;

import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import net.jplugin.core.config.impl.ConfigureChangeManager;
import net.jplugin.core.kernel.api.PluginEnvirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public final class ConfigChangeListener implements Listener {
    
    private static final Logger log = LoggerFactory.getLogger(ConfigChangeListener.class);
    private final String dataId;
    private final String groupid;
    
    public ConfigChangeListener(String dataId, String groupid) {
        this.dataId = dataId;
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
        NacosConfigProvidor.me().updateConfig(this.dataId, this.groupid, configInfo);
        //由于nacos刚启动时也会触发这个监听，框架可能还没启动完成，所以调整为只有整个工程启动完成后，Nacos触发监听事件，才会专递给热更新模块
        if (PluginEnvirement.INSTANCE.getStateLevel() == PluginEnvirement.STAT_LEVEL_WORKING) {
            ConfigureChangeManager.instance.fireConfigChange(Lists.newArrayList(this.groupid));
        }
        
    }
}
