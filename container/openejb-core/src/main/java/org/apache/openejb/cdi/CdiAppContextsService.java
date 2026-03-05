
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
import org.apache.webbeans.annotation.InitializedLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.event.EventMetadataImpl;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.web.context.WebContextsService;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;


public class CdiAppContextsService extends WebContextsService implements ContextsService {
    public static final Object EJB_REQUEST_EVENT = new Object();

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), CdiAppContextsService.class);

    private static final ThreadLocal<Collection<Runnable>> endRequestRunnables = new ThreadLocal<Collection<Runnable>>() {
        @Override
        protected Collection<Runnable> initialValue() {
            return new ArrayList<>();
        }
    };


    public CdiAppContextsService(final WebBeansContext wbc) {
        super(wbc);
    }


    private void runEndRequestTasks() {
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

    @Override // this method is called after the deployment (BeansDeployer) but need beans to be here to get events
    public void init(final Object initializeObject) {
        super.init(initializeObject);
        if (initializeObject != null) {
            Object event = initializeObject;
            if (StartupObject.class.isInstance(initializeObject)) {
                final StartupObject so = StartupObject.class.cast(initializeObject);
                if (so.isFromWebApp()) { // ear webapps
                    event = so.getWebContext().getServletContext();
                } else if (so.getAppInfo().webAppAlone) {
                    event = SystemInstance.get().getComponent(ServletContext.class);
                }
            } else if (ServletContextEvent.class.isInstance(initializeObject)) {
                event = ServletContextEvent.class.cast(initializeObject).getServletContext();
            }
            if (!FiredManually.class.isInstance(event)) {
                applicationStarted(event);
            }
        }
    }

    public void applicationStarted(final Object event) {
        Object appEvent = event != null ? event : applicationContext;
        webBeansContext.getBeanManagerImpl().fireEvent(
                appEvent,
                new EventMetadataImpl(null,
                        ServletContext.class.isInstance(appEvent) ? ServletContext.class : Object.class, null,
                        new Annotation[]{InitializedLiteral.INSTANCE_APPLICATION_SCOPED},
                        webBeansContext),
                false);
    }

    public void destroy(final Object destroyObject) {
        super.destroy(destroyObject);
        removeThreadLocals();
    }


    @Override
    protected void destroyRequestContext(Object requestEvent) {
        // execute endrequest tasks
        runEndRequestTasks();

        super.destroyRequestContext(requestEvent);
    }

    public interface FiredManually {}

    @Override
    protected void initRequestContext(final Object startupObject) {
        if(startupObject instanceof ServletRequestEvent) {

            // OWB gets the ServletContextWrapper from the event and attempts to cast it to an HttpServletRequest
            // in TCK, some tests are actually downgrading the HttpServletRequest to a regular ServletRequest
            // See com.sun.ts.tests.servlet.api.jakarta_servlet.asynccontext
            // In the look bellow, we try to check if we can find an HttpServletRequest in the hierarchy.
            // otherwise, we let the call being delegated to OWB

            ServletRequest current = ((ServletRequestEvent) startupObject).getServletRequest();
            while (!HttpServletRequest.class.isInstance(current) && ServletRequestWrapper.class.isInstance(current)) {
                current = ServletRequestWrapper.class.cast(current).getRequest();
            }
            super.initRequestContext(new ServletRequestEvent(((ServletRequestEvent) startupObject).getServletContext(), current));

        } else {
            super.initRequestContext(startupObject);

        }
    }
}
