/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.webservices;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.cxf.rs.CxfRsHttpListener;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.util.HttpUtil;
import org.apache.openejb.server.rest.RsRegistry;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomee.catalina.environment.Hosts;
import org.apache.tomee.catalina.registration.Registrations;
import org.apache.tomee.loader.TomcatHelper;

import jakarta.servlet.DispatcherType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TomcatRsRegistry implements RsRegistry {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP, TomcatRsRegistry.class);
    private final Hosts hosts;

    private List<Connector> connectors;
    private final Map<Key, HttpListener> listeners = new TreeMap<>();

    public TomcatRsRegistry() {
        for (final Service service : TomcatHelper.getServer().findServices()) {
            if (service.getContainer() instanceof Engine) {
                connectors = Arrays.asList(service.findConnectors());
                break;
            }
        }
        hosts = SystemInstance.get().getComponent(Hosts.class);
    }

    @Override
    public AddressInfo createRsHttpListener(final String appId, final String webContext, final HttpListener listener, final ClassLoader classLoader, final String completePath, final String virtualHost, final String auth, final String realm) {
        String path = webContext;
        if (path == null) {
            throw new NullPointerException("contextRoot is null");
        }
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }

        // find the existing host (we do not auto-create hosts)
        Container host;
        Context context = null;
        if (virtualHost == null) {
            host = hosts.getDefault();
        } else {
            host = hosts.get(virtualHost);
        }

        if (host == null) {
            for (final Host h : hosts) {
                context = findContext(h, webContext);
                if (context != null) {
                    host = h;
                    if (classLoader != null && classLoader.equals(context.getLoader().getClassLoader())) {
                        break;
                    } // else try next to find something better
                }
            }

            if (host == null) {
                throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matching Host entry in the server.xml?");
            }
        } else {
            context = findContext(host, webContext);
        }

        if (context == null) {
            throw new IllegalStateException("Invalid context '" + webContext + "'.  Cannot find context in host " + host.getName());
        }

        final CxfRsHttpListener cxfRsHttpListener = findCxfRsHttpListener(listener);
        final String description = "tomee-jaxrs-" + listener;

        String mapping = completePath;
        if (!completePath.endsWith("/*")) { // respect servlet spec (!= from our embedded listeners)
            if (completePath.endsWith("*")) {
                mapping = completePath.substring(0, completePath.length() - 1);
            }
            mapping = mapping + "/*";
        }

        final String urlPattern = removeWebContext(webContext, mapping);
        cxfRsHttpListener.setUrlPattern(urlPattern.substring(0, urlPattern.length() - 1));

        final FilterDef filterDef = new FilterDef();
        filterDef.setAsyncSupported("true");
        filterDef.setDescription(description);
        filterDef.setFilterName(description);
        filterDef.setDisplayName(description);
        filterDef.setFilter(new CXFJAXRSFilter(cxfRsHttpListener, context.findWelcomeFiles()));
        filterDef.setFilterClass(CXFJAXRSFilter.class.getName());
        filterDef.addInitParameter("mapping", urlPattern.substring(0, urlPattern.length() - "/*".length())); // just keep base path
        context.addFilterDef(filterDef);

        final FilterMap filterMap = new FilterMap();
        filterMap.addURLPattern(urlPattern);
        for (final DispatcherType type : DispatcherType.values()) {
            filterMap.setDispatcher(type.name());
        }
        filterMap.setFilterName(filterDef.getFilterName());
        context.addFilterMap(filterMap);

        Registrations.addFilterConfig(context, filterDef);

        path = address(connectors, host.getName(), webContext);
        final String key = address(connectors, host.getName(), completePath);
        listeners.put(new Key(appId, key), listener);

        return new AddressInfo(path, key);
    }

    private CxfRsHttpListener findCxfRsHttpListener(final HttpListener listener) {
        // can we have some unwrapping to do here? normally no
        return CxfRsHttpListener.class.cast(listener);
    }

    private static String removeWebContext(final String webContext, final String completePath) {
        if (webContext == null) {
            return completePath;
        }
        return completePath.substring((webContext.length() > 0 && !webContext.startsWith("/") ? 1 : 0) + webContext.length());
    }

    private static Context findContext(final Container host, final String webContext) {
        final Container[] children = host.findChildren();

        for (final Container child : children) {
            if (! Context.class.isInstance(child)) {
                continue;
            }

            final Context context = (Context) child;

            if (context.getPath().equals(webContext)) {
                return context;
            }
        }

        return null;
    }

    private static String address(final Collection<Connector> connectors, final String host, final String path) {
        final List<String> addresses = new ArrayList<>();
        for (final Connector connector : connectors) {
            final URI address;
            try {
                address = new URI(connector.getScheme(), null, host, connector.getPort(), path, null, null);
            } catch (final Exception e) { // just an URI problem normally...shouldn't occur
                LOGGER.error("can't add container for path " + path, e);
                continue;
            }
            addresses.add(address.toString());
        }
        return HttpUtil.selectSingleAddress(addresses);
    }

    @Override
    public HttpListener removeListener(final String appId, final String completePath) {
        if(completePath != null) {
            String path = completePath;
            // assure context root with a leading slash
            if (!path.startsWith("/") && !path.startsWith("http://") && !path.startsWith("https://")) {
                path = "/" + path;
            } else {
                path = completePath;
            }

            final Key key = new Key(appId, path);
            if (listeners.containsKey(key)) {
                return listeners.remove(key);
            }
        }
        return null;
    }

    private static final class Key implements Comparable<Key> {
        private final String appId; // can be versionned so context is not enough
        private final String path;
        private final int hash; // hashmap key so compute only once the hash for perf

        private Key(final String appId, final String path) {
            this.appId = appId;
            this.path = path;
            this.hash = 31 * (appId != null ? appId.hashCode() : 0) + (path != null ? path.hashCode() : 0);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Key key = Key.class.cast(o);
            return appId != null ? appId.equals(key.appId) : key.appId == null && (path != null ? path.equals(key.path) : key.path == null);

        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public int compareTo(final Key o) {
            if ((appId != null && !appId.equals(o.appId)) || o.appId != null) {
                final int appCompare = (appId == null ? "" : appId).compareTo(o.appId == null ? "" : o.appId);
                if (appCompare != 0) {
                    return appCompare;
                }
            }
            return path.compareTo(o.path);
        }
    }
}
