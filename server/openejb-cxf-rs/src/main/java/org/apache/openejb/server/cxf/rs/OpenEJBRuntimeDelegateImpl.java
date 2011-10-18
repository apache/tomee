package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * /!\ we need to load it before cxf implementation.
 * see org.apache.geronimo.osgi.locator.ProviderLocator#locateServiceClassName(java.lang.String, java.lang.Class<?>, java.lang.ClassLoader)
 * which need to be overriden or we have to do something to be the first.
 *
 * That's why openejb-cxf-bundle was created.
 *
 * @author rmannibucau
 */
public class OpenEJBRuntimeDelegateImpl extends RuntimeDelegateImpl {
    @Override public UriBuilder createUriBuilder() {
        return new OpenEJBUriBuilderImpl();
    }

    private static class OpenEJBUriBuilderImpl extends UriBuilderImpl {
        private static final String[][] PREFIX = new String[][]{ { "http:/", "http://" }, { "https:/", "https://" } };
        private boolean init = false;

        @Override public UriBuilder replacePath(String value) {
            // UriBuilder.fromPath("foo").replacePath(null) is ok
            // but not UriBuilder.fromPath(null)
            if (value == null && !init) {
                throw new IllegalArgumentException("value is null");
            }
            init = true;
            return super.replacePath(value);
        }

        @Override public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
            return getFixedUri(super.build(values).toString());
        }

        private URI getFixedUri(final String s) throws UriBuilderException {
            String uri = s;
            for (String[] prefix : PREFIX) {
                if (uri.startsWith(prefix[0]) && !uri.startsWith(prefix[1])) {
                    uri = uri.replaceFirst(prefix[0], prefix[1]);
                    break;
                }
            }

            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                throw new UriBuilderException(e);
            }
        }

        @Override public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
            return getFixedUri(super.buildFromEncoded(values).toString());
        }

        @Override public URI buildFromEncodedMap(Map<String, ?> map) throws IllegalArgumentException, UriBuilderException {
            return getFixedUri(super.buildFromEncodedMap(map).toString());
        }

        @Override public URI buildFromMap(Map<String, ?> map) throws IllegalArgumentException, UriBuilderException {
            return getFixedUri(super.buildFromMap(map).toString());
        }
    }
}
