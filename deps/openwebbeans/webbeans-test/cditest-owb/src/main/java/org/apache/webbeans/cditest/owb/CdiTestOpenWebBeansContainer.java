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
package org.apache.webbeans.cditest.owb;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.ResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;

import org.apache.webbeans.cditest.CdiTestContainer;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;

/**
 * OpenWebBeans specific implementation of {@link CdiTestContainer}.
 */
public class CdiTestOpenWebBeansContainer implements CdiTestContainer 
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(CdiTestOpenWebBeansContainer.class);

    private ContainerLifecycle  lifecycle = null;
    private MockServletContext  servletContext = null;
    private MockHttpSession     session = null;

    public void bootContainer() throws Exception 
    {
        servletContext = new MockServletContext();
        session = new MockHttpSession();
        lifecycle = WebBeansContext.getInstance().getService(ContainerLifecycle.class);
        lifecycle.startApplication(new ServletContextEvent(servletContext));
    }

    public void shutdownContainer() throws Exception 
    {
        if (lifecycle != null) 
        {
            lifecycle.stopApplication(new ServletContextEvent(servletContext));
        }
    }

    public void startContexts() throws Exception 
    {
        logger.log(Level.FINE, "starting all OWB Contexts");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();

        contextsService.startContext(Singleton.class, servletContext);
        contextsService.startContext(ApplicationScoped.class, servletContext);
        contextsService.startContext(SessionScoped.class, session);
        contextsService.startContext(ConversationScoped.class, null);
        contextsService.startContext(RequestScoped.class, null);
    }

    public void startApplicationScope() throws Exception 
    {
        logger.log(Level.FINE, "starting the OWB ApplicationContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();

        contextsService.startContext(ApplicationScoped.class, servletContext);
    }

    public void startConversationScope() throws Exception
    {
        logger.log(Level.FINE, "starting the OWB ConversationContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();

        contextsService.startContext(ConversationScoped.class, null);
    }

    public void startCustomScope(Class<? extends Annotation> scopeClass) throws Exception 
    {
        //X TODO
    }

    public void startRequestScope() throws Exception 
    {
        logger.log(Level.FINE, "starting the OWB RequestContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();

        contextsService.startContext(RequestScoped.class, null);
    }

    public void startSessionScope() throws Exception 
    {
        logger.log(Level.FINE, "starting the OWB SessionContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();

        contextsService.startContext(SessionScoped.class, session);
    }

    public void stopContexts() throws Exception 
    {
        logger.log(Level.FINE, "stopping all OWB Contexts");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();

        stopSessionScope();
        stopConversationScope();
        stopRequestScope();
        stopApplicationScope();
        contextsService.endContext(Singleton.class, null);
    }

    public void stopApplicationScope() throws Exception 
    {
        logger.log(Level.FINE, "stopping the OWB ApplicationContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();
        contextsService.endContext(ApplicationScoped.class, servletContext);
    }

    public void stopConversationScope() throws Exception 
    {
        logger.log(Level.FINE, "stopping the OWB ConversationContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();
        contextsService.endContext(ConversationScoped.class, null);
    }

    public void stopCustomScope(Class<? extends Annotation> scopeClass) throws Exception 
    {
        //X TODO
    }

    public void stopRequestScope() throws Exception 
    {
        logger.log(Level.FINE, "stopping the OWB RequestContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();
        contextsService.endContext(RequestScoped.class, null);
    }

    public void stopSessionScope() throws Exception 
    {
        logger.log(Level.FINE, "stopping the OWB SessionContext");
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        ContextsService contextsService = webBeansContext.getContextsService();

        contextsService.endContext(SessionScoped.class, session);
    }
    
    public  BeanManager getBeanManager() 
    {
        return lifecycle.getBeanManager();
    }

    public <T> T getInstance(Class<T> type, Annotation... qualifiers)
    throws ResolutionException 
    {
        Set<Bean<?>> beans = getBeanManager().getBeans(type, qualifiers);
        Bean<?> bean = getBeanManager().resolve(beans);

        @SuppressWarnings("unchecked")
        T instance = (T) getBeanManager().getReference(bean, type, getBeanManager().createCreationalContext(bean));
        return instance;
    }

    public Object getInstance(String name)
    throws ResolutionException 
    {
        Set<Bean<?>> beans = getBeanManager().getBeans(name);
        Bean<?> bean = getBeanManager().resolve(beans);

        @SuppressWarnings("unchecked")
        Object instance = getBeanManager().getReference(bean, bean.getBeanClass(), getBeanManager().createCreationalContext(bean));
        return instance;
    }

}
