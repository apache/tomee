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
package org.apache.webbeans.context;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.inject.Singleton;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.config.WebBeansContext;

/**
 * JSR-299 based standard context
 * related operations.
 * @deprecated user should use the ContextsService directly.
 */
public final class ContextFactory
{
    /**Logger instance*/
    private static final Logger logger = WebBeansLoggerFacade.getLogger(ContextFactory.class);
    private final WebBeansContext webBeansContext;

    private ContextsService contextsService = null;

    public ContextFactory(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    /**
     * @return the ContextService for the current ClassLoader
     */
    private ContextsService getContextsService()
    {
        if (contextsService == null)
        {
            contextsService = webBeansContext.getService(ContextsService.class);
        }
        return contextsService;
    }

    public void initRequestContext(Object request)
    {
        try
        {
            ContextsService contextService = getContextsService();
            contextService.startContext(RequestScoped.class, request);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public Context getCustomContext(Context context)
    {
        if (webBeansContext.getBeanManagerImpl().isPassivatingScope(context.getScope()))
        {
            return new CustomPassivatingContextImpl(context);
        }

        return new CustomContextImpl(context);
    }

    public void destroyRequestContext(Object request)
    {
        ContextsService contextService = getContextsService();
        contextService.endContext(RequestScoped.class, request);
    }

    public void initSessionContext(Object session)
    {
        try
        {
            ContextsService contextService = getContextsService();
            contextService.startContext(SessionScoped.class, session);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void destroySessionContext(Object session)
    {
        ContextsService contextService = getContextsService();
        contextService.endContext(SessionScoped.class, session);
    }

    public void initApplicationContext(Object parameter)
    {
        try
        {
            ContextsService contextService = getContextsService();
            contextService.startContext(ApplicationScoped.class, parameter);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Destroys the application context and all of its components at the end of
     * the application.
     *
     * @param parameter parameter object
     */
    public void destroyApplicationContext(Object parameter)
    {
        ContextsService contextService = getContextsService();
        contextService.endContext(ApplicationScoped.class, parameter);
    }

    public void initSingletonContext(Object parameter)
    {
        try
        {
            ContextsService contextService = getContextsService();
            contextService.startContext(Singleton.class, parameter);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void destroySingletonContext(Object parameter)
    {
        ContextsService contextService = getContextsService();
        contextService.endContext(Singleton.class, parameter);
    }

    public void initConversationContext(Object context)
    {
        try
        {
            ContextsService contextService = getContextsService();
            contextService.startContext(ConversationScoped.class, context);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void destroyConversationContext()
    {
        ContextsService contextService = getContextsService();
        contextService.endContext(ConversationScoped.class, null);
    }

    /**
     * Gets the standard context with given scope type.
     *
     * @return the current context, or <code>null</code> if no standard context exists for the given scopeType
     */
    public Context getStandardContext(Class<? extends Annotation> scopeType)
    {
        ContextsService contextService = getContextsService();

        return contextService.getCurrentContext(scopeType);
    }

    /**
     * Activate context.
     */
    public void activateContext(Class<? extends Annotation> scopeType)
    {
        ContextsService contextService = getContextsService();
        contextService.activateContext(scopeType);
    }

    /**
     * Deactivate context.
     */
    public void deActivateContext(Class<? extends Annotation> scopeType)
    {
        ContextsService contextService = getContextsService();
        contextService.deActivateContext(scopeType);
    }


}
