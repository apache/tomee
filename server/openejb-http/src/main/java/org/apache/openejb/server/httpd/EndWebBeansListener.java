/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * @version $Rev$ $Date$
 *
 *          Used as a stack executed at the end of the request too. Avoid multiple (useless) listeners.
 */
public class EndWebBeansListener implements ServletContextListener, ServletRequestListener, HttpSessionListener, HttpSessionActivationListener {

    /**
     * Manages the container lifecycle
     */
    protected WebBeansContext webBeansContext;
    private final CdiAppContextsService contextsService;
    private final boolean cleanUpSession;

    /**
     * Default constructor
     *
     * @param webBeansContext the OWB context
     */
    public EndWebBeansListener(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
        if (webBeansContext != null) {
            this.contextsService = CdiAppContextsService.class.cast(webBeansContext.getService(ContextsService.class));
            this.cleanUpSession = Boolean.parseBoolean(webBeansContext.getOpenWebBeansConfiguration()
                    .getProperty("tomee.session.remove-cdi-beans-on-passivate", "false"));
        } else {
            this.contextsService = null;
            this.cleanUpSession = false; // ignored anyway
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestInitialized(ServletRequestEvent event) {
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
    public void sessionDestroyed(final HttpSessionEvent event) {
        if (contextsService == null) {
            return;
        }
        WebBeansListenerHelper.ensureRequestScope(contextsService, this);
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent event) {
        if (webBeansContext == null) {
            return;
        }

        if (cleanUpSession) {
            event.getSession().removeAttribute("openWebBeansSessionContext");
        }
        WebBeansListenerHelper.destroyFakedRequest(this);
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent event) {
        // no-op
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        WebBeansListenerHelper.destroyFakedRequest(this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (contextsService == null) {
            return;
        }
        WebBeansListenerHelper.ensureRequestScope(contextsService, this);
    }
}
