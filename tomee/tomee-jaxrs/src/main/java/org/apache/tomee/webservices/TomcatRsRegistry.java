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
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.util.HttpUtil;
import org.apache.openejb.server.rest.RsRegistry;
import org.apache.openejb.server.rest.RsServlet;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.environment.Hosts;
import org.apache.tomee.loader.TomcatHelper;

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
    private final Map<String, HttpListener> listeners = new TreeMap<String, HttpListener>();

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
    public AddressInfo createRsHttpListener(String webContext, HttpListener listener, ClassLoader classLoader, String completePath, String virtualHost, String auth, String realm) {
        String path = webContext;
        if (path == null) {
            throw new NullPointerException("contextRoot is null");
        }
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }

        // find the existing host (we do not auto-create hosts)
        Container host = null;
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

        final Wrapper wrapper = context.createWrapper();
        final String name = "rest_" + listener.hashCode();
        wrapper.setName(name);
        wrapper.setServletClass(RsServlet.class.getName());
        wrapper.setAsyncSupported(true);

        String mapping = completePath;
        if (!completePath.endsWith("/*")) { // respect servlet spec (!= from our embedded listeners)
            if (completePath.endsWith("*")) {
                mapping = completePath.substring(0, completePath.length() - 1);
            }
            mapping = mapping + "/*";
        }

        context.addChild(wrapper);
        wrapper.addMapping(removeWebContext(webContext, mapping));
        context.addServletMapping(mapping, name);

        final String listenerId = wrapper.getName() + RsServlet.class.getName() + listener.hashCode();
        wrapper.addInitParameter(HttpListener.class.getName(), listenerId);
        context.getServletContext().setAttribute(listenerId, listener);

        path = address(connectors, host.getName(), webContext);
        final String key = address(connectors, host.getName(), completePath);
        listeners.put(key, listener);

        return new AddressInfo(path, key);
    }

    private static Context findContext(final Container host, final String webContext) {
        Context webapp = Context.class.cast(host.findChild(webContext));
        if (webapp == null && "/".equals(webContext)) { // ROOT
            webapp = Context.class.cast(host.findChild(""));
        }
        return webapp;
    }

    private static String removeWebContext(final String webContext, final String completePath) {
        if (webContext == null) {
            return completePath;
        }
        return completePath.substring(webContext.length());
    }

    private static String address(final Collection<Connector> connectors, final String host, final String path) {
        List<String> addresses = new ArrayList<String>();
        for (Connector connector : connectors) {
            URI address;
            try {
                address = new URI(connector.getScheme(), null, host, connector.getPort(), path, null, null);
            } catch (Exception e) { // just an URI problem normally...shouldn't occur
                LOGGER.error("can't add container for path " + path, e);
                continue;
            }
            addresses.add(address.toString());
        }
        return HttpUtil.selectSingleAddress(addresses);
    }

    @Override
    public HttpListener removeListener(final String completePath) {
        String path = completePath;
        if (path == null) {
            return listeners.get(path);
        }

        // assure context root with a leading slash
        if (!path.startsWith("/") && !path.startsWith("http://") && !path.startsWith("https://")) {
            path = "/" + path;
        }

        if (listeners.containsKey(path)) {
            return listeners.remove(path);
        }
        return null;
    }
}
