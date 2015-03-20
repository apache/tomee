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
import java.io.IOException;

public class WebBeansFilter implements Filter { // its pupose is to start/stop request scope in async tasks
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        if (!HttpServletRequest.class.isInstance(servletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        filterChain.doFilter(new CdiRequest(HttpServletRequest.class.cast(servletRequest)), servletResponse);
    }

    @Override
    public void destroy() {
        // no-op
    }

    public static class CdiRequest extends HttpServletRequestWrapper {
        public CdiRequest(final HttpServletRequest cast) {
            super(cast);
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            return new AsynContextWrapper(super.startAsync());
        }

        @Override
        public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IllegalStateException {
            return new AsynContextWrapper(super.startAsync(servletRequest, servletResponse));
        }
    }

    public static class AsynContextWrapper implements AsyncContext {
        private final AsyncContext delegate;
        private final CdiAppContextsService service;
        private volatile ServletRequestEvent event;

        public AsynContextWrapper(final AsyncContext asyncContext) {
            this.delegate = asyncContext;
            this.service = CdiAppContextsService.class.cast(WebBeansContext.currentInstance().getService(ContextsService.class));
            this.event = null;
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
            if (event == null || event.getServletRequest() != getRequest()) {
                synchronized (this) {
                    if (event == null || event.getServletRequest() != getRequest()) {
                        final ServletRequest request = delegate.getRequest();
                        event = new ServletRequestEvent(request.getServletContext(), request);
                    }
                }
            }
            return event;
        }

        @Override
        public ServletRequest getRequest() {
            return delegate.getRequest();
        }

        @Override
        public ServletResponse getResponse() {
            return delegate.getResponse();
        }

        @Override
        public boolean hasOriginalRequestAndResponse() {
            return delegate.hasOriginalRequestAndResponse();
        }

        @Override
        public void dispatch() {
            delegate.dispatch();
        }

        @Override
        public void dispatch(final String s) {
            delegate.dispatch(s);
        }

        @Override
        public void dispatch(final ServletContext servletContext, final String s) {
            delegate.dispatch(servletContext, s);
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
