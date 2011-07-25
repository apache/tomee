package org.apache.openejb.rest;

import javax.ws.rs.core.Application;

/**
 * @author Romain Manni-Bucau
 */
public class ThreadLocalContextManager {
    public static final ThreadLocalRequest REQUEST = new ThreadLocalRequest();
    public static final ThreadLocalUriInfo URI_INFO = new ThreadLocalUriInfo();
    public static final ThreadLocalHttpHeaders HTTP_HEADERS = new ThreadLocalHttpHeaders();
    public static final ThreadLocalSecurityContext SECURITY_CONTEXT = new ThreadLocalSecurityContext();
    public static final ThreadLocalContextResolver CONTEXT_RESOLVER = new ThreadLocalContextResolver();
    public static final ThreadLocalApplication APPLICATION = new ThreadLocalApplication();

    public static void reset() {
        REQUEST.remove();
        URI_INFO.remove();
        HTTP_HEADERS.remove();
        SECURITY_CONTEXT.remove();
        CONTEXT_RESOLVER.remove();
        APPLICATION.remove();
    }
}
