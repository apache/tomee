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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.context.AbstractContextsService;
import org.apache.webbeans.context.ApplicationContext;
import org.apache.webbeans.context.DependentContext;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.context.SingletonContext;
import org.apache.webbeans.spi.ContextsService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CdiAppContextsService extends AbstractContextsService implements ContextsService {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), CdiAppContextsService.class);
    private ThreadLocal<RequestContext> requestContext = new ThreadLocal<RequestContext>();
    private ThreadLocal<SessionContext> sessionContext = new ThreadLocal<SessionContext>();
    private DependentContext dependentContext = new DependentContext();

    private final ApplicationContext currentApplicationContext = new ApplicationContext();
    private final SingletonContext currentSingletonContext = new SingletonContext();

    public CdiAppContextsService() {
        dependentContext.setActive(true);
    }

    @Override
    public void init(Object initializeObject) {
        currentApplicationContext.setActive(true);
        currentSingletonContext.setActive(true);
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException {
        if (supportsContext(scopeType)) {
            if (scopeType.equals(RequestScoped.class)) {
                initRequestContext();
            } else if (scopeType.equals(SessionScoped.class)) {
                initSessionContext();
            } else if (scopeType.equals(ApplicationScoped.class)) {
            } else if (scopeType.equals(Dependent.class)) {
                // Do nothing
            } else if (scopeType.equals(Singleton.class)) {
                initSingletonContext();
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
    public void endContext(Class<? extends Annotation> scopeType, Object endParameters) {

        if (supportsContext(scopeType)) {
            if (scopeType.equals(RequestScoped.class)) {
                destroyRequestContext();
            } else if (scopeType.equals(SessionScoped.class)) {
                destroySessionContext();
            } else if (scopeType.equals(ApplicationScoped.class)) {
                destroyApplicationContext();
            } else if (scopeType.equals(Dependent.class)) {
                // Do nothing
            } else if (scopeType.equals(Singleton.class)) {
                destroySingletonContext();
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
    public Context getCurrentContext(Class<? extends Annotation> scopeType) {
        if (supportsContext(scopeType)) {
            if (scopeType.equals(RequestScoped.class)) {
                return getRequestContext();
            } else if (scopeType.equals(SessionScoped.class)) {
                return getSessionContext();
            } else if (scopeType.equals(ApplicationScoped.class)) {
                return currentApplicationContext;
            } else if (scopeType.equals(Dependent.class)) {
                return dependentContext;
            } else {
                return currentSingletonContext;
            }
        }

        return null;
    }

    private Context getRequestContext() {
        RequestContext context = requestContext.get();
        if (context == null) {

            context = new RequestContext();
            context.setActive(true);

            requestContext.set(context);
        }
        return context;
//        return ThreadContext.getThreadContext().get(RequestContext.class);
    }

    private Context getSessionContext() {
        SessionContext context = sessionContext.get();
        if (context == null) {

            context = new SessionContext();
            context.setActive(true);

            sessionContext.set(context);
        }
        return context;
    }

    private void destroyApplicationContext() {
        // look for thread local
        // this can be set by initRequestContext
//        this.currentApplicationContext.destroy();
    }

    private void initSingletonContext() {
    }

    private void destroySingletonContext() {
//        this.currentSingletonContext.destroy();
    }

    @Override
    public boolean supportsContext(Class<? extends Annotation> scopeType) {
        if (scopeType.equals(RequestScoped.class)
                || scopeType.equals(SessionScoped.class)
                || scopeType.equals(ApplicationScoped.class)
                || scopeType.equals(Dependent.class)
                || scopeType.equals(Singleton.class)) {
            return true;
        }

        return false;
    }

    private void initRequestContext() {

        RequestContext rq = new RequestContext();
        rq.setActive(true);

        requestContext.set(rq);
    }

    private void destroyRequestContext() {
        // Get context
        RequestContext context = requestContext.get();

        // Destroy context
        if (context != null) {
            context.destroy();
        }

        // Remove
        requestContext.remove();
    }

    private void initSessionContext() {

        SessionContext rq = new SessionContext();
        rq.setActive(true);

        sessionContext.set(rq);
    }

    private void destroySessionContext() {
        // Get context
        SessionContext context = sessionContext.get();

        // Destroy context
        if (context != null) {
            context.destroy();
        }

        // Remove
        sessionContext.remove();
    }

    public void destroy(Object destroyObject) {
//        //Destroy application context
//        endContext(ApplicationScoped.class, destroyObject);
//
//        //Destroy singleton context
//        endContext(Singleton.class, destroyObject);


        //Remove thread locals
        //for preventing memory leaks
        requestContext.remove();

        //Thread local values to null
        requestContext.set(null);
    }

}
