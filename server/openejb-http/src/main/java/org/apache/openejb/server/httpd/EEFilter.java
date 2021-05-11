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

import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

import javax.enterprise.context.RequestScoped;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

// its purpose is to start/stop request scope in async tasks
// and ensure logout is propagated to security service
public class EEFilter implements Filter {
    private SecurityService securityService;
    private boolean active;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final SystemInstance si = SystemInstance.isInitialized() ? SystemInstance.get() : null;
        final Properties config = si != null ? si.getProperties() : System.getProperties();
        securityService = si != null ? si.getComponent(SecurityService.class) : null;
        active = Boolean.parseBoolean(config.getProperty("tomee.http.request.wrap", "true"));
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final boolean shouldWrap = active && HttpServletRequest.class.isInstance(servletRequest);
        if (!HttpServletRequest.class.isInstance(servletRequest)) {
            filterChain.doFilter(shouldWrap ?
                    new NoCdiRequest(HttpServletRequest.class.cast(servletRequest), servletResponse, this) : servletRequest, servletResponse);
            return;
        }
        WebBeansContext ctx;
        filterChain.doFilter(servletRequest.isAsyncSupported() &&  (ctx = WebBeansContext.currentInstance()) != null ?
                    new CdiRequest(HttpServletRequest.class.cast(servletRequest), servletResponse, ctx, this) :
                    (shouldWrap ? new NoCdiRequest(HttpServletRequest.class.cast(servletRequest), servletResponse, this) : servletRequest),
                servletResponse);
    }

    private void onLogout(final HttpServletRequest request) {
        securityService.onLogout(request);
    }

    @Override
    public void destroy() {
        // no-op
    }

    public static class NoCdiRequest extends HttpServletRequestWrapper {
        private final ServletResponse response;
        private final EEFilter filter;

        public NoCdiRequest(final HttpServletRequest cast, final ServletResponse response, final EEFilter filter) {
            super(cast);
            this.response = response;
            this.filter = filter;
        }

        @Override
        public void logout() throws ServletException {
            try {
                super.logout();
            } finally {
                filter.onLogout(HttpServletRequest.class.cast(getRequest()));
            }
        }

        @Override
        public int hashCode() {
            // unwrap and delegate
            return getRequest().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            // unwrap and delegate
            if (obj instanceof NoCdiRequest /* and CDI Request */) {
                return getRequest().equals(((NoCdiRequest) obj).getRequest());
            }
            return getRequest().equals(obj);
        }

        protected ServletResponse getResponse() {
            return response;
        }
    }

    public static class CdiRequest extends NoCdiRequest {
        private final WebBeansContext webBeansContext;

        // it's a request so not multi-threaded
        private final AtomicReference<AsynContextWrapper> asyncContextWrapperReference = new AtomicReference<>();

        public CdiRequest(final HttpServletRequest cast, final ServletResponse response, final WebBeansContext webBeansContext, final EEFilter filter) {
            super(cast, response, filter);
            this.webBeansContext = webBeansContext;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            asyncContextWrapperReference.compareAndSet(null,
                                                       new AsynContextWrapper(super.startAsync(), this, getResponse(), webBeansContext));
            return asyncContextWrapperReference.get();
        }

        @Override
        public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IllegalStateException {
            asyncContextWrapperReference.compareAndSet(null,
                                                       new AsynContextWrapper(super.startAsync(servletRequest, servletResponse), servletRequest, servletResponse, webBeansContext));
            return asyncContextWrapperReference.get();
        }

        @Override
        public AsyncContext getAsyncContext() {
            // tomcat won't return our wrapper
            return asyncContextWrapperReference.get();
        }
    }

    public static class AsynContextWrapper implements AsyncContext {
        private final AsyncContext delegate;
        private final CdiAppContextsService service;
        private final ServletResponse response;
        private volatile ServletRequest request;
        private volatile ServletRequestEvent event;

        public AsynContextWrapper(final AsyncContext asyncContext, final ServletRequest request,
                                  final ServletResponse response, final WebBeansContext webBeansContext) {
            this.delegate = asyncContext;
            this.service = CdiAppContextsService.class.cast(webBeansContext.getService(ContextsService.class));
            this.event = null;
            this.request = request;
            this.response = response;
        }

        private boolean startRequestScope() {
            if (service.getRequestContext(false) == null) {
                service.startContext(RequestScoped.class, getEvent());
                return true;
            }
            return false;
        }

        private void stopRequestScope() {
            service.endContext(RequestScoped.class, getEvent());
        }

        private ServletRequestEvent getEvent() {
            final ServletRequest request = getRequest();
            if (event == null || event.getServletRequest() != request) {
                synchronized (this) {
                    if (event == null || event.getServletRequest() != request) {
                        event = new ServletRequestEvent(request.getServletContext(), request);
                    }
                }
            }
            return event;
        }

        @Override
        public ServletRequest getRequest() {
            if (request != null) {
                return request;
            }
            return delegate.getRequest();
        }

        @Override
        public ServletResponse getResponse() {
            return delegate.getResponse();
        }

        @Override
        public boolean hasOriginalRequestAndResponse() {
            final boolean tomcatHasOriginalRequestAndResponse = delegate.hasOriginalRequestAndResponse();
            if (!tomcatHasOriginalRequestAndResponse) {
                // unfortunately in the startAsync() Tomcat computes the hasOriginalRequestAndResponse flag
                // Unfortunately we pass in the wrapped request so the flag is false
                // we need to override the value returned by Tomcat in case we are wrapping the request
                if (request instanceof NoCdiRequest) { // and CdiRequest
                    final NoCdiRequest noCdiRequest = (NoCdiRequest) this.request;
                    // Tomcat should have this as the request and not the RequestFacade because of the wrapping
                    // compare with the response we captured during wrapping
                    // they might have been wrapped bu the user during the start
                    return noCdiRequest == delegate.getRequest()
                           && noCdiRequest.getResponse() == delegate.getResponse();
                }
            }
            return tomcatHasOriginalRequestAndResponse;
        }

        @Override
        public void dispatch() {
            try {
                delegate.dispatch();
            } finally {
                request = null;
            }
        }

        @Override
        public void dispatch(final String s) {
            try {
                delegate.dispatch(s);
            } finally {
                request = null;
            }
        }

        @Override
        public void dispatch(final ServletContext servletContext, final String s) {
            try {
                delegate.dispatch(servletContext, s);
            } finally {
                request = null;
            }
        }

        @Override
        public void complete() {
            final boolean created = startRequestScope();
            try {
                delegate.complete();
            } finally {
                if (created) {
                    stopRequestScope();
                }
            }
        }

        @Override
        public void start(final Runnable runnable) {
            delegate.start(new Runnable() {
                @Override
                public void run() {
                    startRequestScope();
                    try {
                        runnable.run();
                    } finally {
                        stopRequestScope();
                    }
                }
            });
        }

        @Override
        public void addListener(final AsyncListener asyncListener) {
            delegate.addListener(wrapListener(asyncListener));
        }

        private AsyncListener wrapListener(final AsyncListener asyncListener) {
            return new ScopeAwareListener(asyncListener);
        }

        @Override
        public void addListener(final AsyncListener asyncListener, final ServletRequest servletRequest, final ServletResponse servletResponse) {
            delegate.addListener(wrapListener(asyncListener), servletRequest, servletResponse);
        }

        @Override
        public <T extends AsyncListener> T createListener(final Class<T> aClass) throws ServletException {
            return delegate.createListener(aClass);
        }

        @Override
        public void setTimeout(final long l) {
            delegate.setTimeout(l);
        }

        @Override
        public long getTimeout() {
            return delegate.getTimeout();
        }

        private class ScopeAwareListener implements AsyncListener {
            private final AsyncListener delegate;

            public ScopeAwareListener(final AsyncListener asyncListener) {
                this.delegate = asyncListener;
            }

            @Override
            public void onComplete(final AsyncEvent event) throws IOException {
                final boolean created = startRequestScope();
                try {
                    delegate.onComplete(event);
                } finally {
                    if (created) {
                        stopRequestScope();
                    }
                }
            }

            @Override
            public void onTimeout(final AsyncEvent event) throws IOException {
                final boolean created = startRequestScope();
                try {
                    delegate.onTimeout(event);
                } finally {
                    if (created) {
                        stopRequestScope();
                    }
                }
            }

            @Override
            public void onError(final AsyncEvent event) throws IOException {
                final boolean created = startRequestScope();
                try {
                    delegate.onError(event);
                } finally {
                    if (created) {
                        stopRequestScope();
                    }
                }
            }

            @Override
            public void onStartAsync(final AsyncEvent event) throws IOException {
                final boolean created = startRequestScope();
                try {
                    delegate.onStartAsync(event);
                } finally {
                    if (created) {
                        stopRequestScope();
                    }
                }
            }
        }
    }
}
