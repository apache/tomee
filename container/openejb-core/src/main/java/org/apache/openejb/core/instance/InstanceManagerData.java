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

package org.apache.openejb.core.instance;

import org.apache.openejb.core.stateless.StatelessContext;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Pool;

import javax.ejb.SessionContext;
import javax.management.ObjectName;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class InstanceManagerData {

    private final Pool<InstanceManager.Instance> pool;
    private final Duration accessTimeout;
    private final Duration closeTimeout;
    private final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
    private final SessionContext sessionContext;
    private final SecurityService securityService;

    public InstanceManagerData(final Pool<InstanceManager.Instance> pool, final Duration accessTimeout, final Duration closeTimeout,
                               SecurityService securityService) {
        this.pool = pool;
        this.accessTimeout = accessTimeout;
        this.closeTimeout = closeTimeout;
        this.securityService = securityService;
        this.sessionContext = new StatelessContext(securityService, new Flushable() {
            @Override
            public void flush() throws IOException {
                getPool().flush();
            }
        });
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public Pool<InstanceManager.Instance>.Entry poolPop() throws InterruptedException, TimeoutException {
        return pool.pop(accessTimeout.getTime(), accessTimeout.getUnit());
    }

    public Pool<InstanceManager.Instance> getPool() {
        return pool;
    }

    public boolean closePool() throws InterruptedException {
        return pool.close(closeTimeout.getTime(), closeTimeout.getUnit());
    }

    public ObjectName add(final ObjectName name) {
        jmxNames.add(name);
        return name;
    }

    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public List<ObjectName> getJmxNames() {
        return jmxNames;
    }


}
