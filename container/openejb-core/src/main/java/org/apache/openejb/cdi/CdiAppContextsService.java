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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.context.AbstractContextsService;
import org.apache.webbeans.context.ApplicationContext;
import org.apache.webbeans.context.DependentContext;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.context.SingletonContext;
import org.apache.webbeans.spi.ContextsService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Context;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

public class CdiAppContextsService extends AbstractContextsService implements ContextsService {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), CdiAppContextsService.class);
    private static ThreadLocal<RequestContext> requestContext = new ThreadLocal<RequestContext>();
    private static ThreadLocal<ApplicationContext> applicationContext = new ThreadLocal<ApplicationContext>();
    private static ThreadLocal<SingletonContext> singletonContext = new ThreadLocal<SingletonContext>();
    private static DependentContext dependentContext = new DependentContext();

    private volatile ApplicationContext currentApplicationContext = null;
    private volatile SingletonContext currentSingletonContext = null;

    public CdiAppContextsService() {
        dependentContext.setActive(true);
    }

    @Override
    public void init(Object initializeObject) {
        //Start application context
        startContext(ApplicationScoped.class, initializeObject);

        //Start signelton context
        startContext(Singleton.class, initializeObject);
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException {
        if (supportsContext(scopeType)) {
            if (scopeType.equals(RequestScoped.class)) {
                initRequestContext();
            } else if (scopeType.equals(ApplicationScoped.class)) {
                initApplicationContext();
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
                return requestContext.get();
            } else if (scopeType.equals(ApplicationScoped.class)) {
                return applicationContext.get();
            } else if (scopeType.equals(Dependent.class)) {
                return dependentContext;
            } else {
                return singletonContext.get();
            }
        }

        return null;
    }

    private void initApplicationContext() {
        if (this.currentApplicationContext != null) {
            applicationContext.set(this.currentApplicationContext);
        } else {
            ApplicationContext currentApplicationContext = new ApplicationContext();
            currentApplicationContext.setActive(true);

            this.currentApplicationContext = currentApplicationContext;
            applicationContext.set(currentApplicationContext);
        }
    }

    private void destroyApplicationContext() {
        // look for thread local
        // this can be set by initRequestContext
        ApplicationContext context = this.currentApplicationContext;

        // using in tests
        if (context == null) {
            context = applicationContext.get();
        }

        // Destroy context
        if (context != null) {
            context.destroy();
        }

        this.currentApplicationContext = null;
    }

    private void initSingletonContext() {
        if (this.currentSingletonContext != null) {
            singletonContext.set(currentSingletonContext);
        } else {
            SingletonContext context = new SingletonContext();
            context.setActive(true);
            this.currentSingletonContext = context;

            singletonContext.set(context);
        }

    }

    private void destroySingletonContext() {
        SingletonContext context = this.currentSingletonContext;

        // using in tests
        if (context == null) {
            context = singletonContext.get();
        }

        // context is not null
        // destroy it
        if (context != null) {
            context.destroy();
        }

        this.currentSingletonContext = null;
    }

    @Override
    public boolean supportsContext(Class<? extends Annotation> scopeType) {
        if (scopeType.equals(RequestScoped.class)
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

        // Init threadLocal application context
        initApplicationContext();

        // Init threadlocal singleton context
        initSingletonContext();
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
        requestContext.set(null);

        // Remove singleton and application contexts
        singletonContext.remove();
        singletonContext.set(null);

        applicationContext.remove();
        applicationContext.set(null);

    }

    public void destroy(Object destroyObject) {
        //Destroy application context
        endContext(ApplicationScoped.class, destroyObject);

        //Destroy singleton context
        endContext(Singleton.class, destroyObject);

        this.currentApplicationContext = null;
        this.currentSingletonContext = null;

        //Remove thread locals
        //for preventing memory leaks
        requestContext.remove();
        applicationContext.remove();
        singletonContext.remove();

        //Thread local values to null
        requestContext.set(null);
        applicationContext.set(null);
        singletonContext.set(null);

    }

}
