package net.jplugin.cloud.config;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.client.auth.impl.NacosAuthLoginConstant;
import com.alibaba.nacos.client.auth.impl.NacosClientAuthServiceImpl;
import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.client.request.JdkHttpClientRequest;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.spi.client.ClientAuthService;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.CloudEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.client.naming.utils.UtilAndComs.webContext;

public final class ConfigProcessor {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final NacosRestTemplate template = new NacosRestTemplate(log, new JdkHttpClientRequest(
            HttpClientConfig.builder().setConTimeOutMillis(3000).setReadTimeOutMillis(3000).build()));
    
    private static final String CONFIG_URL = "/v1/cs/configs";
    
    private volatile String token;
    
    private ConfigProcessor() {
        login();
    }
    
    public static ConfigProcessor me() {
        return ConfigProcessorHolder.ME;
    }
    
    
    private String login() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.USERNAME, CloudEnvironment.INSTANCE.getNacosUser());
        properties.put(PropertyKeyConst.PASSWORD, CloudEnvironment.INSTANCE.getNacosPwd());
        
        ClientAuthService authService = new NacosClientAuthServiceImpl();
        authService.setServerList(Lists.newArrayList(CloudEnvironment.INSTANCE.getNacosUrl()));
        authService.setNacosRestTemplate(this.template);
        
        if (authService.login(properties)) {
            LoginIdentityContext loginIdentityContext = authService.getLoginIdentityContext(null);
            this.token = loginIdentityContext.getParameter(NacosAuthLoginConstant.ACCESSTOKEN);
        } else {
            throw new RuntimeException("登录nacos失败...");
        }
        return this.token;
    }
    
    public Tuple2<Map<String, Properties>, Map<String, String>> initConifgData(String tenant, String dataId, String group) throws Exception {
        Map<String, Properties> propertiesMap = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        
        String url = "http://" + CloudEnvironment.INSTANCE.getNacosUrl() + webContext + CONFIG_URL;
        url += "?pageNo=1";
        url += "&pageSize=100";
        url += "&search=accurate";
        if (null != dataId) {
            url += "&dataId=" + dataId;
        } else {
            url += "&dataId=";
        }
        if (null != group) {
            url += "&group=" + group;
        } else {
            url += "&group=";
        }
        url += "&tenant=" + tenant;
        url += "&accessToken=" + this.token;//登录token
        url += "&username=" + CloudEnvironment.INSTANCE.getNacosUser();
        
        HttpRestResult<String> restResult = template.get(url, Header.EMPTY,
                Query.EMPTY, String.class);
        if (!restResult.ok()) {
            log.error("login failed: {}", JacksonUtils.toJson(restResult));
            throw new RuntimeException("获取配置接口调用异常：" + restResult.getMessage());
        }
        JsonNode obj = JacksonUtils.toObj(restResult.getData());
        ArrayNode data = (ArrayNode) obj.get("pageItems");
        Iterator<JsonNode> elements = data.elements();
        while (elements.hasNext()) {
            JsonNode node = elements.next();
            String content = node.get("content").asText();
            String g = node.get("group").asText();
            String type = node.get("type").asText();
            if (ConfigType.PROPERTIES.getType().equals(type)) {
                Properties properties = new Properties();
                properties.load(new StringReader(content));
                propertiesMap.put(g, properties);
            } else {
                map.put(g, content);
            }
        }
        return Tuple2.with(propertiesMap, map);
    }
    
    public Tuple2<Map<String, Properties>, Map<String, String>> initConifgData(String tenant, String dataId) throws Exception {
        return initConifgData(tenant, dataId, null);
    }
    
    public Tuple2<Map<String, Properties>, Map<String, String>> initConifgData(String tenant) throws Exception {
        return initConifgData(tenant, null);
    }
    
    
    private static final class ConfigProcessorHolder {
        private static final ConfigProcessor ME = new ConfigProcessor();
    }
    
}
