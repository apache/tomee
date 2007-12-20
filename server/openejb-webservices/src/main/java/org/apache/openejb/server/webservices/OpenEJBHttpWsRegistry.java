/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.webservices;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.HttpEjbServer;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpListenerRegistry;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xerces.util.URI;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class OpenEJBHttpWsRegistry implements WsRegistry {
    public static final Logger log = Logger.getInstance(LogCategory.OPENEJB_WS, WsService.class);
    private final HttpListenerRegistry registry;
    private final List<URI> baseUris = new ArrayList<URI>();

    public OpenEJBHttpWsRegistry() {
        try {
            OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
            for (ServiceInfo service : configuration.facilities.services) {
                if (service.className.equals(HttpEjbServer.class.getName())) {
                    int port = Integer.parseInt(service.properties.getProperty("port"));
                    String ip = service.properties.getProperty("bind");
                    if ("0.0.0.0".equals(ip)) {
                        InetAddress[] addresses = InetAddress.getAllByName(ip);
                        for (InetAddress address : addresses) {
                            baseUris.add(new URI("http", null, address.getHostAddress(), port, null, null, null));
                        }
                    } else {
                        baseUris.add(new URI("http", null, ip, port, null, null, null));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Webservices Disabled: Unable to build base URIs for WebService registry", e);
        }
        registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
    }

    public List<String> setWsContainer(String virtualHost, String contextRoot, String servletName, HttpListener wsContainer) throws Exception {
        throw new UnsupportedOperationException("OpenEJB http server does not support POJO webservices");
    }

    public void clearWsContainer(String virtualHost, String contextRoot, String servletName) {
    }

    public List<String> addWsContainer(String path, HttpListener httpListener, String virtualHost, // ignored
            String realmName, // ignored
            String transportGuarantee, // ignored
            String authMethod, // ignored
            ClassLoader classLoader) throws Exception {

        if (path == null) throw new NullPointerException("contextRoot is null");
        if (httpListener == null) throw new NullPointerException("httpListener is null");

        // assure context root with a leading slash
        if (!path.startsWith("/")) path = "/" + path;

        httpListener = new ClassLoaderHttpListener(httpListener, classLoader);
        registry.addHttpListener(httpListener, path);

        // register wsdl locations for service-ref resolution
        List<String> addresses = new ArrayList<String>();
        for (URI baseUri : baseUris) {
            URI address = new URI(baseUri, path);
            addresses.add(address.toString());
        }
        return addresses;
    }

    public void removeWsContainer(String path) {
        registry.removeHttpListener(path);
    }

    private static class ClassLoaderHttpListener implements HttpListener {
        private final HttpListener delegate;
        private final ClassLoader classLoader;

        private ClassLoaderHttpListener(HttpListener delegate, ClassLoader classLoader) {
            this.delegate = delegate;
            this.classLoader = classLoader;
        }

        public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            try {
                delegate.onMessage(request, response);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
    }
}
