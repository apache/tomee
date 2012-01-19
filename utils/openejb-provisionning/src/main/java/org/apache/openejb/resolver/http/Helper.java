package org.apache.openejb.resolver.http;

import org.apache.openejb.loader.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class Helper {
    private static final int CONNECT_TIMEOUT = 10000;

    private Helper() {
        // no-op
    }

    public static void copyTryingProxies(final URI source, final File destination) throws Exception {
        final List<Proxy> proxies = ProxySelector.getDefault().select(source);
        final URL url = source.toURL();
        for (Proxy proxy : ProxySelector.getDefault().select(source)) {
            InputStream is;

            // try to connect
            try {
                URLConnection urlConnection = url.openConnection(proxy);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                is = urlConnection.getInputStream();
            } catch (IOException e) {
                continue;
            }

            // parse
            FileUtils.copy(new FileOutputStream(destination), is);
            break;
        }
    }
}
