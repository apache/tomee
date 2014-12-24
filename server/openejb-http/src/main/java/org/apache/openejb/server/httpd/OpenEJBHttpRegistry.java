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

import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
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

        protected ClassLoaderHttpListener(HttpListener delegate, ClassLoader classLoader) {
            this.delegate = delegate;
            this.classLoader = classLoader;
        }

        public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
            final Thread thread = Thread.currentThread();
            final ClassLoader oldCl = thread.getContextClassLoader();

            WebBeansContext wbc = null;
            try {
                if (request instanceof HttpRequestImpl) {
                    final HttpRequestImpl httpRequest = HttpRequestImpl.class.cast(request);
                    final WebContext web = findWebContext(request.getURI().getPath());
                    if (web != null) {
                        if (web.getClassLoader() != null) {
                            thread.setContextClassLoader(web.getClassLoader());
                        } else if (web.getAppContext().getClassLoader() != null) {
                            thread.setContextClassLoader(web.getAppContext().getClassLoader());
                        }

                        final String ctx = (web.getContextRoot().startsWith("/") ? "" : "/") + web.getContextRoot();
                        httpRequest.initPathFromContext(ctx);
                        if (httpRequest.getServletPath().startsWith('/' + ctx)) { // yes double /, there is an encoding issue with JSF forms with our embedded mode
                            final String servlet = URLDecoder.decode(httpRequest.getServletPath().substring(ctx.length() + 1), "UTF-8");
                            final int query = servlet.indexOf('?');
                            if (query > 0) {
                                httpRequest.initServletPath(servlet.substring(0, query));
                                httpRequest.addQueryParams(servlet.substring(query + 1));
                            } else {
                                httpRequest.initServletPath(servlet);
                            }
                        }
                        wbc = web.getWebbeansContext() != null ? web.getWebbeansContext() : web.getAppContext().getWebBeansContext();
                    } else {
                        thread.setContextClassLoader(classLoader);

                        try { // surely an issue or something just tolerated for fake webapps
                            wbc = WebBeansContext.currentInstance();
                        } catch (final IllegalStateException ise) {
                            // no-op
                        }
                    }
                    if (wbc != null) {
                        initCdi(wbc, httpRequest).init();
                    }
                }

                delegate.onMessage(request, response);
            } finally {
                if (wbc != null) {
                    HttpRequestImpl.class.cast(request).destroy();
                }

                thread.setContextClassLoader(oldCl);
            }
        }

        private static WebContext findWebContext(final String path) { // TODO: this is a poor impl, would need registration of app etc to be better and have a rooter
            for (final AppContext app : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
                for (final WebContext web : app.getWebContexts()) {
                    if (path.startsWith(web.getContextRoot()) || path.startsWith('/' + web.getContextRoot())) {
                        return web;
                    }
                }
            }
            return null;
        }

        private static HttpRequestImpl initCdi(final WebBeansContext context, final HttpRequestImpl request) {
            try {
                if (context.getBeanManagerImpl().isInUse()) {
                    request.setBeginListener(new BeginWebBeansListener(context));
                    request.setEndListener(new EndWebBeansListener(context));
                }
            } catch (IllegalStateException ise) {
                // no-op: ignore
            }
            return request;
        }

        public HttpListener getDelegate() {
            return delegate;
        }
    }
}
