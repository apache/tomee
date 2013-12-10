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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.httpd;

import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.apache.openejb.cdi.WebappWebBeansContext;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @version $Rev$ $Date$
 */
public class BeginWebBeansListener implements ServletContextListener, ServletRequestListener, HttpSessionListener, HttpSessionActivationListener {

    private final String contextKey;

    /**
     * Logger instance
     */
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CDI, BeginWebBeansListener.class);

    protected FailOverService failoverService;

    /**
     * Manages the container lifecycle
     */
    protected WebBeansContext webBeansContext;

    /**
     * Default constructor
     *
     * @param webBeansContext the OWB context
     */
    public BeginWebBeansListener(final WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
        this.failoverService = this.webBeansContext.getService(FailOverService.class);
        this.contextKey = "org.apache.tomee.catalina.WebBeansListener@" + webBeansContext.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestDestroyed(final ServletRequestEvent event) {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestInitialized(final ServletRequestEvent event) {
        final Object oldContext = ThreadSingletonServiceImpl.enter(this.webBeansContext);
        if (event != null) {
            event.getServletRequest().setAttribute(contextKey, oldContext);
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting a new request : [{0}]", event == null ? "null" : event.getServletRequest().getRemoteAddr());
            }

            if (webBeansContext instanceof WebappWebBeansContext) { // start before child
                ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().startContext(RequestScoped.class, event);
            }
            this.webBeansContext.getContextsService().startContext(RequestScoped.class, event);

            // we don't initialise the Session here but do it lazily if it gets requested
            // the first time. See OWB-457

        } catch (final Exception e) {
            logger.error(OWBLogConst.ERROR_0019, event == null ? "null" : event.getServletRequest());
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting a session with session id : [{0}]", event.getSession().getId());
            }
            if (webBeansContext instanceof WebappWebBeansContext) { // start before child
                ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().startContext(SessionScoped.class, event.getSession());
            }
            this.webBeansContext.getContextsService().startContext(SessionScoped.class, event.getSession());
        } catch (final Exception e) {
            logger.error(OWBLogConst.ERROR_0020, event.getSession());
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        ensureRequestScope();
    }

    private void ensureRequestScope() {
        final Context reqCtx = webBeansContext.getContextsService().getCurrentContext(RequestScoped.class);
        if (reqCtx == null || !webBeansContext.getContextsService().getCurrentContext(RequestScoped.class).isActive()) {
            requestInitialized(null);
            EndWebBeansListener.FAKE_REQUEST.set(true);
        }
    }

    @Override
    public void sessionWillPassivate(final HttpSessionEvent event) {
        ensureRequestScope();
    }

    @Override
    public void sessionDidActivate(final HttpSessionEvent event) {
        if (failoverService.isSupportFailOver() || failoverService.isSupportPassivation()) {
            failoverService.sessionDidActivate(event.getSession());
        }
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            OpenEJBLifecycle.initializeServletContext(servletContextEvent.getServletContext(), webBeansContext);
        } catch (final Exception e) {
            logger.warning(e.getMessage(), e);
        }
        ensureRequestScope();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        ensureRequestScope();
    }
}
