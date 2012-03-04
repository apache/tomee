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
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.util.HttpUtil;
import org.apache.openejb.server.rest.RsRegistry;
import org.apache.openejb.server.rest.RsServlet;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.loader.TomcatHelper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.tomee.catalina.TomcatWebAppBuilder.IGNORE_CONTEXT;

public class TomcatRsRegistry implements RsRegistry {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP, TomcatRsRegistry.class);

    private Engine engine;
    private List<Connector> connectors;
    private final Map<String, Context> contexts = new TreeMap<String, Context>();
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
    public AddressInfo createRsHttpListener(String root, HttpListener listener, ClassLoader classLoader, String completePath, String virtualHost) {
        String path = completePath;
        if (path == null) {
            throw new NullPointerException("contextRoot is null");
        }
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }

        // parsing could be optimized a bit...
        String realRoot = root;
        if (!root.startsWith("/")) {
            realRoot = "/" + root;
        }
        if (realRoot.length() > 1) {
            int idx = realRoot.substring(1).indexOf('/');
            if (idx > 0) {
                realRoot = realRoot.substring(0, idx + 1);
            }
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!"/".equals(realRoot)) {
            path = path.substring(realRoot.length(), path.length());
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // find the existing host (we do not auto-create hosts)
        if (virtualHost == null) virtualHost = engine.getDefaultHost();
        Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matching Host entry in the server.xml?");
        }

        // get the webapp context
        Context context = (Context) host.findChild(realRoot);

        if (context == null && "/".equals(realRoot)) { // ROOT
            context = (Context) host.findChild("");
        }

        if (context == null) {
            throw new IllegalStateException("Invalid context '" + realRoot + "'.  Cannot find context in host " + host.getName());
        }

        context.addLifecycleListener(new LifecycleListener() {
            public void lifecycleEvent(LifecycleEvent event) {
                Context context = (Context) event.getLifecycle();
                if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
                    context.getServletContext().setAttribute(IGNORE_CONTEXT, "true");
                }
                if (event.getType().equals(Lifecycle.START_EVENT) || event.getType().equals(Lifecycle.BEFORE_START_EVENT) || event.getType().equals("configure_start")) {
                    context.setConfigured(true);
                }
            }
        });


        Wrapper wrapper = context.createWrapper();
        final String name = "rest_" + listener.hashCode();
        wrapper.setName(name);
        wrapper.setServletClass(RsServlet.class.getName());

        final String mapping = path.replace("/.*", "/*");
        context.addChild(wrapper);
        wrapper.addMapping(mapping);
        context.addServletMapping(mapping, name);

        final String listenerId = wrapper.getName() + RsServlet.class.getName() + listener.hashCode();
        wrapper.addInitParameter(HttpListener.class.getName(), listenerId);
        context.getServletContext().setAttribute(listenerId, listener);

        path = address(connectors, host.getName(), realRoot);
        final String key = address(connectors, host.getName(), completePath);
        contexts.put(key, context);
        listeners.put(key, listener);

        return new AddressInfo(path, key);
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

        if (TomcatHelper.isTomcat7() && TomcatHelper.isStopping() && listeners.containsKey(path)) {
            return listeners.get(path);
        }

        Context context = contexts.remove(path);
        try {
            context.stop();
            context.destroy();
        } catch (Exception e) {
            throw new TomEERuntimeException(e);
        }
        Host host = (Host) context.getParent();
        host.removeChild(context);

        return listeners.remove(completePath);
    }
}
