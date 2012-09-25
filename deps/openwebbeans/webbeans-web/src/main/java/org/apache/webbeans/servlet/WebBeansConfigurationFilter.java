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
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.web.context.WebContextsService;
import org.apache.webbeans.web.util.ServletCompatibilityUtil;

import javax.enterprise.context.RequestScoped;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializing the beans container for using in a web application
 * environment. Caution: Only for buggy containers that do not fire
 * properly ServletRequestEvents or in pre-Servlet 2.5 environments.
 * @see WebBeansConfigurationListener
 *
 * This is the alternative entry point for starting the CDI container
 * for a servlet.
 *
 */
public class WebBeansConfigurationFilter implements Filter
{

    private static final String CALL_COUNT_ATTRIBUTE_NAME = WebBeansConfigurationFilter.class.getName();

    /**Logger instance*/
    private static final Logger logger = WebBeansLoggerFacade.getLogger(WebBeansConfigurationFilter.class);

    /**Manages the container lifecycle*/
    protected ContainerLifecycle lifeCycle = null;

    protected FailOverService failoverService = null;
    private WebBeansContext webBeansContext;
    private ServletContext servletContext;

    /**
     * Default constructor
     */
    public WebBeansConfigurationFilter()
    {
        webBeansContext = WebBeansContext.getInstance();
        failoverService = webBeansContext.getService(FailOverService.class);
    }

    /**
     * {@inheritDoc}
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.lifeCycle = webBeansContext.getService(ContainerLifecycle.class);

        try
        {
            this.servletContext = filterConfig.getServletContext();
            this.lifeCycle.startApplication(new ServletContextEvent(this.servletContext));

        }
        catch (Exception e)
        {
             logger.log(Level.SEVERE,
                     WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0018, ServletCompatibilityUtil.getServletInfo(servletContext)));
             WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        try
        {
            if (firstInit(servletRequest))
            {
                requestInitialized(servletRequest);
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }
        finally
        {
            if (lastDestroy(servletRequest))
            {
                 requestDestroyed(servletRequest);
            }
        }
    }

    private boolean firstInit(ServletRequest servletRequest)
    {
        Integer callCount = (Integer) servletRequest.getAttribute(CALL_COUNT_ATTRIBUTE_NAME);
        if (callCount == null)
        {
            callCount = 0;
        }
        callCount++;
        servletRequest.setAttribute(CALL_COUNT_ATTRIBUTE_NAME, callCount);

        return callCount == 1;
    }

    private boolean lastDestroy(ServletRequest servletRequest)
    {
        Integer callCount = (Integer) servletRequest.getAttribute(CALL_COUNT_ATTRIBUTE_NAME);
        callCount--;
        servletRequest.setAttribute(CALL_COUNT_ATTRIBUTE_NAME, callCount);
        return callCount == 0;
    }

    public void requestInitialized(ServletRequest servletRequest)
    {
        try
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.log(Level.FINE, "Starting a new request : [{0}]", servletRequest.getRemoteAddr());
            }

            this.lifeCycle.getContextService().startContext(RequestScoped.class,
                    new ServletRequestEvent(this.servletContext, servletRequest));

            // we don't initialise the Session here but do it lazily if it gets requested
            // the first time. See OWB-457
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE,
                    WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0019, servletRequest));
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    public void requestDestroyed(ServletRequest request)
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, "Destroying a request : [{0}]", request.getRemoteAddr());
        }

        if (failoverService != null &&
                failoverService.isSupportFailOver())
        {
            if(request instanceof HttpServletRequest)
            {
                HttpServletRequest httpRequest = (HttpServletRequest)request;
                HttpSession session = httpRequest.getSession(false);
                if (session != null)
                {
                    failoverService.sessionIsIdle(session);
                }
            }
        }

        // clean up the EL caches after each request
        ELContextStore elStore = ELContextStore.getInstance(false);
        if (elStore != null)
        {
            elStore.destroyELContextStore();
        }

        this.lifeCycle.getContextService().endContext(RequestScoped.class,
                new ServletRequestEvent(this.servletContext, request));

        this.cleanupRequestThreadLocals();
    }

    /**
     * {@inheritDoc}
     */
    public void destroy()
    {
        this.lifeCycle.stopApplication(new ServletContextEvent(this.servletContext));
        this.lifeCycle = null;
        this.servletContext = null;

        // just to be sure that we didn't lazily create anything...
        cleanupRequestThreadLocals();
    }

    /**
     * Ensures that all ThreadLocals, which could have been set in this
     * requests Thread, are removed in order to prevent memory leaks.
     */
    private void cleanupRequestThreadLocals()
    {
        // TODO maybe there are more to cleanup

        InjectionPointBean.removeThreadLocal();
        WebContextsService.removeThreadLocals();
    }
}
