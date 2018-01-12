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

import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Pool;

/**
 * @version $Rev$ $Date$
 */
public class StatelessContainerFactory {

    private final Pool.Builder pool = new Pool.Builder();

    private Integer max;
    private Object id;
    private SecurityService securityService;
    private Duration accessTimeout;
    private int callbackThreads = 5;
    private Duration closeTimeout;
    private boolean useOneSchedulerThreadByBean;
    private int evictionThreads = 1;

    public void setCallbackThreads(final int callbackThreads) {
        this.callbackThreads = callbackThreads;
    }

    public void setId(final Object id) {
        this.id = id;
    }

    public void setSecurityService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    /**
     * Alias for AccessTimeout
     * backwards compatibility
     *
     * @param accessTimeout Duration
     * @deprecated use AccessTimeout
     */
    @Deprecated
    public void setTimeOut(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    /**
     * @param accessTimeout Duration
     */
    public void setAccessTimeout(final Duration accessTimeout) {
        if (this.accessTimeout == null) {
            setTimeOut(accessTimeout);
        }
    }

    public void setMaxSize(final int max) {
        if (this.max == null) {
            setPoolSize(max);
        }
    }

    /**
     * @param max int
     * @deprecated use MaxSize
     */
    @Deprecated
    public void setPoolSize(final int max) {
        this.max = max;
        pool.setPoolSize(max);
    }

    public void setMinSize(final int min) {
        pool.setMinSize(min);
    }

    public void setStrictPooling(final boolean strict) {
        pool.setStrictPooling(strict);
    }

    public void setMaxAge(final Duration maxAge) {
        pool.setMaxAge(maxAge);
    }

    public void setIdleTimeout(final Duration idleTimeout) {
        pool.setIdleTimeout(idleTimeout);
    }

    public void setSweepInterval(final Duration interval) {
        pool.setSweepInterval(interval);
    }

    public void setReplaceAged(final boolean replaceAged) {
        pool.setReplaceAged(replaceAged);
    }

    public void setReplaceFlushed(final boolean replaceFlushed) {
        pool.setReplaceFlushed(replaceFlushed);
    }

    public void setGarbageCollection(final boolean garbageCollection) {
        pool.setGarbageCollection(garbageCollection);
    }

    public void setMaxAgeOffset(final double maxAgeOffset) {
        pool.setMaxAgeOffset(maxAgeOffset);
    }

    public void setCloseTimeout(final Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    public void setUseOneSchedulerThreadByBean(final boolean useOneSchedulerThreadByBean) {
        this.useOneSchedulerThreadByBean = useOneSchedulerThreadByBean;
    }

    public void setEvictionThreads(final int evictionThreads) {
        this.evictionThreads = evictionThreads;
    }

    public StatelessContainer create() {
        return new StatelessContainer(id, securityService, accessTimeout, closeTimeout, pool, callbackThreads,
                useOneSchedulerThreadByBean, evictionThreads);
    }
}
