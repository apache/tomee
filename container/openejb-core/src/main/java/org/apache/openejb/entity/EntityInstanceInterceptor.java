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
package org.apache.openejb.entity;

import java.rmi.NoSuchObjectException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EntityEjbDeployment;
import org.apache.openejb.NotReentrantException;
import org.apache.openejb.NotReentrantLocalException;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.cache.InstancePool;

/**
 * Simple Instance Interceptor that does not cache instances in the ready state
 * but passivates between each invocation.
 *
 * @version $Revision$ $Date$
 */
public final class EntityInstanceInterceptor implements Interceptor {
    private final Interceptor next;

    public EntityInstanceInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EjbDeployment deployment = ejbInvocation.getEjbDeployment();
        if (!(deployment instanceof EntityEjbDeployment)) {
            throw new IllegalArgumentException("EntityInstanceInterceptor can only be used with a EntityEjbDeploymentContext: " + deployment.getClass().getName());
        }

        EntityEjbDeployment entityEjbDeploymentContext = ((EntityEjbDeployment) deployment);
        String containerId = entityEjbDeploymentContext.getContainerId();
        InstancePool pool = entityEjbDeploymentContext.getInstancePool();

        EjbTransactionContext ejbTransactionContext = ejbInvocation.getEjbTransactionData();
        Object id = ejbInvocation.getId();

        // get the context
        EntityInstanceContext ctx = null;

        // if we have an id then check if there is already a context associated with the transaction
        if (id != null) {
            ctx = (EntityInstanceContext) ejbTransactionContext.getContext(containerId, id);
            // if we have a dead context, the cached context was discarded, so we need clean it up and get a new one
            if (ctx != null && ctx.isDead()) {
                ejbTransactionContext.unassociate(ctx);
                ctx = null;
            }
        }

        // if we didn't find an existing context, create a new one.
        if (ctx == null) {
            ctx = (EntityInstanceContext) pool.acquire();
            ctx.setId(id);
            ctx.setPool(pool);
            ctx.setEjbTransactionData(ejbTransactionContext);
        }

        // set the instanct into the invocation
        ejbInvocation.setEJBInstanceContext(ctx);

        // check reentrancy
        if (!entityEjbDeploymentContext.isReentrant() && ctx.isInCall()) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NotReentrantLocalException("" + containerId);
            } else {
                throw new NotReentrantException("" + containerId);
            }
        }

        // associates the context with the transaction, this may result an a load that throws
        // and NoSuchEntityException, which needs to be converted to the a NoSuchObject[Local]Exception
        EJBInstanceContext oldContext = null;
        try {
            oldContext = ejbTransactionContext.beginInvocation(ctx);
        } catch (NoSuchEntityException e) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NoSuchObjectLocalException().initCause(e);
            } else {
                throw new NoSuchObjectException(e.getMessage());
            }
        }

        // send the invocation down the chain
        try {
            InvocationResult result = next.invoke(invocation);
            return result;
        } catch (Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();
            // id may have been set during create
            if (id == null) {
                id = ctx.getId();
            }
            // if we have an id unassociate the context
            if (id != null) {
                ejbTransactionContext.unassociate(containerId, id);
            }
            throw t;
        } finally {
            ejbTransactionContext.endInvocation(oldContext);
            ejbInvocation.setEJBInstanceContext(null);
        }
    }
}
