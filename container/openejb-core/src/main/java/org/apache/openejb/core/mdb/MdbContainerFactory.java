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

import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Pool;

import jakarta.resource.spi.ResourceAdapter;
import java.util.Properties;

public class MdbContainerFactory {


    private Object id;
    private SecurityService securityService;
    private ResourceAdapter resourceAdapter;
    private Class messageListenerInterface;
    private Class activationSpecClass;
    private int instanceLimit;
    private boolean failOnUnknownActivationSpec;
    private Duration accessTimeout;
    private Duration closeTimeout;
    private Pool.Builder poolBuilder = new Pool.Builder();
    private int callbackThreads = 5;
    private boolean useOneSchedulerThreadByBean = false;
    private int evictionThreads = 1;
    private boolean pool;
    private Properties properties = new Properties();



    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public Class getMessageListenerInterface() {
        return messageListenerInterface;
    }

    public void setMessageListenerInterface(Class messageListenerInterface) {
        this.messageListenerInterface = messageListenerInterface;
    }

    public Class getActivationSpecClass() {
        return activationSpecClass;
    }

    public void setActivationSpecClass(Class activationSpecClass) {
        this.activationSpecClass = activationSpecClass;
    }

    public int getInstanceLimit() {
        return instanceLimit;
    }

    /**
     * @deprecated use MaxSize
     */
    @Deprecated
    public void setInstanceLimit(int instanceLimit) {
        setMaxSize(instanceLimit);
    }

    public void setMaxSize(final int max) {
        this.instanceLimit = max;
        this.poolBuilder.setPoolSize(max);
    }

    public void setMinSize(final int min) {
        this.poolBuilder.setMinSize(min);
    }

    public boolean isFailOnUnknownActivationSpec() {
        return failOnUnknownActivationSpec;
    }

    public void setFailOnUnknownActivationSpec(boolean failOnUnknownActivationSpec) {
        this.failOnUnknownActivationSpec = failOnUnknownActivationSpec;
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public Duration getCloseTimeout() {
        return closeTimeout;
    }

    public void setCloseTimeout(Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    public Pool.Builder getPoolBuilder() {
        return poolBuilder;
    }

    public void setPoolBuilder(Pool.Builder poolBuilder) {
        this.poolBuilder = poolBuilder;
    }

    public int getCallbackThreads() {
        return callbackThreads;
    }

    public void setCallbackThreads(int callbackThreads) {
        this.callbackThreads = callbackThreads;
    }

    public boolean isUseOneSchedulerThreadByBean() {
        return useOneSchedulerThreadByBean;
    }

    public void setUseOneSchedulerThreadByBean(boolean useOneSchedulerThreadByBean) {
        this.useOneSchedulerThreadByBean = useOneSchedulerThreadByBean;
    }

    public int getEvictionThreads() {
        return evictionThreads;
    }

    public void setEvictionThreads(int evictionThreads) {
        this.evictionThreads = evictionThreads;
    }

    public void setStrictPooling(final boolean strict) {
        poolBuilder.setStrictPooling(strict);
    }

    public void setMaxAge(final Duration maxAge) {
        poolBuilder.setMaxAge(maxAge);
    }

    public void setIdleTimeout(final Duration idleTimeout) {
        poolBuilder.setIdleTimeout(idleTimeout);
    }

    public void setSweepInterval(final Duration interval) {
        poolBuilder.setSweepInterval(interval);
    }

    public void setReplaceAged(final boolean replaceAged) {
        poolBuilder.setReplaceAged(replaceAged);
    }

    public void setReplaceFlushed(final boolean replaceFlushed) {
        poolBuilder.setReplaceFlushed(replaceFlushed);
    }

    public void setGarbageCollection(final boolean garbageCollection) {
        poolBuilder.setGarbageCollection(garbageCollection);
    }

    public void setMaxAgeOffset(final double maxAgeOffset) {
        poolBuilder.setMaxAgeOffset(maxAgeOffset);
    }

    public boolean isPool() {
        return pool;
    }

    public void setPool(boolean pool) {
        this.pool = pool;
    }

    public Properties getProperties() {
        return properties;
    }

    public BaseMdbContainer create() {

        if (pool) {
            final MdbPoolContainer mdbPoolContainer = new MdbPoolContainer(id, securityService, resourceAdapter,
                    messageListenerInterface, activationSpecClass,
                    failOnUnknownActivationSpec, accessTimeout, closeTimeout, poolBuilder,
                    callbackThreads, useOneSchedulerThreadByBean, evictionThreads);

            mdbPoolContainer.getProperties().putAll(this.getProperties());
            return mdbPoolContainer;
        } else {
            final MdbContainer mdbContainer = new MdbContainer(id, securityService, resourceAdapter,
                    messageListenerInterface, activationSpecClass, instanceLimit,
                    failOnUnknownActivationSpec);

            mdbContainer.getProperties().putAll(this.getProperties());
            return mdbContainer;
        }
    }
}
