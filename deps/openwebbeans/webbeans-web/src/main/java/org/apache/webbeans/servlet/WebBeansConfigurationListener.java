/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.servlet;

import org.apache.webbeans.component.InjectionPointBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.web.context.WebContextsService;
import org.apache.webbeans.web.util.ServletCompatibilityUtil;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializing the beans container for using in an web application
 * environment.
 *
 * This is the main entry point for starting the CDI container
 * for a servlet.
 *
 *
 * If you have a container with &lt; Servlet-2.5 then use
 * {@link WebBeansConfigurationFilter} and {@link WebBeansConfigurationHttpSessionListener}
 * instead.
 */
public class WebBeansConfigurationListener implements ServletContextListener, ServletRequestListener, HttpSessionListener
{
    /**Logger instance*/
    private static final Logger logger = WebBeansLoggerFacade.getLogger(WebBeansConfigurationListener.class);
    
    /**Manages the container lifecycle*/
    protected ContainerLifecycle lifeCycle = null;
    
    protected FailOverService failoverService = null;
    private WebBeansContext webBeansContext;

    /**
     * Default constructor
     */
    public WebBeansConfigurationListener()
    {
        webBeansContext = WebBeansContext.getInstance();
        failoverService = webBeansContext.getService(FailOverService.class);
    }

    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent event)
    {
        this.lifeCycle = webBeansContext.getService(ContainerLifecycle.class);

        try
        {
                this.lifeCycle.startApplication(event);
        }
        catch (Exception e)
        {
             logger.log(Level.SEVERE,
                     WebBeansLoggerFacade.constructMessage(
                             OWBLogConst.ERROR_0018,
                             ServletCompatibilityUtil.getServletInfo(event.getServletContext())));
             WebBeansUtil.throwRuntimeExceptions(e);
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent event)
    {
        this.lifeCycle.stopApplication(event);
        this.lifeCycle = null;

        // just to be sure that we didn't lazily create anything...
        cleanupRequestThreadLocals();
    }

    /**
     * {@inheritDoc}
     */
    public void requestDestroyed(ServletRequestEvent event)
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, "Destroying a request : [{0}]", event.getServletRequest().getRemoteAddr());
        }
        
        // clean up the EL caches after each request
        ELContextStore elStore = ELContextStore.getInstance(false);
        if (elStore != null)
        {
            elStore.destroyELContextStore();
        }

        this.lifeCycle.getContextService().endContext(RequestScoped.class, event);

        this.cleanupRequestThreadLocals();
    }

    /**
     * Ensures that all ThreadLocals, which could have been set in this
     * requests Thread, are removed in order to prevent memory leaks.
     */
    private void cleanupRequestThreadLocals()
    {
        InjectionPointBean.removeThreadLocal();
        WebContextsService.removeThreadLocals();
    }

    /**
     * {@inheritDoc}
     */
    public void requestInitialized(ServletRequestEvent event)
    {
        try
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.log(Level.FINE, "Starting a new request : [{0}]", event.getServletRequest().getRemoteAddr());
            }
            
            this.lifeCycle.getContextService().startContext(RequestScoped.class, event);

            // we don't initialise the Session here but do it lazily if it gets requested
            // the first time. See OWB-457
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE,
                    WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0019, event.getServletRequest()));
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sessionCreated(HttpSessionEvent event)
    {
        try
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.log(Level.FINE, "Starting a session with session id : [{0}]", event.getSession().getId());
            }
            this.lifeCycle.getContextService().startContext(SessionScoped.class, event.getSession());
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE,
                    WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0020, event.getSession()));
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sessionDestroyed(HttpSessionEvent event)
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, "Destroying a session with session id : [{0}]", event.getSession().getId());
        }
        this.lifeCycle.getContextService().endContext(SessionScoped.class, event.getSession());

        ConversationManager conversationManager = WebBeansContext.getInstance().getConversationManager();
        conversationManager.destroyConversationContextWithSessionId(event.getSession().getId());
    }
}
