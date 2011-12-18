/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.httpd;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenEJBHttpRegistry {
    public static final Logger log = Logger.getInstance(LogCategory.HTTPSERVER, OpenEJBHttpRegistry.class);

    protected final HttpListenerRegistry registry;
    protected final List<URI> baseUris = new ArrayList<URI>();

    public OpenEJBHttpRegistry() {
        try {
            OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
            for (ServiceInfo service : configuration.facilities.services) {
                if (service.className.equals(HttpServerFactory.class.getName())) {
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
            log.error("Unable to build base URIs for " + getClass().getSimpleName() + " registry", e);
        }
        registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
    }

    public HttpListener addWrappedHttpListener(HttpListener httpListener, ClassLoader classLoader, String regex) {
        HttpListener listener = new ClassLoaderHttpListener(httpListener, classLoader);
        registry.addHttpListener(listener, regex);
        return listener;
    }

    public List<String> getResolvedAddresses(String path) {
        String suffix = path;
        if (!path.startsWith("/")) {
            suffix = '/' + suffix;
        }

        List<String> addresses = new ArrayList<String>();
        for (URI baseUri : baseUris) {
            URI address = baseUri.resolve(suffix);
            addresses.add(address.toString());
        }
        return  addresses;
    }

    protected static class ClassLoaderHttpListener implements HttpListener {
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

        public HttpListener getDelegate() {
            return delegate;
        }
    }
}
