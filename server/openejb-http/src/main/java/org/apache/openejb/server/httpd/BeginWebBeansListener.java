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

import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.apache.openejb.cdi.WebappWebBeansContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.util.WebBeansUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * @version $Rev$ $Date$
 */
public class BeginWebBeansListener implements ServletContextListener, ServletRequestListener, HttpSessionListener, HttpSessionActivationListener {

    private final String contextKey;

    /**
     * Logger instance
     */
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CDI, BeginWebBeansListener.class);

    private final CdiAppContextsService contextsService;

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
        this.contextsService = webBeansContext != null ? CdiAppContextsService.class.cast(webBeansContext.getService(ContextsService.class)) : null;
        this.contextKey = "org.apache.tomee.catalina.WebBeansListener@" + webBeansContext.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestDestroyed(final ServletRequestEvent event) {
        if (webBeansContext == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Destroying a request : [{0}]", event == null ? "null" : event.getServletRequest().getRemoteAddr());
        }

        final Object oldContext;
        if (event != null) {
            oldContext = event.getServletRequest().getAttribute(contextKey);
        } else {
            oldContext = null;
        }

        try {
            // clean up the EL caches after each request
            final ELContextStore elStore = ELContextStore.getInstance(false);
            if (elStore != null) {
                elStore.destroyELContextStore();
            }

            contextsService.endContext(RequestScoped.class, event);
            if (webBeansContext instanceof WebappWebBeansContext) { // end after child
                ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().endContext(RequestScoped.class, event);
            }
        } finally {
            contextsService.removeThreadLocals();
            ThreadSingletonServiceImpl.enter((WebBeansContext) oldContext);
        }
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
            contextsService.startContext(RequestScoped.class, event);

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
            contextsService.startContext(SessionScoped.class, event.getSession());
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
        if (webBeansContext == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Destroying a session with session id : [{0}]", event.getSession().getId());
        }

        contextsService.endContext(SessionScoped.class, event.getSession());

        WebBeansListenerHelper.destroyFakedRequest(this);
    }

    @Override
    public void sessionWillPassivate(final HttpSessionEvent event) {
        WebBeansListenerHelper.ensureRequestScope(contextsService, this);
    }

    @Override
    public void sessionDidActivate(final HttpSessionEvent event) {
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            OpenEJBLifecycle.initializeServletContext(servletContextEvent.getServletContext(), webBeansContext);
        } catch (final Exception e) {
            logger.warning(e.getMessage(), e);
        }
        WebBeansListenerHelper.ensureRequestScope(contextsService, this);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        WebBeansListenerHelper.destroyFakedRequest(this);
        final WebContext wc = WebContext.class.cast(servletContextEvent.getServletContext().getAttribute("openejb.web.context"));
        if (wc != null) {
            wc.release();
        }
    }

}
