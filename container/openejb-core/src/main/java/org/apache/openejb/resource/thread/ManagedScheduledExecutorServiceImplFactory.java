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
package org.apache.openejb.resource.thread;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ContextServiceImplFactory;
import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.threads.reject.CURejectHandler;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.enterprise.concurrent.ManagedThreadFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ManagedScheduledExecutorServiceImplFactory {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, ManagedScheduledExecutorServiceImplFactory.class);

    private static final String DEFAULT_MES = "java:comp/DefaultManagedExecutorService";
    private static final String DEFAULT_MSES = "java:comp/DefaultManagedScheduledExecutorService";

    public static ManagedScheduledExecutorServiceImpl lookup(String name) {
        // If the caller passes the default ManagedExecutorService JNDI name, map it to the
        // default ManagedScheduledExecutorService instead
        final boolean isDefault = DEFAULT_MES.equals(name) || DEFAULT_MSES.equals(name);
        if (DEFAULT_MES.equals(name)) {
            name = DEFAULT_MSES;
        }

        // Try direct JNDI lookup first
        try {
            final Object obj = InitialContext.doLookup(name);
            if (obj instanceof ManagedScheduledExecutorServiceImpl mses) {
                return mses;
            }
        } catch (final NamingException ignored) {
            // fall through to container JNDI
        }

        // Try container JNDI with resource ID
        try {
            final Context ctx = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            final String resourceId = DEFAULT_MSES.equals(name)
                    ? "Default Scheduled Executor Service"
                    : name;

            final Object obj = ctx.lookup("openejb/Resource/" + resourceId);
            if (obj instanceof ManagedScheduledExecutorServiceImpl mses) {
                return mses;
            }
        } catch (final NamingException ignored) {
            // fall through
        }

        // Only fall back to default for the well-known default names.
        // For custom/invalid names, throw so the caller gets RejectedExecutionException.
        if (isDefault) {
            LOGGER.debug("Cannot lookup ManagedScheduledExecutorService '" + name + "', creating default instance");
            return new ManagedScheduledExecutorServiceImplFactory().create();
        }

        throw new IllegalArgumentException("Cannot find ManagedScheduledExecutorService with name '" + name + "'");
    }

    private int core = 5;
    private String threadFactory = ManagedThreadFactoryImpl.class.getName();
    private boolean virtual;

    private String context;

    public ManagedScheduledExecutorServiceImpl create(final ContextServiceImpl contextService) {
        return new ManagedScheduledExecutorServiceImpl(createScheduledExecutorService(), contextService);
    }
    public ManagedScheduledExecutorServiceImpl create() {
        return new ManagedScheduledExecutorServiceImpl(createScheduledExecutorService(), ContextServiceImplFactory.lookupOrDefault(context));
    }

    private ScheduledExecutorService createScheduledExecutorService() {
        ManagedThreadFactory managedThreadFactory;
        try {
            managedThreadFactory = ThreadFactories.findThreadFactory(threadFactory);
        } catch (final Exception e) {
            Logger.getInstance(LogCategory.OPENEJB, ManagedScheduledExecutorServiceImplFactory.class).warning("Unable to create configured thread factory: " + threadFactory, e);
            managedThreadFactory = new ManagedThreadFactoryImpl(ManagedThreadFactoryImpl.DEFAULT_PREFIX, null, ContextServiceImplFactory.lookupOrDefault(context), virtual);
        }

        return new ScheduledThreadPoolExecutor(core, managedThreadFactory, CURejectHandler.INSTANCE);
    }

    public void setCore(final int core) {
        this.core = core;
    }

    public void setThreadFactory(final String threadFactory) {
        this.threadFactory = threadFactory;
    }

    public String getContext() {
        return context;
    }

    public void setContext(final String context) {
        this.context = context;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(final boolean virtual) {
        this.virtual = virtual;
    }
}
