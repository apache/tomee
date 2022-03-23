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

import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.DaemonThreadFactory;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// stupid impl, ~ mock. it handles itself eviction to avoid to have it always started which is not the common case
public class OpenEJBAsyncContext implements AsyncContext {
    private static final Set<OpenEJBAsyncContext> INITIALIZED = new CopyOnWriteArraySet<>();
    private static volatile ScheduledExecutorService es;

    public static void destroy() {
        if (es == null) {
            return;
        }
        es.shutdownNow();
        for (final OpenEJBAsyncContext ctx : new ArrayList<>(INITIALIZED)) {
            if (ctx.lastTouch + ctx.getTimeout() < System.currentTimeMillis()) {
                for (final AsyncListener listener : ctx.listeners) {
                    try {
                        listener.onTimeout(ctx.event);
                    } catch (final IOException t) {
                        throw new OpenEJBRuntimeException(t);
                    }
                }
                ctx.complete();
            }
        }
        INITIALIZED.clear();
    }

    public static void init() {
        if (!"true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.http.async.eviction", "true"))) {
            return;
        }
        es = Executors.newScheduledThreadPool(1, new DaemonThreadFactory(OpenEJBAsyncContext.class));
        es.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (final OpenEJBAsyncContext ctx : new ArrayList<>(INITIALIZED)) {
                    if (ctx.lastTouch + ctx.getTimeout() < System.currentTimeMillis()) {
                        INITIALIZED.remove(ctx);
                        for (final AsyncListener listener : ctx.listeners) {
                            try {
                                listener.onTimeout(ctx.event);
                            } catch (final IOException t) {
                                throw new OpenEJBRuntimeException(t);
                            }
                        }
                        ctx.complete();
                    }
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private final List<AsyncListener> listeners = new ArrayList<>();
    private final ServletResponse response;
    private final HttpServletRequest request;
    private final Socket socket;
    private final AsyncEvent event;
    private WebContext context = null;
    private final String contextPath; // fallback if we don't find the context
    private volatile boolean started = false;
    private volatile boolean committed = false;
    private long timeout = 30000;
    private long lastTouch = System.currentTimeMillis();

    public OpenEJBAsyncContext(final HttpServletRequest request, final ServletResponse response, final String contextPath) {
        if (es == null) {
            synchronized (OpenEJBAsyncContext.class) { // we don't care since impl is not really thread safe, just here for testing
                if (es == null) {
                    init();
                }
            }
        }

        this.request = request;
        this.contextPath = contextPath;
        if (contextPath != null) {
            for (final AppContext app : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
                for (final WebContext web : app.getWebContexts()) {
                    if (web.getContextRoot().replace("/", "").equals(contextPath.replace("/", ""))) {
                        this.context = web;
                        break;
                    }
                }
            }
        }
        this.response = response;
        this.socket = Socket.class.cast(request.getAttribute("openejb_socket"));
        this.event = new AsyncEvent(this, request, response);
        INITIALIZED.add(this);
    }

    @Override
    public void complete() {
        for (final AsyncListener listener : listeners) {
            try {
                listener.onComplete(event);
            } catch (final IOException t) {
                throw new OpenEJBRuntimeException(t);
            }
        }

        try {
            commit();
        } finally {
            INITIALIZED.remove(this);
        }
    }

    private void onError(final Throwable ignored) {
        for (final AsyncListener listener : listeners) {
            try {
                listener.onError(event);
            } catch (final IOException t) {
                throw new OpenEJBRuntimeException(t);
            }
        }
        try {
            HttpServletResponse.class.cast(response).sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
            commit();
        } catch (final IOException e) {
            // no-op
        }
    }

    private void commit() {
        if (committed) { // avoid to commit it twice on errors (often a listener will do it, we just force it in case of)
            return;
        }
        committed = true;
        if (HttpResponseImpl.class.isInstance(response) && socket != null) {
            try {
                HttpResponseImpl.class.cast(response).writeMessage(socket.getOutputStream(), false);
            } catch (final IOException e) {
                // no-op
            }
        } // else TODO

        if (socket != null) {
            try {
                socket.getInputStream().close();
            } catch (final IOException e) {
                // no-op
            }
            try {
                socket.getOutputStream().close();
            } catch (final IOException e) {
                // no-op
            }
            try {
                socket.close();
            } catch (final IOException e) {
                // no-op
            }
        }
    }

    @Override
    public void dispatch() {
        String path;
        final String pathInfo;
        ServletRequest servletRequest = getRequest();
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest sr = (HttpServletRequest) servletRequest;
            path = sr.getServletPath();
            pathInfo = sr.getPathInfo();
        } else {
            path = request.getServletPath();
            pathInfo = request.getPathInfo();
        }
        if (pathInfo != null) {
            path += pathInfo;
        }
        dispatch(path);
    }

    @Override
    public void dispatch(final String path) {
        dispatch(request.getServletContext(), path);
    }

    @Override
    public void dispatch(final ServletContext context, final String path) {
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        try {
            final String contextPath = this.context == null ?
                    this.contextPath : (this.context.getContextRoot().startsWith("/") ? this.context.getContextRoot() : ('/' + this.context.getContextRoot()));
            final HttpRequestImpl req = new HttpRequestImpl(new URI(request.getRequestURI())) {
                private String thisPath = path;
                private String thisContextPath = contextPath;

                @Override
                public String getContextPath() {
                    return thisContextPath;
                }

                @Override
                public String getServletPath() {
                    return thisPath;
                }
                
                @Override
                public void initServletPath(final String servlet) {
                    thisPath = servlet;
                }

                @Override
                public void initPathFromContext(final String context) {
                    super.initPathFromContext(context);
                    thisContextPath = super.getContextPath();
                }

                @Override
                public String getRequestURI() { // not thisContextPath + thisPath since it would break JAXRS
                    return contextPath + path;
                }

                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return request.getInputStream();
                }

                @Override
                public Object getAttribute(final String name) {
                    return request.getAttribute(name);
                }

                @Override
                public Enumeration<String> getAttributeNames() {
                    return request.getAttributeNames();
                }

                @Override
                public void setAttribute(final String name, final Object value) {
                    request.setAttribute(name, value);
                }

                @Override
                public String getHeader(final String name) {
                    return request.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    return request.getHeaderNames();
                }

                @Override
                public Enumeration<String> getHeaders(final String s) {
                    return request.getHeaders(s);
                }

                @Override
                public int getIntHeader(final String s) {
                    return request.getIntHeader(s);
                }

                @Override
                public String getMethod() {
                    return request.getMethod();
                }
            };
            if (HttpRequestImpl.class.isInstance(request)) { // needed for some advanced cases like async
                req.setUri(HttpRequestImpl.class.cast(request).getURI());
            }
            req.setAttribute("openejb_async_dispatch", true);
            registry.onMessage(req, HttpResponse.class.isInstance(response) ? HttpResponse.class.cast(response) : new ServletResponseAdapter(HttpServletResponse.class.cast(response)));
            complete();
        } catch (final Exception e) {
            onError(e);
        }
    }

    @Override
    public ServletRequest getRequest() {
        return request;
    }

    @Override
    public ServletResponse getResponse() {
        if (response == null) {
            throw new IllegalStateException("no response");
        }
        return response;
    }

    @Override
    public void start(final Runnable run) {
        internalStartAsync();
        // TODO: another thread
        run.run();
    }

    public void internalStartAsync() {
        started = true;
        for (final AsyncListener listener : listeners) {
            try {
                listener.onStartAsync(event);
            } catch (final IOException t) {
                throw new OpenEJBRuntimeException(t);
            }
        }
    }

    @Override
    public void addListener(final AsyncListener listener) {
        listeners.add(listener);
        if (started) {
            try {
                listener.onStartAsync(event);
            } catch (final IOException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }

    @Override
    public void addListener(final AsyncListener listener,
                            final ServletRequest servletRequest,
                            final ServletResponse servletResponse) {
        addListener(listener);
    }

    @Override
    public <T extends AsyncListener> T createListener(final Class<T> clazz)
            throws ServletException {
        try {
            return (T) context.inject(clazz.newInstance());
        } catch (final Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return true;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
