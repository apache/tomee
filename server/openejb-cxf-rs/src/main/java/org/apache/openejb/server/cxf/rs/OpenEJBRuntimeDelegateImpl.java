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
 * @author rmannibucau
 */
public class OpenEJBRuntimeDelegateImpl extends RuntimeDelegateImpl {
    @Override public UriBuilder createUriBuilder() {
        return new OpenEJBUriBuilderImpl();
    }

    private static class OpenEJBUriBuilderImpl extends UriBuilderImpl {
        private static final String[][] PREFIX = new String[][]{ { "http:/", "http://"}, { "https:/", "https://" } };

        @Override public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
            String uri = getFixedUri(super.build(values).toString());
            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                throw new UriBuilderException(e);
            }
        }

        private String getFixedUri(final String s) {
            String uri = s;
            for (String[] prefix : PREFIX) {
                if (uri.startsWith(prefix[0]) && !uri.startsWith(prefix[1])) {
                    uri = uri.replaceFirst(prefix[0], prefix[1]);
                    break;
                }
            }
            return uri;
        }

        @Override public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
            String uri = getFixedUri(super.buildFromEncoded(values).toString());
            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                throw new UriBuilderException(e);
            }
        }

        @Override public URI buildFromEncodedMap(Map<String, ?> map) throws IllegalArgumentException, UriBuilderException {
            String uri = getFixedUri(super.buildFromEncodedMap(map).toString());
            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                throw new UriBuilderException(e);
            }
        }

        @Override public URI buildFromMap(Map<String, ?> map) throws IllegalArgumentException, UriBuilderException {
            String uri = getFixedUri(super.buildFromMap(map).toString());
            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                throw new UriBuilderException(e);
            }
        }
    }
}
