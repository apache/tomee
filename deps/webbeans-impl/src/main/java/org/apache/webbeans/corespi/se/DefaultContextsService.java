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
package org.apache.webbeans.corespi.se;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.inject.Singleton;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.AbstractContextsService;
import org.apache.webbeans.context.ApplicationContext;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.context.DependentContext;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.context.SingletonContext;

public class DefaultContextsService extends AbstractContextsService
{
    private static ThreadLocal<RequestContext> requestContext = null;

    private static ThreadLocal<SessionContext> sessionContext = null;

    private ApplicationContext applicationContext = null;

    private static ThreadLocal<ConversationContext> conversationContext = null;
    
    private static ThreadLocal<SingletonContext> singletonContext = null;

    private static ThreadLocal<DependentContext> dependentContext = null;


    static
    {
        requestContext = new ThreadLocal<RequestContext>();
        sessionContext = new ThreadLocal<SessionContext>();
        conversationContext = new ThreadLocal<ConversationContext>();
        dependentContext = new ThreadLocal<DependentContext>();
        singletonContext = new ThreadLocal<SingletonContext>();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endContext(Class<? extends Annotation> scopeType, Object endParameters)
    {
        
        if(supportsContext(scopeType))
        {
            if(scopeType.equals(RequestScoped.class))
            {
                stopRequestContext(endParameters);
            }
            else if(scopeType.equals(SessionScoped.class))
            {
                stopSessionContext(endParameters);
            }
            else if(scopeType.equals(ApplicationScoped.class))
            {
                stopApplicationContext(endParameters);
            }
            else if(scopeType.equals(ConversationScoped.class))
            {
                stopConversationContext(endParameters);
            }
            else if(scopeType.equals(Dependent.class))
            {
                //Do nothing
            }
            else
            {
                stopSingletonContext(endParameters);
            }
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context getCurrentContext(Class<? extends Annotation> scopeType)
    {
        if(scopeType.equals(RequestScoped.class))
        {
            return getCurrentRequestContext();
        }
        else if(scopeType.equals(SessionScoped.class))
        {
            return getCurrentSessionContext();
        }
        else if(scopeType.equals(ApplicationScoped.class))
        {
            return getCurrentApplicationContext();
        }
        else if(scopeType.equals(ConversationScoped.class))
        {
            return getCurrentConversationContext();
        }
        else if(scopeType.equals(Dependent.class))
        {
            return getCurrentDependentContext();
        }
        else if(scopeType.equals(Singleton.class))
        {
            return getCurrentSingletonContext();
        }

        return null;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException
    {
        try
        {
            if(scopeType.equals(RequestScoped.class))
            {
                startRequestContext(startParameter);
            }
            else if(scopeType.equals(SessionScoped.class))
            {
                startSessionContext(startParameter);
            }
            else if(scopeType.equals(ApplicationScoped.class))
            {
                startApplicationContext(startParameter);
            }
            else if(scopeType.equals(ConversationScoped.class))
            {
                startConversationContext((ConversationContext)startParameter);
            }
            else if(scopeType.equals(Dependent.class))
            {
                //Do nothing
            }
            else if(scopeType.equals(Singleton.class))
            {
                startSingletonContext(startParameter);
            }
        }
        catch(Exception e)
        {
            if(e instanceof ContextException)
            {
                throw (ContextException)e;
            }
            
            throw new ContextException(e);
        }        
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsContext(Class<? extends Annotation> scopeType)
    {
        
        if(scopeType.equals(RequestScoped.class) ||
                scopeType.equals(SessionScoped.class) ||
                scopeType.equals(ApplicationScoped.class) ||
                scopeType.equals(ConversationScoped.class) ||
                scopeType.equals(Dependent.class) ||
                scopeType.equals(Singleton.class))
        {
            return true;
        }
        
        return false;

    }


    @Override
    public void destroy(Object destroyObject)
    {
        requestContext.set(null);
        sessionContext.set(null);
        conversationContext.set(null);
        dependentContext.set(null);
        singletonContext.set(null);
        
        requestContext.remove();
        sessionContext.remove();
        conversationContext.remove();
        dependentContext.remove();
        singletonContext.remove();
    }
    
    
    private Context getCurrentApplicationContext()
    {        
        return applicationContext;
    }

    
    private Context getCurrentConversationContext()
    {        
        return conversationContext.get();
    }

    
    private Context getCurrentDependentContext()
    {        
        if(dependentContext.get() == null)
        {
            dependentContext.set(new DependentContext());
        }
        
        return dependentContext.get();
    }

    
    private Context getCurrentRequestContext()
    {        
        return requestContext.get();
    }

    
    private Context getCurrentSessionContext()
    {        
        return sessionContext.get();
    }

    
    private Context getCurrentSingletonContext()
    {        
        return singletonContext.get();
    }

    
    private void startApplicationContext(Object instance)
    {
        ApplicationContext ctx = new ApplicationContext();
        ctx.setActive(true);
        
        applicationContext = ctx;
    }

    
    private void startConversationContext(Object object)
    {
        ConversationContext ctx = new ConversationContext();
        ctx.setActive(true);
        
        conversationContext.set(ctx);
        
    }

    
    private void startRequestContext(Object instance)
    {
        
        RequestContext ctx = new RequestContext();
        ctx.setActive(true);
        
        requestContext.set(ctx);
    }

    
    private void startSessionContext(Object instance)
    {
        SessionContext ctx = new SessionContext();
        ctx.setActive(true);
        
        sessionContext.set(ctx);
    }

    
    private void startSingletonContext(Object object)
    {
        
        SingletonContext ctx = new SingletonContext();
        ctx.setActive(true);
        
        singletonContext.set(ctx);
    }

    
    private void stopApplicationContext(Object object)
    {
        if(applicationContext != null)
        {
            applicationContext.destroy();
            applicationContext = null;

            // this is needed to get rid of ApplicationScoped beans which are cached inside the proxies...
            WebBeansContext.currentInstance().getBeanManagerImpl().clearCacheProxies();
        }
    }

    
    private void stopConversationContext(Object object)
    {
        if(conversationContext.get() != null)
        {
            conversationContext.get().destroy();   
        }

        conversationContext.set(null);
        conversationContext.remove();
    }

    
    private void stopRequestContext(Object instance)
    {        
        if(requestContext.get() != null)
        {
            requestContext.get().destroy();   
        }

        requestContext.set(null);
        requestContext.remove();
    }

    
    private void stopSessionContext(Object instance)
    {
        if(sessionContext.get() != null)
        {
            sessionContext.get().destroy();   
        }

        sessionContext.set(null);
        sessionContext.remove();
    }

    
    private void stopSingletonContext(Object object)
    {
        if(singletonContext.get() != null)
        {
            singletonContext.get().destroy();   
        }

        singletonContext.set(null);
        singletonContext.remove();
    }

}
