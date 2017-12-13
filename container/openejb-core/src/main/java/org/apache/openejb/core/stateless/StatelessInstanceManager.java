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

package org.apache.openejb.core.stateless;

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.instance.InstanceCreatorRunnable;
import org.apache.openejb.core.instance.InstanceManager;
import org.apache.openejb.core.instance.InstanceManagerData;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.core.timer.TimerServiceWrapper;
import org.apache.openejb.loader.Options;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.openejb.util.Pool;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.ejb.EJBContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatelessInstanceManager extends InstanceManager {

    public StatelessInstanceManager(final SecurityService securityService,
                                    final Duration accessTimeout, final Duration closeTimeout,
                                    final Pool.Builder poolBuilder, final int callbackThreads,
                                    final ScheduledExecutorService ses) {
        super(securityService, accessTimeout, closeTimeout, poolBuilder, callbackThreads, ses);
    }


    @SuppressWarnings("unchecked")
    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        final Options options = new Options(beanContext.getProperties());

        final Duration accessTimeout = getDuration(
                options,
                "AccessTimeout",
                getDuration(options, "Timeout", this.accessTimeout, TimeUnit.MILLISECONDS), // default timeout
                TimeUnit.MILLISECONDS
        );
        final Duration closeTimeout = getDuration(options, "CloseTimeout", this.closeTimeout, TimeUnit.MINUTES);

        final ObjectRecipe recipe = PassthroughFactory.recipe(new Pool.Builder(poolBuilder));
        recipe.allow(Option.CASE_INSENSITIVE_FACTORY);
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        recipe.setAllProperties(beanContext.getProperties());
        final Pool.Builder builder = (Pool.Builder) recipe.create();

        setDefault(builder.getMaxAge(), TimeUnit.HOURS);
        setDefault(builder.getIdleTimeout(), TimeUnit.MINUTES);
        setDefault(builder.getInterval(), TimeUnit.MINUTES);

        final StatelessSupplier supplier = new StatelessSupplier(beanContext);
        builder.setSupplier(supplier);
        builder.setExecutor(executor);
        builder.setScheduledExecutor(scheduledExecutor);


        final InstanceManagerData data = new InstanceManagerData(builder.build(), accessTimeout, closeTimeout);

        StatelessContext sessionContext = new StatelessContext(securityService, new Flushable() {
            @Override
            public void flush() throws IOException {
                data.flush();
            }
        });
        data.setSessionContext(sessionContext);

        beanContext.setContainerData(data);

        beanContext.set(EJBContext.class, data.getSessionContext());

        try {
            final Context context = beanContext.getJndiEnc();
            context.bind("comp/EJBContext", data.getSessionContext());
            context.bind("comp/WebServiceContext", new EjbWsContext(sessionContext));
            context.bind("comp/TimerService", new TimerServiceWrapper());
        } catch (final NamingException e) {
            throw new OpenEJBException("Failed to bind EJBContext/WebServiceContext/TimerService", e);
        }

        final int min = builder.getMin();
        final long maxAge = builder.getMaxAge().getTime(TimeUnit.MILLISECONDS);
        final double maxAgeOffset = builder.getMaxAgeOffset();

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("EJBModule", beanContext.getModuleID());
        jmxName.set("StatelessSessionBean", beanContext.getEjbName());
        jmxName.set("name", beanContext.getEjbName());

        final MBeanServer server = LocalMBeanServer.get();

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {
            StatsInterceptor stats = null;
            for (final InterceptorInstance interceptor : beanContext.getUserAndSystemInterceptors()) {
                if (interceptor.getInterceptor() instanceof StatsInterceptor) {
                    stats = (StatsInterceptor) interceptor.getInterceptor();
                }
            }
            if (stats == null) { // normally useless
                stats = new StatsInterceptor(beanContext.getBeanClass());
                beanContext.addFirstSystemInterceptor(stats);
            }

            // register the invocation stats interceptor
            try {
                final ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(stats), objectName);
                data.add(objectName);
            } catch (final Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        // register the pool
        try {
            final ObjectName objectName = jmxName.set("j2eeType", "Pool").build();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
            server.registerMBean(new ManagedMBean(data.getPool()), objectName);
            data.add(objectName);
        } catch (final Exception e) {
            logger.error("Unable to register MBean ", e);
        }

        // Finally, fill the pool and start it
        if (!options.get("BackgroundStartup", false) && min > 0) {
            final ExecutorService es = Executors.newFixedThreadPool(min);
            for (int i = 0; i < min; i++) {
                es.submit(new InstanceCreatorRunnable(maxAge, i, min, maxAgeOffset, data, supplier));
            }
            es.shutdown();
            try {
                es.awaitTermination(5, TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                logger.error("can't fill the stateless pool", e);
            }
        }

        data.getPool().start();
    }
}
