/**
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.cdi.Proxys;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.web.LightweightWebAppBuilder;


/**
 * @version $Revision$ $Date$
 */
public class HttpListenerRegistry implements HttpListener {
    private final Map<String, HttpListener> registry = new LinkedHashMap<>();
    private final Map<String, Collection<HttpListener>> filterRegistry = new LinkedHashMap<>();
    private final ThreadLocal<FilterListener> currentFilterListener = new ThreadLocal<>();
    private final ThreadLocal<HttpRequest> request = new ThreadLocal<>();
    private final ClassLoader defaultClassLoader;
    private final File[] resourceBases;
    private final Map<String, String> defaultContextTypes = new HashMap<>();
    private final String welcomeFile = SystemInstance.get().getProperty("openejb.http.welcome", "index.html");
    private final Map<String, byte[]> cache = new HashMap<>();
    private final boolean cacheResources = "true".equals(SystemInstance.get().getProperty("openejb.http.resource.cache", "false"));

    public HttpListenerRegistry() {
        HttpServletRequest mock = null;
        final SystemInstance systemInstance = SystemInstance.get();
        if ("true".equalsIgnoreCase(systemInstance.getProperty("openejb.http.mock-request", "false"))) {
            HttpRequestImpl mockRequest = null;
            try {
                mockRequest = new HttpRequestImpl(new URI("http://mock/"));
                mockRequest.parseURI(new StringTokenizer("mock\n"));  // will do http://mock/mock, we don't really care
                mock = mockRequest;
            } catch (final Exception e) {
                // no-op
            }
        }
        if (systemInstance.getComponent(HttpServletRequest.class) == null) {
            systemInstance.setComponent(HttpServletRequest.class, Proxys.threadLocalProxy(HttpServletRequest.class, request, mock));
        }
        if (systemInstance.getComponent(HttpSession.class) == null) {
            final jakarta.servlet.http.HttpSession delegate = mock != null ? mock.getSession() : null;
            systemInstance.setComponent(jakarta.servlet.http.HttpSession.class, Proxys.threadLocalRequestSessionProxy(request, new ServletSessionAdapter(delegate) {
                @Override
                public void invalidate() {
                    final Object web = AppFinder.findAppContextOrWeb(Thread.currentThread().getContextClassLoader(), AppFinder.AppOrWebContextTransformer.INSTANCE);
                    if (WebContext.class.isInstance(web)) {
                        doInvokeSpecificListeners(WebContext.class.cast(web).getContextRoot());
                    } else if (AppContext.class.isInstance(web)) {
                        doInvokeSpecificListeners(AppContext.class.cast(web).getId());
                    }
                    super.invalidate();
                }

                private void doInvokeSpecificListeners(final String web) {
                    final WebAppBuilder wab = SystemInstance.get().getComponent(WebAppBuilder.class);
                    if (LightweightWebAppBuilder.class.isInstance(wab)) {
                        final Collection<HttpSessionListener> listeners = LightweightWebAppBuilderListenerExtractor.findByTypeForContext(web, HttpSessionListener.class);
                        final HttpSessionEvent event = new HttpSessionEvent(this);
                        for (final HttpSessionListener o : listeners) {
                            try {
                                o.sessionDestroyed(event);
                            } catch (final Throwable th) {
                                // ignore, may be undeployed
                            }
                        }
                    }
                }
            }));
        }
        if (systemInstance.getComponent(ServletContext.class) == null) { // a poor impl but at least we set something
            systemInstance.setComponent(ServletContext.class, new EmbeddedServletContext());
        }

        defaultClassLoader = ParentClassLoaderFinder.Helper.get();

        String resourceFolderPaths = SystemInstance.get().getProperty("openejb.embedded.http.resources");
        Collection<File> resources = new LinkedList<>();
        if (resourceFolderPaths != null) {
            for (final String path : resourceFolderPaths.split(" , ")) {
                if (!path.isEmpty()) {
                    resources.add(new File(path));
                }
            }
        }
        resourceBases = resources.toArray(new File[resources.size()]);

        defaultContextTypes.put("html", "text/html");
        defaultContextTypes.put("html", "text/html");
        defaultContextTypes.put("css", "text/css");
        defaultContextTypes.put("txt", "text/plain");
        defaultContextTypes.put("xml", "application/xml");
        defaultContextTypes.put("xsl", "application/xml");
        defaultContextTypes.put("js", "application/javascript");
        defaultContextTypes.put("gif", "image/gif");
        defaultContextTypes.put("jpeg", "image/jpeg");
        defaultContextTypes.put("jpg", "image/jpeg");
        defaultContextTypes.put("png", "image/png");
        defaultContextTypes.put("tiff", "image/tiff");
    }

    @Override
    public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
        final String path;
        if (!HttpRequestImpl.class.isInstance(request)) {
            path = request.getRequestURI();
        } else {
            path = getRequestHandledPath(request);
        }

        final FilterListener currentFL = currentFilterListener.get();

