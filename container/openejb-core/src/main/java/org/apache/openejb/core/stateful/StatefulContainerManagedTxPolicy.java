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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.stateful;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;

public class StatefulContainerManagedTxPolicy extends org.apache.openejb.core.transaction.TransactionPolicy {

    protected TransactionPolicy policy;

    public StatefulContainerManagedTxPolicy(TransactionPolicy policy) {
        super(policy.getPolicyType(), policy.getContainer());
        this.policy = policy;
        if (container.getContainerType() != ContainerType.STATEFUL) {
            throw new IllegalArgumentException();
        }
    }

    public String policyToString() {
        return policy.policyToString();
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException {
        policy.beforeInvoke(instance, context);
    }

    public void afterInvoke(Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {
        policy.afterInvoke(instance, context);
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {
        policy.handleApplicationException(appException, context);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {
        try {
            policy.handleSystemException(sysException, instance, context);
        } catch (ApplicationException e) {
            throw new InvalidateReferenceException(e.getRootCause());
        }
    }

}

