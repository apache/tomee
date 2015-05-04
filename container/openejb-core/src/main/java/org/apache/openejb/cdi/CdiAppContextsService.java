
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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.web.context.WebContextsService;

import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;


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
        //Start application context
        startContext(ApplicationScoped.class, initializeObject);

        //Start signelton context
        startContext(Singleton.class, initializeObject);
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
}
