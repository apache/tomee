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

package org.apache.openejb.core.entity;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;

import jakarta.transaction.TransactionSynchronizationRegistry;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

public class EntrancyTracker {
    /**
     * Thread local used to track the insances in the current call stack so we can determine if an nonreentrant
     * instance is being reentered.
     */
    private final ThreadLocal<Set<InstanceKey>> inCallThreadLocal = new ThreadLocal<Set<InstanceKey>>() {
        protected Set<InstanceKey> initialValue() {
            return new HashSet<>();
        }
    };

    private final TransactionSynchronizationRegistry synchronizationRegistry;

    public EntrancyTracker(final TransactionSynchronizationRegistry synchronizationRegistry) {
        this.synchronizationRegistry = synchronizationRegistry;
    }

    public void enter(final BeanContext beanContext, final Object primaryKey) throws ApplicationException {
        if (primaryKey == null || beanContext.isReentrant()) {
            return;
        }

        final Object deploymentId = beanContext.getDeploymentID();
        final InstanceKey key = new InstanceKey(deploymentId, primaryKey);


        Set<InstanceKey> inCall;
        try {
            //noinspection unchecked
            inCall = (Set<InstanceKey>) synchronizationRegistry.getResource(EntrancyTracker.class);
            if (inCall == null) {
                inCall = new HashSet<>();
                synchronizationRegistry.putResource(EntrancyTracker.class, inCall);
            }
        } catch (final IllegalStateException e) {
            inCall = inCallThreadLocal.get();
        }

        if (!inCall.add(key)) {
            final ApplicationException exception = new ApplicationException(new RemoteException("Attempted reentrant access. " + "Bean " + deploymentId + " is not reentrant and instance " + primaryKey + " has already been entered : " + inCall));
            exception.printStackTrace();
            throw exception;
        }

    }

    public void exit(final BeanContext beanContext, final Object primaryKey) throws ApplicationException {
        if (primaryKey == null || beanContext.isReentrant()) {
            return;
        }

        final Object deploymentId = beanContext.getDeploymentID();
        final InstanceKey key = new InstanceKey(deploymentId, primaryKey);

        Set<InstanceKey> inCall = null;
        try {
            //noinspection unchecked
            inCall = (Set<InstanceKey>) synchronizationRegistry.getResource(EntrancyTracker.class);
        } catch (final IllegalStateException e) {
            inCall = inCallThreadLocal.get();
        }

        if (inCall != null) {
            inCall.remove(key);
        }
    }

    private static class InstanceKey {
        private final Object deploymentId;
        private final Object primaryKey;


        public InstanceKey(final Object deploymentId, final Object primaryKey) {
            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final InstanceKey that = (InstanceKey) o;

            return deploymentId.equals(that.deploymentId) && primaryKey.equals(that.primaryKey);
        }

        public int hashCode() {
            int result;
            result = deploymentId.hashCode();
            result = 31 * result + primaryKey.hashCode();
            return result;
        }


        public String toString() {
            return deploymentId + ":" + primaryKey;
        }
    }
}
