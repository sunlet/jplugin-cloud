package net.jplugin.cloud.rpc.client.spi;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.kernel.api.BindExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@BindExtension
public class NacosSubscribeService implements IClientSubscribeService {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String USERNAME = "nacos";
    private static final String PASSWORD = "nacos";
    private static final String NAMESPACE = "public";
    
    private final ConcurrentMap<String, List<Instance>> services = new ConcurrentHashMap<>();
    
    private static final List<Instance> EMPTY_LIST = new ArrayList<>(0);
    
    private IServiceNodeChangeListener listener;
    
    @Override
    public void initSubscribCodeList(Set<String> appCodes) {
        if (null != appCodes && !appCodes.isEmpty()) {
            try {
                Properties properties = new Properties();
                properties.put(PropertyKeyConst.USERNAME, USERNAME);
                properties.put(PropertyKeyConst.PASSWORD, PASSWORD);
                properties.put(PropertyKeyConst.SERVER_ADDR, CloudEnvironment.INSTANCE.getNacosUrl());
                properties.put(PropertyKeyConst.NAMESPACE, NAMESPACE);
                final NamingService namingService = NacosFactory.createNamingService(properties);
                for (String appcode : appCodes) {
                    if (log.isInfoEnabled()) {
                        log.info("init subscribe serviceName:{}", appcode);
                    }
                    namingService.subscribe(appcode, new NacosEventListener());
                }
                for (int i = 0; i < 30; i++) {
                    if (check(appCodes)) {
                        return;
                    }
                    Thread.sleep(100);
                }
                throw new RuntimeException("");
            } catch (Exception e) {
                log.error("初始化nacos服务订阅异常", e);
                throw new RuntimeException(e);
            }
        }
    }
    
    private boolean check(Set<String> appCodes) {
        for (String appcode : appCodes) {
            if (null == this.services.get(appcode)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Set<String> getServiceNodesList(String appCode) {
        List<Instance> instances = this.services.get(appCode);
        if (null != instances) {
            return instances.stream().filter(Instance::isHealthy).map(instance -> instance.getIp() + ":" + instance.getPort())
                    .collect(Collectors.toSet());
        }
        throw new IllegalArgumentException("appcode :" + appCode + "is illegal!");
    }
    
    @Override
    public void addServiceNodesChangeListener(IServiceNodeChangeListener listener) {
        this.listener = listener;
    }
    
    class NacosEventListener implements EventListener {
        
        private final Logger log = LoggerFactory.getLogger(NacosEventListener.class);
        
        @Override
        public void onEvent(Event e) {
            NamingEvent event = (NamingEvent) e;
            
            if (log.isInfoEnabled()) {
                if (null == event.getInstances() || event.getInstances().isEmpty()) {
                    log.info("onEvent serviceName:{}, instance is Empty.", event.getServiceName());
                } else {
                    log.info("onEvent serviceName:{}, instance {}.", event.getServiceName(), event.getInstances());
                }
            }
            
            if (null == event.getInstances() || event.getInstances().isEmpty()) {
                services.put(event.getServiceName(), EMPTY_LIST);
                if (null != listener) {
                    listener.changed(event.getServiceName(), conversion(EMPTY_LIST));
                }
            } else {
                services.put(event.getServiceName(), event.getInstances());
                if (null != listener) {
                    listener.changed(event.getServiceName(), conversion(event.getInstances()));
                }
            }
        }
        
        private Set<String> conversion(List<Instance> list) {
            return list.stream().filter(Instance::isHealthy).map(instance -> instance.getIp() + ":" + instance.getPort())
                    .collect(Collectors.toSet());
        }
    }
}
