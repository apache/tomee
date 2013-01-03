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

import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.apache.openejb.cdi.WebappWebBeansContext;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.component.InjectionPointBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.web.context.WebContextsService;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
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
public class EndWebBeansListener implements ServletRequestListener, HttpSessionListener, HttpSessionActivationListener {

    private final String contextKey;

    /**
     * Logger instance
     */
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CDI, EndWebBeansListener.class);

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
    public EndWebBeansListener(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
        this.failoverService = this.webBeansContext.getService(FailOverService.class);
        this.contextKey = "org.apache.tomee.catalina.WebBeansListener@" + webBeansContext.hashCode();
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
    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        if (logger.isDebugEnabled()) {
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
            final ELContextStore elStore = ELContextStore.getInstance(false);
            if (elStore != null) {
                elStore.destroyELContextStore();
            }


            webBeansContext.getContextsService().endContext(RequestScoped.class, event);
            if (webBeansContext instanceof WebappWebBeansContext) { // end after child
                ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().endContext(RequestScoped.class, event);
            }
            cleanupRequestThreadLocals();
        } finally {
            ThreadSingletonServiceImpl.enter((WebBeansContext) oldContext);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestInitialized(ServletRequestEvent event) {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying a session with session id : [{0}]", event.getSession().getId());
        }

        this.webBeansContext.getContextsService().endContext(SessionScoped.class, event.getSession());
        if (webBeansContext instanceof WebappWebBeansContext) { // end after child
            ((WebappWebBeansContext) webBeansContext).getParent().getContextsService().endContext(SessionScoped.class, event.getSession());
        }

        ConversationManager conversationManager = webBeansContext.getConversationManager();
        conversationManager.destroyConversationContextWithSessionId(event.getSession().getId());
    }


    @Override
    public void sessionWillPassivate(HttpSessionEvent event) {
        if (failoverService != null && failoverService.isSupportPassivation()) {
            failoverService.sessionWillPassivate(event.getSession());
        }
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent event) {
        // no-op
    }
}
