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

import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.util.HttpUtil;
import org.apache.openejb.server.rest.RsRegistry;
import org.apache.openejb.server.rest.RsServlet;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.loader.TomcatHelper;

import java.net.URI;
import java.util.*;

public class TomcatRsRegistry implements RsRegistry {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP, TomcatRsRegistry.class);

    private Engine engine;
    private List<Connector> connectors;
    private final Map<String, HttpListener> listeners = new TreeMap<String, HttpListener>();

    public TomcatRsRegistry() {
        StandardServer standardServer = TomcatHelper.getServer();
        for (Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                connectors = Arrays.asList(service.findConnectors());
                engine = (Engine) service.getContainer();
                break;
            }
        }
    }

    @Override
    public AddressInfo createRsHttpListener(String webContext, HttpListener listener, ClassLoader classLoader, String completePath, String virtualHost) {
        String path = webContext;
        if (path == null) {
            throw new NullPointerException("contextRoot is null");
        }
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }

        // find the existing host (we do not auto-create hosts)
        if (virtualHost == null) virtualHost = engine.getDefaultHost();
        Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matching Host entry in the server.xml?");
        }

        // get the webapp context
        Context context = (Context) host.findChild(webContext);

        if (context == null && "/".equals(webContext)) { // ROOT
            context = (Context) host.findChild("");
        }

        if (context == null) {
            throw new IllegalStateException("Invalid context '" + webContext + "'.  Cannot find context in host " + host.getName());
        }

        Wrapper wrapper = context.createWrapper();
        final String name = "rest_" + listener.hashCode();
        wrapper.setName(name);
        wrapper.setServletClass(RsServlet.class.getName());

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
