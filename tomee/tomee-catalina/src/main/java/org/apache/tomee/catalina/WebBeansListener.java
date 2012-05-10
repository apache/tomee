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
package org.apache.tomee.catalina;

import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.apache.openejb.cdi.WebappWebBeansContext;
import org.apache.webbeans.component.InjectionPointBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.logger.WebBeansLogger;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.web.context.WebContextsService;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @version $Rev$ $Date$
 */
public class WebBeansListener implements ServletContextListener, ServletRequestListener, HttpSessionListener, HttpSessionActivationListener {

    private final String contextKey = this.getClass().getName() + "@" + hashCode();

    /**
     * Logger instance
     */
    private static final WebBeansLogger logger = WebBeansLogger.getLogger(WebBeansListener.class);

    protected FailOverService failoverService;

    /**
     * Manages the container lifecycle
     */
    protected WebBeansContext webBeansContext;

    /**
     * Default constructor
     *
     * @param webBeansContext
     */
    public WebBeansListener(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
        this.failoverService = this.webBeansContext.getService(FailOverService.class);
    }

    /**
     * {@inheritDoc}
     */
    public void requestDestroyed(ServletRequestEvent event) {
        if (logger.wblWillLogDebug()) {
            logger.debug("Destroying a request : [{0}]", event.getServletRequest().getRemoteAddr());
        }

        final Object oldContext = event.getServletRequest().getAttribute(contextKey);

        try {
            if (failoverService != null &&
                    failoverService.isSupportFailOver()) {
                Object request = event.getServletRequest();
                if (request instanceof HttpServletRequest) {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;
                    HttpSession session = httpRequest.getSession(false);
                    if (session != null) {
                        failoverService.sessionIsIdle(session);
                    }
                }
            }

            // clean up the EL caches after each request
            ELContextStore elStore = ELContextStore.getInstance(false);
            if (elStore != null) {
                elStore.destroyELContextStore();
            }


            webBeansContext.getContextsService().endContext(RequestScoped.class, event);
            if (webBeansContext instanceof WebappWebBeansContext) {
                ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().endContext(RequestScoped.class, event);
            }
            cleanupRequestThreadLocals();
        } finally {
            ThreadSingletonServiceImpl.enter((WebBeansContext) oldContext);
        }
    }

    /**
     * Ensures that all ThreadLocals, which could have been set in this
     * request's Thread, are removed in order to prevent memory leaks.
     */
    private void cleanupRequestThreadLocals() {
        // TODO maybe there are more to cleanup

        InjectionPointBean.removeThreadLocal();
        WebContextsService.removeThreadLocals();
    }

    /**
     * {@inheritDoc}
     */
    public void requestInitialized(ServletRequestEvent event) {
        final Object oldContext = ThreadSingletonServiceImpl.enter(this.webBeansContext);
        event.getServletRequest().setAttribute(contextKey, oldContext);

        try {
            if (logger.wblWillLogDebug()) {
                logger.debug("Starting a new request : [{0}]", event.getServletRequest().getRemoteAddr());
            }

            this.webBeansContext.getContextsService().startContext(RequestScoped.class, event);
            if (webBeansContext instanceof WebappWebBeansContext) {
                ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().startContext(RequestScoped.class, event);
            }

            // we don't initialise the Session here but do it lazily if it gets requested
            // the first time. See OWB-457

        } catch (Exception e) {
            logger.error(OWBLogConst.ERROR_0019, event.getServletRequest());
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sessionCreated(HttpSessionEvent event) {
        try {
            if (logger.wblWillLogDebug()) {
                logger.debug("Starting a session with session id : [{0}]", event.getSession().getId());
            }
            this.webBeansContext.getContextsService().startContext(SessionScoped.class, event.getSession());
            if (webBeansContext instanceof WebappWebBeansContext) {
                ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().startContext(SessionScoped.class, event.getSession());
            }
        } catch (Exception e) {
            logger.error(OWBLogConst.ERROR_0020, event.getSession());
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        if (logger.wblWillLogDebug()) {
            logger.debug("Destroying a session with session id : [{0}]", event.getSession().getId());
        }

        this.webBeansContext.getContextsService().endContext(SessionScoped.class, event.getSession());
        if (webBeansContext instanceof WebappWebBeansContext) {
            ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().endContext(SessionScoped.class, event.getSession());
        }

        ConversationManager conversationManager = webBeansContext.getConversationManager();
        conversationManager.destroyConversationContextWithSessionId(event.getSession().getId());
    }


    @Override
    public void sessionWillPassivate(HttpSessionEvent event) {
        if (failoverService != null &&
                failoverService.isSupportPassivation()) {
            HttpSession session = event.getSession();
            failoverService.sessionWillPassivate(session);
        }

    }

    @Override
    public void sessionDidActivate(HttpSessionEvent event) {
        if (failoverService.isSupportFailOver() || failoverService.isSupportPassivation()) {
            HttpSession session = event.getSession();
            failoverService.restoreBeans(session);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            OpenEJBLifecycle.initializeServletContext(servletContextEvent.getServletContext(), webBeansContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
