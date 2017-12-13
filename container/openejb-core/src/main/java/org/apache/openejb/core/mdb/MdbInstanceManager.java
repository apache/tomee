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

package org.apache.openejb.core.mdb;

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.instance.InstanceManager;
import org.apache.openejb.loader.Options;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Pool;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import java.util.concurrent.ScheduledExecutorService;

public class MdbInstanceManager extends InstanceManager {

    public MdbInstanceManager(final SecurityService securityService,
                                    final Duration accessTimeout, final Duration closeTimeout,
                                    final Pool.Builder poolBuilder, final int callbackThreads,
                                    final ScheduledExecutorService ses) {
        super(securityService, accessTimeout, closeTimeout, poolBuilder, callbackThreads, ses);
    }


    @SuppressWarnings("unchecked")
    public void deploy(final BeanContext beanContext) throws OpenEJBException {

    }
}
