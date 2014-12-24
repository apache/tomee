package org.apache.openejb.arquillian.openejb.http;

import org.apache.openejb.arquillian.openejb.SWClassLoader;
import org.apache.openejb.server.httpd.EmbeddedServletContext;

import java.net.URL;

public class WebArchiveResourceProvider implements EmbeddedServletContext.ResourceProvider {
    @Override
    public URL getResource(final String s) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (SWClassLoader.class.isInstance(tccl)) {
            return SWClassLoader.class.cast(tccl).getWebResource(s);
        }
        return null;
    }
}
