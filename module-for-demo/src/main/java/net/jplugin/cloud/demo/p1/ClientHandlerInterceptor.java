package net.jplugin.cloud.demo.p1;

import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.core.kernel.api.AbstractExtensionInterceptor;
import net.jplugin.core.kernel.api.BindExtensionInterceptor;
import net.jplugin.core.kernel.api.ExtensionInterceptorContext;
import net.jplugin.core.rclient.Plugin;

//@BindExtensionInterceptor(forExtensionPoints = Plugin.EP_CLIENT_HANDLER)
//public class ClientHandlerInterceptor extends AbstractExtensionInterceptor {
//    @Override
//    protected Object execute(FilterChain fc, ExtensionInterceptorContext ctx) throws Throwable {
//        System.out.println("begin calling ..............." + ctx.getExtensionPointName() +" "+ctx.getArgs());
//
//        Object[] args = ctx.getArgs();
//
//
//        System.out.println(args);
//
//        return super.execute(fc, ctx);
//    }
//}
