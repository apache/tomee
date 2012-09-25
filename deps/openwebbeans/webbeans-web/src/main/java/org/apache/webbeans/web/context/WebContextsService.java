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
package org.apache.webbeans.web.context;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.AbstractContextsService;
import org.apache.webbeans.context.ApplicationContext;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.context.DependentContext;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.context.SingletonContext;
import org.apache.webbeans.conversation.ConversationImpl;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.web.intercept.RequestScopedBeanInterceptorHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextException;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web container {@link org.apache.webbeans.spi.ContextsService}
 * implementation.
 */
public class WebContextsService extends AbstractContextsService
{
    /**Logger instance*/
    private static final Logger logger = WebBeansLoggerFacade.getLogger(WebContextsService.class);

    /**Current request context*/
    private static ThreadLocal<RequestContext> requestContexts = null;

    /**Current session context*/
    private static ThreadLocal<SessionContext> sessionContexts = null;

    /**Current application context*/
    private static ThreadLocal<ApplicationContext> applicationContexts = null;

    /**
     * This applicationContext will be used for all non servlet-request threads
     */
    private ApplicationContext sharedApplicationContext ;

    /**Current conversation context*/
    private static ThreadLocal<ConversationContext> conversationContexts = null;
    
    /**Current singleton context*/
    private static ThreadLocal<SingletonContext> singletonContexts = null;

    /**Current dependent context*/
    private static DependentContext dependentContext;

    /**Current application contexts*/
    private static Map<ServletContext, ApplicationContext> currentApplicationContexts = new ConcurrentHashMap<ServletContext, ApplicationContext>();
    
    /**Current singleton contexts*/
    private static Map<ServletContext, SingletonContext> currentSingletonContexts = new ConcurrentHashMap<ServletContext, SingletonContext>();

    /**Session context manager*/
    private final SessionContextManager sessionCtxManager = new SessionContextManager();

    /**Conversation context manager*/
    private final ConversationManager conversationManager;

    private boolean supportsConversation = false;
    
    protected FailOverService failoverService = null;

    private WebBeansContext webBeansContext;

    /**Initialize thread locals*/
    static
    {
        requestContexts = new ThreadLocal<RequestContext>();
        sessionContexts = new ThreadLocal<SessionContext>();
        applicationContexts = new ThreadLocal<ApplicationContext>();
        conversationContexts = new ThreadLocal<ConversationContext>();
        singletonContexts = new ThreadLocal<SingletonContext>();
        
        //Dependent context is always active
        dependentContext = new DependentContext();
        dependentContext.setActive(true);
    }

    /**
     * Removes the ThreadLocals from the ThreadMap to prevent memory leaks.
     */
    public static void removeThreadLocals()
    {
        requestContexts.remove();
        sessionContexts.remove();
        applicationContexts.remove();
        conversationContexts.remove();
        singletonContexts.remove();
        RequestScopedBeanInterceptorHandler.removeThreadLocals();
    }
    
    /**
     * Creates a new instance.
     */
    public WebContextsService(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        supportsConversation =  webBeansContext.getOpenWebBeansConfiguration().supportsConversation();
        failoverService = webBeansContext.getService(FailOverService.class);
        conversationManager = webBeansContext.getConversationManager();

        sharedApplicationContext = new ApplicationContext();
        sharedApplicationContext.setActive(true);
    }

