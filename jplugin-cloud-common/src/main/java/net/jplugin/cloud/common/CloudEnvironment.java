package net.jplugin.cloud.common;


import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.FileKit;
import net.jplugin.common.kits.PropertiesKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.kernel.api.PluginEnvirement;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 云服务环境的配置信息。云服务环境的注册中心、配置中心依赖于nacos。
 */
public class CloudEnvironment {


    public static CloudEnvironment INSTANCE = new CloudEnvironment();
    public static final String NACOS_URL = "nacosUrl";
    public static final String SERVICE_CODE = "serviceCode";
    public static final String APP_CODE = "appCode";
    private static final String RPC_PORT = "rpcPort";
    public static final String NACOS_USER = "nacosUser";
    public static final String NACOS_PWD = "nacosPwd";

    private String nacosUrl;
    private String appCode;
    private String serviceCode;
    private String rpcPort;
    private String nacosUser;
    private String nacosPwd;

    private boolean inited = false;

    private CloudEnvironment() {
    }

    public String getNacosUrl() {
        checkInit();
        return nacosUrl;
    }

    public String getNacosPwd() {
        return nacosPwd;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    public String getNacosUser() {
        return nacosUser;
    }

    public String getAppCode() {
        checkInit();
        return appCode;
    }

    public String getServiceCode() {
        checkInit();
        return serviceCode;
    }

    private void checkInit() {
        if (!inited) {
            throw new RuntimeException("init not called");
        }
    }

    public void init(Map<String, String> map) {
        if (inited)
            PluginEnvirement.getInstance().getStartLogger().log("WARNNING: CloudEnvironment init called a second time，Ignored!");

        AssertKit.assertStringNotNull(map.get(NACOS_URL), NACOS_URL);
        AssertKit.assertStringNotNull(map.get(APP_CODE), APP_CODE);
        AssertKit.assertStringNotNull(map.get(SERVICE_CODE), SERVICE_CODE);
        AssertKit.assertStringNotNull(map.get(RPC_PORT), RPC_PORT);

        nacosUrl = map.get(NACOS_URL).trim();
        appCode = map.get(APP_CODE).trim();
        serviceCode = map.get(SERVICE_CODE).trim();
        rpcPort = map.get(RPC_PORT).trim();

        //user
        String temp = map.get(NACOS_USER);
        if (StringKit.isNotNull(temp))
            nacosUser = temp.trim();
        //pwd
        temp = map.get(NACOS_PWD);
        if (StringKit.isNotNull(temp))
            nacosPwd = temp.trim();

        PluginEnvirement.getInstance().getStartLogger().log("$$$ CloudEnvironment Init: nacosUrl=" + nacosUrl + ", appCode=" + appCode + ", serviceCode=" + serviceCode + " nacosUser=" + nacosUser + " rpcPort=" + rpcPort);
        inited = true;
    }

    private String arrToString(String[] serviceCodes) {
        StringBuffer sb = new StringBuffer();
        for (String s : serviceCodes) {
            sb.append(s).append(",");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put(NACOS_URL, "url1");
        map.put(APP_CODE, "appcode1");
        map.put(SERVICE_CODE, "S1,S2");
        CloudEnvironment.INSTANCE.init(map);

        System.out.println(CloudEnvironment.INSTANCE.getAppCode());
        System.out.println(CloudEnvironment.INSTANCE.getNacosUrl());
        System.out.println(CloudEnvironment.INSTANCE.getServiceCode());

    }

    public void loadFromConfig() {
        String cfgName = PluginEnvirement.getInstance().getConfigDir() + "/jplugin-cloud.properties";
        if (!FileKit.existsAndIsFile(cfgName)) {
            PluginEnvirement.getInstance().getStartLogger().log("$$$ jplugin-cloud.properties not found");
            return;
        } else {
            Properties prop = null;
            try {
                prop = PropertiesKit.loadProperties(cfgName);
            } catch (Exception e) {
                throw new RuntimeException("load jplugin-cloud.properties error");
            }
            Map<String, String> map = propertiesToMap(prop);
            CloudEnvironment.INSTANCE.init(map);

            PluginEnvirement.getInstance().getStartLogger().log("$$$ CloudEnvironment init ok from jplugin-cloud.properties");
        }
    }

    private Map<String,String> propertiesToMap(Properties prop) {
        Map<String,String> map = new HashMap(0);
        for (Map.Entry en:prop.entrySet()){
            map.put((String)en.getKey(),(String)en.getValue());
        }
        return map;
    }

}