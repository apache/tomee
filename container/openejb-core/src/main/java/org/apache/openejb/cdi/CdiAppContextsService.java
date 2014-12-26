/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.cdi;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
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
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.ConversationService;
import org.apache.webbeans.web.context.ServletRequestContext;
import org.apache.webbeans.web.intercept.RequestScopedBeanInterceptorHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextException;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CdiAppContextsService extends AbstractContextsService implements ContextsService {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), CdiAppContextsService.class);

    private final ThreadLocal<ServletRequestContext> requestContext = new ThreadLocal<ServletRequestContext>();

    private final ThreadLocal<SessionContext> sessionContext = new ThreadLocal<SessionContext>();
    private final UpdatableSessionContextManager sessionCtxManager = new UpdatableSessionContextManager();

    /**
     * Conversation context manager
     */
    private final ThreadLocal<ConversationContext> conversationContext;

    private final DependentContext dependentContext = new DependentContext();

    private final ApplicationContext applicationContext = new ApplicationContext();

    private final SingletonContext singletonContext = new SingletonContext();

    private final WebBeansContext webBeansContext;

    private final ConversationService conversationService;

    private static final ThreadLocal<Collection<Runnable>> endRequestRunnables = new ThreadLocal<Collection<Runnable>>() {
        @Override
        protected Collection<Runnable> initialValue() {
            return new ArrayList<Runnable>();
        }
    };


    public CdiAppContextsService(final WebBeansContext wbc) {
        this(wbc, wbc.getOpenWebBeansConfiguration().supportsConversation());
    }

    public CdiAppContextsService(final WebBeansContext wbc, final boolean supportsConversation) {
        if (wbc != null) {
            webBeansContext = wbc;
        } else {
            webBeansContext = WebBeansContext.currentInstance();
        }

        dependentContext.setActive(true);
        if (supportsConversation) {
            conversationService = webBeansContext.getService(ConversationService.class);
            if (conversationService == null) {
                conversationContext = null;
            } else {
                conversationContext = new ThreadLocal<ConversationContext>();
            }
        } else {
            conversationService = null;
            conversationContext = null;
        }
        applicationContext.setActive(true);
        singletonContext.setActive(true);
    }

    private void endRequest() {
        for (final Runnable r : endRequestRunnables.get()) {
            try {
                r.run();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        endRequestRunnables.remove();
    }

    public static void pushRequestReleasable(final Runnable runnable) {
        endRequestRunnables.get().add(runnable);
    }

    @Override
    public void init(final Object initializeObject) {
        //Start application context
        startContext(ApplicationScoped.class, initializeObject);

        //Start signelton context
        startContext(Singleton.class, initializeObject);
    }

    public void beforeStop(final Object destroyObject) {
        {   // trigger @PreDestroy mainly but keep it active until destroy(xxx)
            applicationContext.destroy();
            webBeansContext.getBeanManagerImpl().fireEvent(destroyObject != null ? destroyObject : applicationContext, DestroyedLiteral.APP);
            applicationContext.setActive(true);

            singletonContext.destroy();
            singletonContext.setActive(true);
        }

        for (final Map.Entry<Conversation, ConversationContext> conversation : webBeansContext.getConversationManager().getAllConversationContexts().entrySet()) {
            conversation.getValue().destroy();
            webBeansContext.getBeanManagerImpl().fireEvent(conversation.getKey().getId(), DestroyedLiteral.CONVERSATION);
        }
        for (final SessionContext sc : sessionCtxManager.getContextById().values()) {
            final Object event = HttpSessionContextSessionAware.class.isInstance(sc) ? HttpSessionContextSessionAware.class.cast(sc).getSession() : sc;
            if (HttpSession.class.isInstance(event)) {
                final HttpSession httpSession = HttpSession.class.cast(event);
                initSessionContext(httpSession);
                try {
                    httpSession.invalidate();
                } finally {
                    destroySessionContext(httpSession);
                }
            } else {
                sc.destroy();
            }
            webBeansContext.getBeanManagerImpl().fireEvent(event, DestroyedLiteral.SESSION);
        }
        sessionCtxManager.getContextById().clear();
    }

    public void destroy(final Object destroyObject) {
        //Destroy application context
        endContext(ApplicationScoped.class, destroyObject);

        //Destroy singleton context
        endContext(Singleton.class, destroyObject);

        removeThreadLocals();
    }

    public void removeThreadLocals() {
        //Remove thread locals
        //for preventing memory leaks
        requestContext.set(null);
        requestContext.remove();
        sessionContext.set(null);
        sessionContext.remove();

        if (null != conversationContext) {
            conversationContext.set(null);
            conversationContext.remove();
        }
    }

    @Override
    public void endContext(final Class<? extends Annotation> scopeType, final Object endParameters) {
        if (supportsContext(scopeType)) {
            if (scopeType.equals(RequestScoped.class)) {
                destroyRequestContext();
            } else if (scopeType.equals(SessionScoped.class)) {
                destroySessionContext((HttpSession) endParameters);
            } else if (scopeType.equals(ApplicationScoped.class)) {
                destroyApplicationContext();
            } else if (scopeType.equals(Dependent.class)) { //NOPMD
                // Do nothing
            } else if (scopeType.equals(Singleton.class)) {
                destroySingletonContext();
            } else if (supportsConversation() && scopeType.equals(ConversationScoped.class)) {
                destroyConversationContext(endParameters);
            } else {
                if (logger.isWarningEnabled()) {
                    logger.warning("CDI-OpenWebBeans container in OpenEJB does not support context scope "
                        + scopeType.getSimpleName()
                        + ". Scopes @Dependent, @RequestScoped, @ApplicationScoped and @Singleton are supported scope types");
                }
            }
        }

    }

    @Override
    public Context getCurrentContext(final Class<? extends Annotation> scopeType) {
        if (scopeType.equals(RequestScoped.class)) {
            return getRequestContext(true);
        } else if (scopeType.equals(SessionScoped.class)) {
            return getSessionContext(true);
        } else if (scopeType.equals(ApplicationScoped.class)) {
            return getApplicationContext();
        } else if (supportsConversation() && scopeType.equals(ConversationScoped.class)) {
            return getConversationContext(true);
        } else if (scopeType.equals(Dependent.class)) {
            return dependentContext;
        } else if (scopeType.equals(Singleton.class)) {
            return getSingletonContext();
        }

        return null;
    }

    @Override
    public void startContext(final Class<? extends Annotation> scopeType, final Object startParameter) throws ContextException {
        if (supportsContext(scopeType)) {
            if (scopeType.equals(RequestScoped.class)) {
                initRequestContext((ServletRequestEvent) startParameter);
            } else if (scopeType.equals(SessionScoped.class)) {
                initSessionContext((HttpSession) startParameter);
            } else if (scopeType.equals(ApplicationScoped.class)) {
                initApplicationContext(startParameter);
            } else if (scopeType.equals(Dependent.class)) {
                initSingletonContext();
            } else if (scopeType.equals(Singleton.class)) { //NOPMD
                // Do nothing
            } else if (supportsConversation() && scopeType.equals(ConversationScoped.class)) {
                initConversationContext(startParameter);
            } else {
                if (logger.isWarningEnabled()) {
                    logger.warning("CDI-OpenWebBeans container in OpenEJB does not support context scope "
                        + scopeType.getSimpleName()
                        + ". Scopes @Dependent, @RequestScoped, @ApplicationScoped and @Singleton are supported scope types");
                }
            }
        }
    }

    private void initSingletonContext() {
        singletonContext.setActive(true);
    }

    private void initApplicationContext(final Object init) { // in case contexts are stop/start
        final boolean alreadyStarted = applicationContext.isActive();
        if (!alreadyStarted) {
            applicationContext.setActive(true);
            webBeansContext.getBeanManagerImpl().fireEvent(init != null ? init : applicationContext, InitializedLiteral.APP);
        }
    }

    @Override
    public boolean supportsContext(final Class<? extends Annotation> scopeType) {
        return scopeType.equals(RequestScoped.class)
            || scopeType.equals(SessionScoped.class)
            || scopeType.equals(ApplicationScoped.class)
            || scopeType.equals(Dependent.class)
            || scopeType.equals(Singleton.class)
            || scopeType.equals(ConversationScoped.class) && supportsConversation();

    }

    private void initRequestContext(final ServletRequestEvent event) {
        final ServletRequestContext rq = new ServletRequestContext();
        rq.setActive(true);

        requestContext.set(rq);// set thread local
        if (event != null) {
            final HttpServletRequest request = (HttpServletRequest) event.getServletRequest();
            ((ServletRequestContext) rq).setServletRequest(request);

            if (request != null) {
                webBeansContext.getBeanManagerImpl().fireEvent(request, InitializedLiteral.REQUEST);
            }

            if (request != null) {
                //Re-initialize thread local for session
                final HttpSession session = request.getSession(false);

                if (session != null) {
                    initSessionContext(session);
                }
            }
        }
    }

    private void destroyRequestContext() {
        // execute request tasks
        endRequest();

        if (supportsConversation()) { // OWB-595
            cleanupConversation();
        }

        //Get context
        final RequestContext context = getRequestContext(false);

        //Destroy context
        if (context != null) {
            final HttpServletRequest servletRequest = ServletRequestContext.class.cast(context).getServletRequest();
            if (servletRequest != null) {
                webBeansContext.getBeanManagerImpl().fireEvent(servletRequest, DestroyedLiteral.REQUEST);
            }
            context.destroy();
        }

        // clean up the EL caches after each request
        final ELContextStore elStore = ELContextStore.getInstance(false);
        if (elStore != null) {
            elStore.destroyELContextStore();
        }

        //Clear thread locals - only for request to let user do with deltaspike start(session, request)restart(request)...stop()
        requestContext.remove();

        RequestScopedBeanInterceptorHandler.removeThreadLocals();
    }

    private void cleanupConversation() {
        if (conversationService == null) {
            return;
        }

        final ConversationContext conversationContext = getConversationContext(false);
        if (conversationContext == null) {
            return;
        }

        final ConversationManager conversationManager = webBeansContext.getConversationManager();
        final Conversation conversation = conversationManager.getConversationBeanReference();
        if (conversation == null) {
            return;
        }

        if (conversation.isTransient()) {
            webBeansContext.getContextsService().endContext(ConversationScoped.class, null);
        } else {
            final ConversationImpl conversationImpl = (ConversationImpl) conversation;
            conversationImpl.updateTimeOut();
            conversationImpl.setInUsed(false);
        }
    }

    /**
     * Creates the session context at the session start.
     *
     * @param session http session object
     */
    private void initSessionContext(final HttpSession session) {
        if (session == null) {
            // no session -> no SessionContext
            return;
        }

        final String sessionId = session.getId();
        //Current context
        SessionContext currentSessionContext = sessionCtxManager.getSessionContextWithSessionId(sessionId);

        //No current context
        if (currentSessionContext == null) {
            currentSessionContext = newSessionContext(session);
            sessionCtxManager.addNewSessionContext(sessionId, currentSessionContext);
            webBeansContext.getBeanManagerImpl().fireEvent(session, InitializedLiteral.SESSION);
        }
        //Activate
        currentSessionContext.setActive(true);

        //Set thread local
        sessionContext.set(currentSessionContext);
    }

    private SessionContext newSessionContext(final HttpSession session) {
        final String classname = SystemInstance.get().getComponent(ThreadSingletonService.class).sessionContextClass();
        if (classname != null) {
            try {
                final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(classname);
                try {
                    final Constructor<?> constr = clazz.getConstructor(HttpSession.class);
                    return (SessionContext) constr.newInstance(session);
                } catch (final Exception e) {
                    return (SessionContext) clazz.newInstance();
                }

            } catch (final Exception e) {
                logger.error("Can't instantiate " + classname + ", using default session context", e);
            }
        }
        return new HttpSessionContextSessionAware(session);
    }

    /**
     * Destroys the session context and all of its components at the end of the
     * session.
     *
     * @param session http session object
     */
    private void destroySessionContext(final HttpSession session) {
        if (session != null) {
            final SessionContext context = sessionContext.get();

            if (context != null && context.isActive()) {
                context.destroy();
                pushRequestReleasable(new Runnable() { // call it at the end of the request
                    @Override
                    public void run() {
                        webBeansContext.getBeanManagerImpl().fireEvent(session, DestroyedLiteral.SESSION);
                    }
                });
            }

            //Clear thread locals
            sessionContext.set(null);
            sessionContext.remove();

            //Remove session from manager
            sessionCtxManager.removeSessionContextWithSessionId(session.getId());
        }
    }

    //we don't have initApplicationContext

    private void destroyApplicationContext() {
        applicationContext.destroy();
    }

    private void destroySingletonContext() {
        singletonContext.destroy();
    }

    /**
     * Initialize conversation context.
     *
     * @param context context
     */
    private void initConversationContext(final Object request) {
        if (conversationService == null) {
            return;
        }

        final HttpServletRequest req = HttpServletRequest.class.isInstance(request) ? HttpServletRequest.class.cast(request) : null;
        ConversationContext context = ConversationContext.class.isInstance(request) ? ConversationContext.class.cast(request) : null;
        if (context == null) {
            final ConversationContext existingContext = conversationContext.get();
            if (existingContext == null) {
                context = new ConversationContext();
                context.setActive(true);

                conversationContext.set(context);
                final Object event;
                if (req != null) {
                    event = req;
                } else {
                    final ServletRequestContext servletRequestContext = getRequestContext(true);
                    event = servletRequestContext != null && servletRequestContext.getServletRequest() != null ? servletRequestContext.getServletRequest() : context;
                }
                webBeansContext.getBeanManagerImpl().fireEvent(event, InitializedLiteral.CONVERSATION);
            } else {
                context = existingContext;
            }
        } else {
            conversationContext.set(context);
        }
        context.setActive(true);
    }

    /**
     * Destroy conversation context.
     */
    private void destroyConversationContext(final Object destroy) {
        if (conversationService == null) {
            return;
        }

        final ConversationContext context = getConversationContext(false);

        if (context != null) {
            context.destroy();
            final ServletRequestContext servletRequestContext = getRequestContext(false);
            final Object destroyObject = servletRequestContext != null && servletRequestContext.getServletRequest() != null ?
                    servletRequestContext.getServletRequest() : destroy;
            webBeansContext.getBeanManagerImpl().fireEvent(
                    destroyObject == null ? context : destroyObject, DestroyedLiteral.CONVERSATION);
        }

        if (null != conversationContext) {
            conversationContext.remove();
        }
    }


    private ServletRequestContext getRequestContext(final boolean create) {
        ServletRequestContext context = requestContext.get();
        if (context == null && create) {
            initRequestContext(null);
        }
        return context;
    }

    private Context getSessionContext(final boolean create) {
        SessionContext context = sessionContext.get();
        if ((context == null || !context.isActive()) && create) {
            lazyStartSessionContext();
            context = sessionContext.get();
            if (context == null) {
                context = new SessionContext();
                context.setActive(true);
                sessionContext.set(context);
            }
        }
        return context;
    }

    /**
     * Gets application context.
     *
     * @return application context
     */
    private ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Gets singleton context.
     *
     * @return singleton context
     */
    private SingletonContext getSingletonContext() {
        return singletonContext;
    }

    /**
     * Get current conversation ctx.
     *
     * @return conversation context
     */
    private ConversationContext getConversationContext(final boolean createIfMissing) {
        ConversationContext context = conversationContext.get();
        if (context == null && createIfMissing) {
            getRequestContext(true); // needs to exist for Conversation scope
            initConversationContext(null);
            return getConversationContext(false);
        }
        return context;
    }

    private Context lazyStartSessionContext() {

        if (logger.isDebugEnabled()) {
            logger.debug(">lazyStartSessionContext");
        }

        final Context webContext = null;
        final Context context = getCurrentContext(RequestScoped.class);
        if (context instanceof ServletRequestContext) {
            final ServletRequestContext requestContext = (ServletRequestContext) context;
            final HttpServletRequest servletRequest = requestContext.getServletRequest();
            if (null != servletRequest) { // this could be null if there is no active request context
                try {
                    final HttpSession currentSession = servletRequest.getSession();
                    initSessionContext(currentSession);

                    /*
                    final FailOverService failoverService = webBeansContext.getService(FailOverService.class);
                    if (failoverService != null && failoverService.isSupportFailOver()) {
                        failoverService.sessionIsInUse(currentSession);
                    }
                    */

                    if (logger.isDebugEnabled()) {
                        logger.debug("Lazy SESSION context initialization SUCCESS");
                    }
                } catch (final Exception e) {
                    logger.error(OWBLogConst.ERROR_0013, e);
                }

            } else {
                logger.warning("Could NOT lazily initialize session context because NO active request context");
            }
        } else {
            logger.warning("Could NOT lazily initialize session context because of " + context + " RequestContext");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<lazyStartSessionContext " + webContext);
        }
        return webContext;
    }

    private boolean supportsConversation() {
        return conversationContext != null;
    }

    public void updateSessionIdMapping(final String oldId, final String newId) {
        sessionCtxManager.updateSessionIdMapping(oldId, newId);
    }

    public State saveState() {
        return new State(requestContext.get(), sessionContext.get(), conversationContext.get());
    }

    public State restoreState(final State state) {
        final State old = saveState();
        requestContext.set(state.request);
        sessionContext.set(state.session);
        conversationContext.set(state.conversation);
        return old;
    }

    public static class State {
        private final ServletRequestContext request;
        private final SessionContext session;
        private final ConversationContext conversation;

        public State(final ServletRequestContext request, final SessionContext session, final ConversationContext conversation) {
            this.request = request;
            this.session = session;
            this.conversation = conversation;
        }
    }

    public static class InitializedLiteral extends AnnotationLiteral<Initialized> implements Initialized {
        private static final InitializedLiteral APP = new InitializedLiteral(ApplicationScoped.class);
        private static final InitializedLiteral CONVERSATION = new InitializedLiteral(ConversationScoped.class);
        private static final InitializedLiteral REQUEST = new InitializedLiteral(RequestScoped.class);
        private static final InitializedLiteral SESSION = new InitializedLiteral(SessionScoped.class);

        private final Class<? extends Annotation> value;

        public InitializedLiteral(final Class<? extends Annotation> value) {
            this.value = value;
        }

        public Class<? extends Annotation> value() {
            return value;
        }
    }

    public static class DestroyedLiteral extends AnnotationLiteral<Destroyed> implements Destroyed {
        private static final DestroyedLiteral APP = new DestroyedLiteral(ApplicationScoped.class);
        private static final DestroyedLiteral CONVERSATION = new DestroyedLiteral(ConversationScoped.class);
        private static final DestroyedLiteral REQUEST = new DestroyedLiteral(RequestScoped.class);
        private static final DestroyedLiteral SESSION = new DestroyedLiteral(SessionScoped.class);

        private final Class<? extends Annotation> value;

        public DestroyedLiteral(final Class<? extends Annotation> value) {
            this.value = value;
        }

        public Class<? extends Annotation> value() {
            return value;
        }
    }

    public static class HttpSessionContextSessionAware extends SessionContext {
        private final HttpSession session;

        public HttpSessionContextSessionAware(final HttpSession session) {
            this.session = session;
        }

        public HttpSession getSession() {
            return session;
        }
    }
}