    public SessionContextManager getSessionContextManager()
    {
        return sessionCtxManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Object initializeObject)
    {        
        //Start application context
        startContext(ApplicationScoped.class, initializeObject);
        
        //Start signelton context
        startContext(Singleton.class, initializeObject);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(Object destroyObject)
    {
        //Destroy application context
        endContext(ApplicationScoped.class, destroyObject);

        // we also need to destroy the shared ApplicationContext
        sharedApplicationContext.destroy();
        
        //Destroy singleton context
        endContext(Singleton.class, destroyObject);
     
        //Clear saved contexts related with 
        //this servlet context
        currentApplicationContexts.clear();
        currentSingletonContexts.clear();
        
        //Thread local values to null
        requestContexts.set(null);
        sessionContexts.set(null);
        conversationContexts.set(null);
        applicationContexts.set(null);
        singletonContexts.set(null);
        
        //Remove thread locals
        //for preventing memory leaks
        requestContexts.remove();
        sessionContexts.remove();
        conversationContexts.remove();
        applicationContexts.remove();
        singletonContexts.remove();
                
    }    
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endContext(Class<? extends Annotation> scopeType, Object endParameters)
    {        
        if(scopeType.equals(RequestScoped.class))
        {
            destroyRequestContext((ServletRequestEvent)endParameters);
        }
        else if(scopeType.equals(SessionScoped.class))
        {
            destroySessionContext((HttpSession)endParameters);
        }
        else if(scopeType.equals(ApplicationScoped.class))
        {
            destroyApplicationContext((ServletContext)endParameters);
        }
        else if(supportsConversation && scopeType.equals(ConversationScoped.class))
        {
            destroyConversationContext();
        }
        else if(scopeType.equals(Dependent.class))
        {
            //Do nothing
        }
        else if (scopeType.equals(Singleton.class))
        {
            destroySingletonContext((ServletContext)endParameters);
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
            return getRequestContext();
        }
        else if(scopeType.equals(SessionScoped.class))
        {
            return getSessionContext();
        }
        else if(scopeType.equals(ApplicationScoped.class))
        {
            return getApplicationContext();
        }
        else if(supportsConversation && scopeType.equals(ConversationScoped.class))
        {
            return getConversationContext();
        }
        else if(scopeType.equals(Dependent.class))
        {
            return dependentContext;
        }
        else if (scopeType.equals(Singleton.class))
        {
            return getSingletonContext();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException
    {
        if(scopeType.equals(RequestScoped.class))
        {
            initRequestContext((ServletRequestEvent)startParameter);
        }
        else if(scopeType.equals(SessionScoped.class))
        {
            initSessionContext((HttpSession)startParameter);
        }
        else if(scopeType.equals(ApplicationScoped.class))
        {
            initApplicationContext((ServletContext)startParameter);
        }
        else if(supportsConversation && scopeType.equals(ConversationScoped.class))
        {
            initConversationContext((ConversationContext)startParameter);
        }
        else if(scopeType.equals(Dependent.class))
        {
            //Do nothing
        }
        else if (scopeType.equals(Singleton.class))
        {
            initSingletonContext((ServletContext)startParameter);
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
                scopeType.equals(Dependent.class) ||
                scopeType.equals(Singleton.class) ||
                (scopeType.equals(ConversationScoped.class) && supportsConversation))
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * Initialize requext context with the given request object.
     * @param event http servlet request event
     */
    private void initRequestContext(ServletRequestEvent event)
    {
        
        RequestContext rq = new ServletRequestContext();
        rq.setActive(true);

        requestContexts.set(rq);// set thread local

        if(event != null)
        {
            HttpServletRequest request = (HttpServletRequest) event.getServletRequest();
            ((ServletRequestContext)rq).setServletRequest(request);
            
            if (request != null)
            {
                //Re-initialize thread local for session
                HttpSession session = request.getSession(false);
                
                if(session != null)
                {
                    initSessionContext(session);    
                }
                            
                //Init thread local application context
                initApplicationContext(event.getServletContext());
                
                //Init thread local sigleton context
                initSingletonContext(event.getServletContext());
            }            
        }
        else
        {
                //Init thread local application context
                initApplicationContext(null);

                //Init thread local sigleton context
                initSingletonContext(null);
        }
    }
    
    /**
     * Destroys the request context and all of its components. 
     * @param request http servlet request object
     */
    private void destroyRequestContext(ServletRequestEvent request)
    {
        // cleanup open conversations first
        if (supportsConversation)
        {
            cleanupConversations();
        }


        //Get context
        RequestContext context = getRequestContext();

        //Destroy context
        if (context != null)
        {
            context.destroy();
        }
        
        // clean up the EL caches after each request
        ELContextStore elStore = ELContextStore.getInstance(false);
        if (elStore != null)
        {
            elStore.destroyELContextStore();
        }

        //Clear thread locals
        requestContexts.set(null);
        requestContexts.remove();

        RequestScopedBeanInterceptorHandler.removeThreadLocals();
    }

    private void cleanupConversations()
    {
        ConversationContext conversationContext = getConversationContext();

        if (conversationContext == null)
        {
            return;
        }

        Conversation conversation = conversationManager.getConversationBeanReference();

        if (conversation == null)
        {
            return;
        }

        if (conversation.isTransient())
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.log(Level.FINE, "Destroying the transient conversation context with cid : [{0}]", conversation.getId());
            }
            destroyConversationContext();
        }
        else
        {
            //Conversation must be used by one thread at a time
            ConversationImpl owbConversation = (ConversationImpl)conversation;
            owbConversation.updateTimeOut();
            //Other threads can now access propogated conversation.
            owbConversation.setInUsed(false);
        }
    }

    /**
     * Creates the session context at the session start. 
     * @param session http session object
     */
    private void initSessionContext(HttpSession session)
    {
        SessionContext currentSessionContext;

        if (session == null)
        {
            // no session -> create a dummy SessionContext
            // this is handy if you create asynchronous tasks or
            // batches which use a 'admin' user.
            currentSessionContext = new SessionContext();
        }
        else
        {
            String sessionId = session.getId();

            //Current context
            currentSessionContext = sessionCtxManager.getSessionContextWithSessionId(sessionId);

            //No current context
            if (currentSessionContext == null)
            {
                currentSessionContext = new SessionContext();
                sessionCtxManager.addNewSessionContext(sessionId, currentSessionContext);
            }
        }

        //Activate
        currentSessionContext.setActive(true);

        //Set thread local
        sessionContexts.set(currentSessionContext);
    }

    /**
     * Destroys the session context and all of its components at the end of the
     * session. 
     * @param session http session object
     */
    private void destroySessionContext(HttpSession session)
    {
        if (session != null)
        {
            //Get current session context
            SessionContext context = sessionContexts.get();

            //Destroy context
            if (context != null)
            {
                context.destroy();
            }

            //Clear thread locals
            sessionContexts.set(null);
            sessionContexts.remove();

            //Remove session from manager
            sessionCtxManager.destroySessionContextWithSessionId(session.getId());
        }
    }

    /**
     * Creates the application context at the application startup 
     * @param servletContext servlet context object
     */
    private void initApplicationContext(ServletContext servletContext)
    {
        
        if(servletContext != null)
        {
            if (currentApplicationContexts.containsKey(servletContext))
            {
                applicationContexts.set(currentApplicationContexts.get(servletContext));
            }
            else
            {
                ApplicationContext currentApplicationContext = new ApplicationContext();
                currentApplicationContext.setActive(true);
                currentApplicationContexts.put(servletContext, currentApplicationContext);

                applicationContexts.set(currentApplicationContext);
            }
        }
        else
        {
            // if we are in a thread which is not related to a Servlet request,
            // then we use a shared ApplicationContext.
            // this happens for asynchronous jobs and JMS invocations, etc.
            applicationContexts.set(sharedApplicationContext);
        }
    }

    /**
     * Destroys the application context and all of its components at the end of
     * the application. 
     * @param servletContext servlet context object
     */
    private void destroyApplicationContext(ServletContext servletContext)
    {
        //look for thread local
        //this can be set by initRequestContext
        ApplicationContext context = null;
        
        //Looking the context from saved context
        //This is used in real web applications
        if(servletContext != null)
        {
            context = currentApplicationContexts.get(servletContext);   
        }
        
        //using in tests
        if(context == null)
        {
            context = getApplicationContext();
        }
        
        //Destroy context
        if(context != null)
        {
            context.destroy();
        }
        
        //Remove from saved contexts
        if(servletContext != null)
        {
            currentApplicationContexts.remove(servletContext);   
        }
        
        //destroyDependents all sessions
        sessionCtxManager.destroyAllSessions();
        
        //destroyDependents all conversations
        conversationManager.destroyAllConversations();

        //Also clear application and singleton context
        applicationContexts.set(null);
        applicationContexts.remove();

        // this is needed to get rid of ApplicationScoped beans which are cached inside the proxies...
        webBeansContext.getBeanManagerImpl().clearCacheProxies();

    }
    
    /**
     * Initialize singleton context.
     * @param servletContext servlet context
     */
    private void initSingletonContext(ServletContext servletContext)
    {
        if(servletContext != null && currentSingletonContexts.containsKey(servletContext))
        {
            singletonContexts.set(currentSingletonContexts.get(servletContext));
        }
        
        else
        {
            SingletonContext context = new SingletonContext();
            context.setActive(true);
            
            if(servletContext != null)
            {
                currentSingletonContexts.put(servletContext, context);
                
            }
            
            singletonContexts.set(context);
        }
                        
    }
    
    /**
     * Destroy singleton context.
     * @param servletContext servlet context
     */
    private void destroySingletonContext(ServletContext servletContext)
    {
        SingletonContext context = null;

        //look for saved context
        if(servletContext != null)
        {
            context = currentSingletonContexts.get(servletContext);
        }
        
        //using in tests
        if(context == null)
        {
            context = getSingletonContext();
        }        
        
        //context is not null
        //destroyDependents it
        if(context != null)
        {
            context.destroy();
        }

        //remove it from saved contexts
        if(servletContext != null)
        {
            currentSingletonContexts.remove(servletContext);   
        }

        //Singleton context
        singletonContexts.set(null);
        singletonContexts.remove();
    }

    /**
     * Initialize conversation context.
     * @param context context
     */
    private void initConversationContext(ConversationContext context)
    {
        if (context == null)
        {
            if(conversationContexts.get() == null)
            {
                ConversationContext newContext = new ConversationContext();
                newContext.setActive(true);
                
                conversationContexts.set(newContext);
            }
            else
            {
                conversationContexts.get().setActive(true);
            }
            
        }
        else
        {
            context.setActive(true);
            conversationContexts.set(context);
        }
    }

    /**
     * Destroy conversation context.
     */
    private void destroyConversationContext()
    {
        ConversationContext context = getConversationContext();

        if (context != null)
        {
            context.destroy();
        }

        conversationContexts.set(null);
        conversationContexts.remove();
    }

    /**
     * Get current request ctx.
     * @return request context
     */
    private  RequestContext getRequestContext()
    {
        return requestContexts.get();
    }

    /**
     * Get current session ctx.
     * @return session context
     */
    private  SessionContext getSessionContext()
    {
        SessionContext context = sessionContexts.get();
        if (null == context)
        {
            lazyStartSessionContext();
            context = sessionContexts.get();
        }

        return context;
    }

    /**
     * Gets application context.
     * @return application context
     */
    private  ApplicationContext getApplicationContext()
    {
        return applicationContexts.get();
    }

    /**
     * Gets singleton context.
     * @return singleton context
     */
    private  SingletonContext getSingletonContext()
    {
        return singletonContexts.get();
    }

    /**
     * Get current conversation ctx.
     * @return conversation context
     */
    private  ConversationContext getConversationContext()
    {
        return conversationContexts.get();
    }

    private Context lazyStartSessionContext()
    {

        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, ">lazyStartSessionContext");
        }

        Context webContext = null;
        Context context = getCurrentContext(RequestScoped.class);
        if (context instanceof ServletRequestContext)
        {
            ServletRequestContext requestContext = (ServletRequestContext) context;
            HttpServletRequest servletRequest = requestContext.getServletRequest();
            if (null != servletRequest)
            { // this could be null if there is no active request context
                try
                {
                    HttpSession currentSession = servletRequest.getSession();
                    initSessionContext(currentSession);
                    if (failoverService != null && failoverService.isSupportFailOver())
                    {
                        failoverService.sessionIsInUse(currentSession);
                    }

                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.log(Level.FINE, "Lazy SESSION context initialization SUCCESS");
                    }
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0013, e));
                }

            }
            else
            {
                logger.log(Level.WARNING, "Could NOT lazily initialize session context because NO active request context");
            }
        }
        else
        {
            logger.log(Level.WARNING, "Could NOT lazily initialize session context because of "+context+" RequestContext");
        }

        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, "<lazyStartSessionContext "+ webContext);
        }
        return webContext;
    }


    /**
     * This might be needed when you aim to start a new thread in a WebApp.
     * @param scopeType
     */
    @Override
    public void activateContext(Class<? extends Annotation> scopeType)
    {
        if (scopeType.equals(ApplicationScoped.class))
        {
            if (applicationContexts.get() == null)
            {
                applicationContexts.set(sharedApplicationContext);
            }
        }
        if (scopeType.equals(SessionScoped.class))
        {
            // getSessionContext() implicitely creates and binds the SessionContext
            // to the current Thread if it doesn't yet exist.
            getSessionContext().setActive(true);
        }
        else
        {
            super.activateContext(scopeType);
        }
    }

}
