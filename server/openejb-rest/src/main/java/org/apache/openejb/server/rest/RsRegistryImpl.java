package org.apache.openejb.server.rest;

import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.OpenEJBHttpRegistry;

import java.util.List;

/**
 * @author Romain Manni-Bucau
 */
public class RsRegistryImpl extends OpenEJBHttpRegistry implements RsRegistry {
    @Override public List<String> createRsHttpListener(HttpListener listener, ClassLoader classLoader, String path) {
        addWrappedHttpListener(listener, classLoader, path);
        return getResolvedAddresses(path);
    }

    @Override public HttpListener removeListener(String context) {
        return registry.removeHttpListener(context);
    }
}
