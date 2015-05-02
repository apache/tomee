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
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @version $Rev$ $Date$
 *          <p/>
 *          Used as a stack executed at the end of the request too. Avoid multiple (useless) listeners.
 */
public class EndWebBeansListener implements ServletContextListener, ServletRequestListener, HttpSessionListener, HttpSessionActivationListener {

    /**
     * Manages the container lifecycle
     */
    protected WebBeansContext webBeansContext;
    private final CdiAppContextsService contextsService;

    /**
     * Default constructor
     *
     * @param webBeansContext the OWB context
     */
    public EndWebBeansListener(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
        if (webBeansContext != null) {
            this.contextsService = CdiAppContextsService.class.cast(webBeansContext.getService(ContextsService.class));
        } else {
            this.contextsService = null;
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
        WebBeansListenerHelper.ensureRequestScope(contextsService, this);
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent event) {
        if (webBeansContext == null) {
            return;
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
        WebBeansListenerHelper.ensureRequestScope(contextsService, this);
    }
}
