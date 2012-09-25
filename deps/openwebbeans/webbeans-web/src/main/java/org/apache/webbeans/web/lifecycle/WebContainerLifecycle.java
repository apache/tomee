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
package org.apache.webbeans.web.lifecycle;

import org.apache.webbeans.component.InjectionPointBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.lifecycle.AbstractLifeCycle;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.web.context.WebContextsService;
import org.apache.webbeans.web.util.ServletCompatibilityUtil;

import javax.el.ELResolver;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages container lifecycle.
 * 
 * <p>
 * Behaves according to the request, session, and application
 * contexts of the web application. 
 * </p>
 * 
 * @version $Rev: 911764 $ $Date: 2010-02-19 11:52:54 +0200 (Fri, 19 Feb 2010) $
 * @see org.apache.webbeans.servlet.WebBeansConfigurationListener
 */
public final class WebContainerLifecycle extends AbstractLifeCycle
{
    /**Manages unused conversations*/
    private ScheduledExecutorService service = null;


    /**
     * Creates a new lifecycle instance and initializes
     * the instance variables.
     */
    public WebContainerLifecycle()
    {
        super(null);
        this.logger = WebBeansLoggerFacade.getLogger(WebContainerLifecycle.class);
    }

    /**
     * Creates a new lifecycle instance and initializes
     * the instance variables.
     */
    public WebContainerLifecycle(WebBeansContext webBeansContext)
    {
        super(null, webBeansContext);
        this.logger = WebBeansLoggerFacade.getLogger(WebContainerLifecycle.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void startApplication(Object startupObject)
    {
        ServletContext servletContext = getServletContext(startupObject);
        super.startApplication(servletContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopApplication(Object endObject)
    {
        ServletContext servletContext = getServletContext(endObject);
        super.stopApplication(servletContext);
    }

    /**
     * {@inheritDoc}
     */
    protected void afterStartApplication(final Object startupObject)
    {
        String strDelay = getWebBeansContext().getOpenWebBeansConfiguration().getProperty(OpenWebBeansConfiguration.CONVERSATION_PERIODIC_DELAY,"150000");
        long delay = Long.parseLong(strDelay);

        service = Executors.newScheduledThreadPool(1, new ThreadFactory()
        {            
            public Thread newThread(Runnable runable)
            {
              Thread t = new Thread(runable, "OwbConversationCleaner-"
                  + ServletCompatibilityUtil.getServletInfo((ServletContext) (startupObject)));
                t.setDaemon(true);
                return t;                
            }
        });
        service.scheduleWithFixedDelay(new ConversationCleaner(), delay, delay, TimeUnit.MILLISECONDS);

        ELAdaptor elAdaptor = getWebBeansContext().getService(ELAdaptor.class);
        ELResolver resolver = elAdaptor.getOwbELResolver();
        //Application is configured as JSP
        if(getWebBeansContext().getOpenWebBeansConfiguration().isJspApplication())
        {
            logger.log(Level.FINE, "Application is configured as JSP. Adding EL Resolver.");
            
            JspFactory factory = JspFactory.getDefaultFactory();
            if (factory != null) 
            {
                JspApplicationContext applicationCtx = factory.getJspApplicationContext((ServletContext)(startupObject));
                applicationCtx.addELResolver(resolver);                
            }            
            else
            {
                logger.log(Level.FINE, "Default JSPFactroy instance has not found");
            }
        }

        // Add BeanManager to the 'javax.enterprise.inject.spi.BeanManager' servlet context attribute
        ServletContext servletContext = (ServletContext)(startupObject);
        servletContext.setAttribute(BeanManager.class.getName(), getBeanManager());

    }

    protected void beforeStartApplication(Object startupObject)
    {
        this.scannerService.init(startupObject);
    }


    /**
     * {@inheritDoc}
     */
    protected void beforeStopApplication(Object stopObject)
    {
        if(service != null)
        {
            service.shutdownNow();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void afterStopApplication(Object stopObject)
    {
        ServletContext servletContext;

        if(stopObject instanceof ServletContext)
        {
            servletContext = (ServletContext)stopObject;
        }
        else
        {
            servletContext = getServletContext(stopObject);
        }

        //Clear the resource injection service
        ResourceInjectionService injectionServices = getWebBeansContext().getService(ResourceInjectionService.class);
        if(injectionServices != null)
        {
            injectionServices.clear();
        }

        //Comment out for commit OWB-502
        //ContextFactory.cleanUpContextFactory();

        this.cleanupShutdownThreadLocals();
        
        if (logger.isLoggable(Level.INFO))
        {
          logger.log(Level.INFO, OWBLogConst.INFO_0002, ServletCompatibilityUtil.getServletInfo(servletContext));
        }
    }

  /**
     * Ensures that all ThreadLocals, which could have been set in this
     * (shutdown-) Thread, are removed in order to prevent memory leaks.
     */
    private void cleanupShutdownThreadLocals()
    {
        InjectionPointBean.removeThreadLocal();
        WebContextsService.removeThreadLocals();
    }
    
    /**
     * Returns servelt context otherwise throws exception.
     * @param object object
     * @return servlet context
     */
    private ServletContext getServletContext(Object object)
    {
        if(object != null)
        {
            if(object instanceof ServletContextEvent)
            {
                object = ((ServletContextEvent) object).getServletContext();
                return (ServletContext)object;
            }
            else
            {
                throw new WebBeansException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0002));
            }
        }                
        
        throw new IllegalArgumentException("ServletContextEvent object but found null");
    }
    
    /**
     * Conversation cleaner thread, that
     * clears unused conversations.
     *
     */
    private static class ConversationCleaner implements Runnable
    {
        public ConversationCleaner()
        {

        }

        public void run()
        {
            WebBeansContext.getInstance().getConversationManager().destroyWithRespectToTimout();

        }
    }
}
