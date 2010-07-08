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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

    public void setCallbackThreads(int callbackThreads) {
        this.callbackThreads = callbackThreads;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    /**
     * Alias for AccessTimeout
     * backwards compatibility
     *
     * @deprecated use AccessTimeout
     * @param accessTimeout
     */
    public void setTimeOut(Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    /**
     *
     * @param accessTimeout
     */
    public void setAccessTimeout(Duration accessTimeout) {
        if (this.accessTimeout == null) setTimeOut(accessTimeout);
    }

    public void setMaxSize(int max) {
        if (this.max == null) setPoolSize(max);
    }

    /**
     * @deprecated use MaxSize
     * @param max
     */
    public void setPoolSize(int max) {
        this.max = max;
        pool.setPoolSize(max);
    }

    public void setMinSize(int min) {
        pool.setMinSize(min);
    }

    public void setStrictPooling(boolean strict) {
        pool.setStrictPooling(strict);
    }

    public void setMaxAge(Duration maxAge) {
        pool.setMaxAge(maxAge);
    }

    public void setIdleTimeout(Duration idleTimeout) {
        pool.setIdleTimeout(idleTimeout);
    }

    public void setSweepInterval(Duration interval) {
        pool.setSweepInterval(interval);
    }

    public void setReplaceAged(boolean replaceAged) {
        pool.setReplaceAged(replaceAged);
    }

    public void setReplaceFlushed(boolean replaceFlushed) {
        pool.setReplaceFlushed(replaceFlushed);
    }

    public void setGarbageCollection(boolean garbageCollection) {
        pool.setGarbageCollection(garbageCollection);
    }

    public void setMaxAgeOffset(double maxAgeOffset) {
        pool.setMaxAgeOffset(maxAgeOffset);
    }

    public void setCloseTimeout(Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    public StatelessContainer create() {
        return new StatelessContainer(id, securityService, accessTimeout, closeTimeout, pool, callbackThreads);
    }
}
