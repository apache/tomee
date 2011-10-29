package org.superbiz.dynamic;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * @author rmannibucau
 */
public class SocialInterceptor {
    @AroundInvoke
    public Object around(InvocationContext context) throws Exception {
        String mtd = context.getMethod().getName();
        String address;
        if (mtd.toLowerCase().contains("facebook")) {
            address = "http://www.facebook.com";
        } else if (mtd.toLowerCase().contains("twitter")) {
            address = "http://twitter.com";
        } else {
            address = "no website for you";
        }

        System.out.println("go on " + address);
        return context.proceed();
    }
}