        // first look filters
        Map<String, Collection<HttpListener>> filters;
        synchronized (filterRegistry) {
            filters = new HashMap<>(filterRegistry);
        }

        final HttpRequest registered = this.request.get();
        final boolean reset = registered == null;
        try {
            if (reset) {
                this.request.set(request);
            }
            boolean lastWasCurrent = false;
            for (Map.Entry<String, Collection<HttpListener>> entry : filters.entrySet()) {
                String pattern = entry.getKey();
                for (HttpListener listener : entry.getValue()) {
                    if ((lastWasCurrent || currentFL == null) && path.matches(pattern)) {
                        listener.onMessage(request, response);
                        return;
                    }
                    lastWasCurrent = listener == currentFL;
                }
            }


            // then others
            Map<String, HttpListener> listeners;
            synchronized (registry) {
                listeners = new HashMap<>(registry);
            }

            boolean found = false;
            for (final Map.Entry<String, HttpListener> entry : listeners.entrySet()) {
                final String pattern = entry.getKey();
                if (path.matches(pattern) || path.equals(pattern)) {
                    if (pattern.contains("/.*\\.") && HttpRequestImpl.class.isInstance(request)) { // TODO: enhance it, basically servlet *.xxx
                        HttpRequestImpl.class.cast(request).noPathInfo();
                    }
                    entry.getValue().onMessage(request, response);
                    found = true;
                    break;
                }
            }
            if (!found) {
                final String servletPath = request.getServletPath();
                if (servletPath != null) {
                    URL url = SystemInstance.get().getComponent(ServletContext.class).getResource(servletPath);
                    if (url != null) {
                        serveResource(servletPath, response, url);
                    } else {
                        final String pathWithoutSlash = "/".equals(path) || "".equals(servletPath) || "/".equals(servletPath) ? welcomeFile :
                                (servletPath.startsWith("/") ? servletPath.substring(1) : servletPath);
                        url = defaultClassLoader.getResource("META-INF/resources/" + pathWithoutSlash);
                        if (url != null) {
                            serveResource(servletPath, response, url);
                        } else if (resourceBases.length > 0) {
                            for (final File f : resourceBases) {
                                final File file = new File(f, pathWithoutSlash);
                                if (file.isFile()) {
                                    url = file.toURI().toURL();
                                    serveResource(servletPath, response, url);
                                    break;
                                }
                            }
                        }
                    }
                    if (url != null) {
                        final int dot = servletPath.lastIndexOf('.');
                        if (dot > 0 && dot < servletPath.length() - 1) {
                            final String ext = servletPath.substring(dot + 1);
                            final String ct = defaultContextTypes.get(ext);
                            if (ct != null) {
                                response.setContentType(ct);
                            } else {
                                final String uct = SystemInstance.get().getProperty("openejb.embedded.http.content-type." + ext);
                                if (uct != null) {
                                    response.setContentType(uct);
                                }
                            }
                        }
                    }
                } // TODO else 404
            }
        } finally {
            if (currentFL == null) {
                currentFilterListener.set(null);
            }
            if (reset) {
                this.request.set(null);
            }
        }
    }

    private void serveResource(final String key, final HttpResponse response, final URL url) throws IOException {
        if (cacheResources) {
            byte[] value = cache.get(key);
            if (value == null) {
                final InputStream from = url.openStream();
                try {
                    ByteArrayOutputStream to = new ByteArrayOutputStream();
                    IO.copy(from, to);
                    value = to.toByteArray();
                    cache.put(key, value);
                } finally {
                    IO.close(from);
                }
            }
            response.getOutputStream().write(value);
        } else {
            final InputStream from = url.openStream();
            try {
                IO.copy(from, response.getOutputStream());
            } finally {
                IO.close(from);
            }
        }
    }

    private String getRequestHandledPath(final HttpRequest request) {
        final String servletPath = request.getServletPath();
        return request.getContextPath() + (!servletPath.startsWith("/") ? "/" : "") + servletPath;
    }

    public void addHttpListener(HttpListener listener, String regex) {
        synchronized (registry) {
            registry.put(regex, listener);
        }
    }

    public HttpListener removeHttpListener(String regex) {
        HttpListener listener;
        synchronized (registry) {
            listener = registry.remove(regex);
        }
        return listener;
    }

    public void addHttpFilter(HttpListener listener, String regex) {
        synchronized (filterRegistry) {
            if (!filterRegistry.containsKey(regex)) {
                filterRegistry.put(regex, new ArrayList<HttpListener>());
            }
            filterRegistry.get(regex).add(listener);
        }
    }

    public Collection<HttpListener> removeHttpFilter(String regex) {
        synchronized (filterRegistry) {
            return filterRegistry.remove(regex);
        }
    }

    public void setOrigin(final FilterListener origin) {
        if (origin == null) {
            currentFilterListener.remove();
        } else {
            currentFilterListener.set(origin);
        }
    }
}
