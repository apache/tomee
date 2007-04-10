/**
 *
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
package org.apache.openejb.core.entity;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ApplicationException;

import javax.transaction.TransactionSynchronizationRegistry;
import java.util.Set;
import java.util.HashSet;
import java.rmi.RemoteException;

public class EntrancyTracker {
    /**
     * Thread local used to track the insances in the current call stack so we can determine if an nonreentrant
     * instance is being reentered.
     */
    private final ThreadLocal<Set<InstanceKey>> inCallThreadLocal = new ThreadLocal<Set<InstanceKey>>() {
        protected Set<InstanceKey> initialValue() {
            return new HashSet<InstanceKey>();
        }
    };

    private final TransactionSynchronizationRegistry synchronizationRegistry;

    public EntrancyTracker(TransactionSynchronizationRegistry synchronizationRegistry) {
        this.synchronizationRegistry = synchronizationRegistry;
    }

    public void enter(DeploymentInfo deploymentInfo, Object primaryKey) throws ApplicationException {
        if (primaryKey == null || deploymentInfo.isReentrant()) {
            return;
        }

        Object deploymentId = deploymentInfo.getDeploymentID();
        InstanceKey key = new InstanceKey(deploymentId, primaryKey);


        Set<InstanceKey> inCall;
        try {
            //noinspection unchecked
            inCall = (Set<InstanceKey>) synchronizationRegistry.getResource(EntrancyTracker.class);
            if (inCall == null) {
                inCall = new HashSet<InstanceKey>();
                synchronizationRegistry.putResource(EntrancyTracker.class, inCall);
            }
        } catch (IllegalStateException e) {
            inCall = inCallThreadLocal.get();
        }

        if (!inCall.add(key)) {
            ApplicationException exception = new ApplicationException(new RemoteException("Attempted reentrant access. " + "Bean " + deploymentId + " is not reentrant and instance " + primaryKey + " has already been entered : " +inCall));
            exception.printStackTrace();
            throw exception;
        }

    }

    public void exit(DeploymentInfo deploymentInfo, Object primaryKey) throws ApplicationException {
        if (primaryKey == null || deploymentInfo.isReentrant()) {
            return;
        }

        Object deploymentId = deploymentInfo.getDeploymentID();
        InstanceKey key = new InstanceKey(deploymentId, primaryKey);

        Set<InstanceKey> inCall = null;
        try {
            //noinspection unchecked
            inCall = (Set<InstanceKey>) synchronizationRegistry.getResource(EntrancyTracker.class);
        } catch (IllegalStateException e) {
            inCall = inCallThreadLocal.get();
        }

        if (inCall != null) {
            inCall.remove(key);
        }
    }

    private static class InstanceKey {
        private final Object deploymentId;
        private final Object primaryKey;


        public InstanceKey(Object deploymentId, Object primaryKey) {
            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InstanceKey that = (InstanceKey) o;

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
