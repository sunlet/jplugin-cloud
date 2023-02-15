package net.jplugin.cloud.rpc.io.handler;

import net.jplugin.cloud.rpc.common.constant.ReportMonitorConstant;
import net.jplugin.common.kits.ReflactKit;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class Util {
    static ConcurrentHashMap<String,Method> methodMap=new ConcurrentHashMap<>();
    public static Method getMethod(Class clazz,String name){
        String key = clazz.getName()+'#'+name;
        Method method = methodMap.get(key);
        if (method==null){
            method = ReflactKit.findSingeMethodExactly(clazz,name);
            if (method!=null) {
                methodMap.put(key, method);
            }
        }
        return method;
    }

     /**
     * 拼接URL
     *
     * @param serviceName
     * @param methodName
     * @param args
     * @return String 返回拼接内容格式：服务名/方法名/参数1类型/参数2类型/.../参数N类型
     */
    public static String convertURL(String serviceName, String methodName, Object[] args) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(serviceName).append(ReportMonitorConstant.SPLICE_CODE).append(methodName);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                buffer.append(ReportMonitorConstant.SPLICE_CODE);
                if (args[i] == null) {
                    buffer.append(ReportMonitorConstant.NULL_CODE);
                    continue;
                }
                buffer.append(args[i].getClass().getName());
            }
        }
        return buffer.toString();
    }
}
