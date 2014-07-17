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
import org.apache.webbeans.config.WebBeansContext;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class OpenEJBHttpRegistry {
    public static final Logger log = Logger.getInstance(LogCategory.HTTPSERVER, OpenEJBHttpRegistry.class);

    protected final HttpListenerRegistry registry;
    protected final List<URI> baseUris = new ArrayList<URI>();

    public OpenEJBHttpRegistry() {
        try {
            final OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
            for (final ServiceInfo service : configuration.facilities.services) {
                if (service.className.equals(HttpServerFactory.class.getName())) {
                    final int port = Integer.parseInt(service.properties.getProperty("port"));
                    final String ip = service.properties.getProperty("bind");
                    if ("0.0.0.0".equals(ip)) {
                        final InetAddress[] addresses = InetAddress.getAllByName(ip);
                        for (final InetAddress address : addresses) {
                            baseUris.add(new URI("http", null, address.getHostAddress(), port, null, null, null));
                        }
                    } else {
                        baseUris.add(new URI("http", null, ip, port, null, null, null));
                    }
                    break;
                }
            }
        } catch (final Exception e) {
            log.error("Unable to build base URIs for " + getClass().getSimpleName() + " registry", e);
        }
        registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
    }

    public HttpListener addWrappedHttpListener(final HttpListener httpListener, final ClassLoader classLoader, final String regex) {
        final HttpListener listener = new ClassLoaderHttpListener(httpListener, classLoader);
        registry.addHttpListener(listener, regex);
        return listener;
    }

    public List<String> getResolvedAddresses(final String path) {
        String suffix = path;
        if (!path.startsWith("/")) {
            suffix = '/' + suffix;
        }

        final List<String> addresses = new ArrayList<String>();
        for (final URI baseUri : baseUris) {
            URI uri = baseUri;
            if (baseUri.getPort() == 0) { // if port was set to 0 we need to get httpejbd service port which was updated in SystemInstance
                final int port = Integer.parseInt(SystemInstance.get().getProperty("httpejbd.port", "0"));
                if (port != 0) {
                    try {
                        uri = new URI(baseUri.getScheme(), baseUri.getUserInfo(), baseUri.getHost(), port, baseUri.getPath(), baseUri.getQuery(), baseUri.getFragment());
                    } catch (final URISyntaxException e) {
                        // no-op
                    }
                }
            }
            final URI address = uri.resolve(suffix);
            addresses.add(address.toString());
        }
        return addresses;
    }

    protected static class ClassLoaderHttpListener implements HttpListener {
        private final HttpListener delegate;
        private final ClassLoader classLoader;

        protected ClassLoaderHttpListener(final HttpListener delegate, final ClassLoader classLoader) {
            this.delegate = delegate;
            this.classLoader = classLoader;
        }

        public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            try {
                if (request instanceof HttpRequestImpl) {
                    initCdi((HttpRequestImpl) request).init();
                }

                delegate.onMessage(request, response);
            } finally {
                if (request instanceof HttpRequestImpl) {
                    ((HttpRequestImpl) request).destroy();
                }

                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }

        private static HttpRequestImpl initCdi(final HttpRequestImpl request) {
            try {
                final WebBeansContext context = WebBeansContext.currentInstance();
                if (context.getBeanManagerImpl().isInUse()) {
                    request.setBeginListener(new BeginWebBeansListener(context));
                    request.setEndListener(new EndWebBeansListener(context));
                }
            } catch (final IllegalStateException ise) {
                // no-op: ignore
            }
            return request;
        }

        public HttpListener getDelegate() {
            return delegate;
        }
    }
}
