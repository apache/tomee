/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.ValidatorService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @version $Rev:$ $Date:$
 */
public class ThreadSingletonServiceImpl implements ThreadSingletonService {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ThreadSingletonServiceImpl.class);
    //this needs to be static because OWB won't tell us what the existing SingletonService is and you can't set it twice.
    private static final ThreadLocal<WebBeansContext> contexts = new ThreadLocal<WebBeansContext>();

    public ThreadSingletonServiceImpl() {

    }



    @Override
    public void initialize(StartupObject startupObject) {
        //initialize owb context, cf geronimo's OpenWebBeansGBean
        WebBeansContext webBeansContext = new WebBeansContext();
        startupObject.getAppContext().set(WebBeansContext.class, webBeansContext);
        Object old = contextEntered(webBeansContext);
        try {
            setConfiguration(webBeansContext.getOpenWebBeansConfiguration());
            try {
                webBeansContext.getService(ContainerLifecycle.class).startApplication(startupObject);
            } catch (Exception e) {
                throw new RuntimeException("couldn't start owb context", e);
            }
        } finally {
            contextExited(old);
        }
    }

    //not sure what openejb will need

    private void setConfiguration(OpenWebBeansConfiguration configuration) {
        //from CDI builder
        configuration.setProperty(SecurityService.class.getName(), ManagedSecurityService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
//        configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");

        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, OpenEJBLifecycle.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.TRANSACTION_SERVICE, OpenEJBTransactionService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, CdiScanner.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, CdiAppContextsService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.VALIDATOR_SERVICE, OpenEJBValidatorService.class.getName());
        configuration.setProperty(ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
    }

    @Override
    public Object contextEntered(WebBeansContext newOWBContext) {
        return enter(newOWBContext);
    }

    public static Object enter(WebBeansContext newOWBContext) {
        WebBeansContext oldContext = contexts.get();
        contexts.set(newOWBContext);
        contextMessage(newOWBContext, "Enter:");
        return oldContext;
    }

    private static void contextMessage(WebBeansContext newOWBContext, String prefix) {
    }

    @Override
    public void contextExited(Object oldContext) {
        exit(oldContext);
    }

    public static void exit(Object oldContext) {
        if (oldContext != null && !(oldContext instanceof WebBeansContext)) throw new IllegalArgumentException("ThreadSingletonServiceImpl can only be used with WebBeansContext, not " + oldContext.getClass().getName());
        contexts.set((WebBeansContext) oldContext);
    }

    private WebBeansContext getContext() {
        return get();
    }

    public static WebBeansContext get()
    {
        WebBeansContext context = contexts.get();
        if (context == null) {
            // Fallback strategy is to just grab the first AppContext and assume it is the right one
            // This kind of algorithm could be greatly improved
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

            final List<AppContext> appContexts = containerSystem.getAppContexts();

            if (appContexts.size() > 0) return getWebBeansContext(appContexts);

            throw new IllegalStateException("On a thread without an initialized context");
        }
        return context;
    }

    private static WebBeansContext getWebBeansContext(List<AppContext> appContexts) {
        Collections.sort(appContexts, new Comparator<AppContext>() {
            @Override
            public int compare(AppContext appContext, AppContext appContext1) {
                return appContext1.getWebBeansContext().getBeanManagerImpl().getBeans().size() - appContext.getWebBeansContext().getBeanManagerImpl().getBeans().size();
            }
        });
        return appContexts.get(0).getWebBeansContext();
    }

    @Override
    public WebBeansContext get(Object key) {
        return getContext();
    }

    @Override
    public void clear(Object key) {
        contextMessage(getContext(), "clearing ");
        getContext().clear();
    }

}
