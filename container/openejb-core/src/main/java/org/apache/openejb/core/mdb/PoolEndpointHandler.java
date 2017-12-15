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
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ThreadContext;

import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.UnavailableException;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;

public class PoolEndpointHandler extends AbstractEndpointHandler {


    private final BeanContext deployment;
    private final MdbInstanceManager instanceManager;
    private final XAResource xaResource;

    private ThreadContext callContext;

    public PoolEndpointHandler(final BaseMdbContainer container, final BeanContext deployment, final MdbInstanceManager instanceManager, final XAResource xaResource) throws UnavailableException {
        super(container);
        this.deployment = deployment;
        this.instanceManager = instanceManager;
        this.xaResource = xaResource;
        this.callContext = ThreadContext.getThreadContext();
    }

   @Override
    public void beforeDelivery(final Method method) throws ApplicationServerInternalException {
        // verify current state
        switch (state) {
            case RELEASED:
                throw new IllegalStateException("Message endpoint factory has been released");
            case BEFORE_CALLED:
                throw new IllegalStateException("beforeDelivery can not be called again until message is delivered and afterDelivery is called");
            case METHOD_CALLED:
            case SYSTEM_EXCEPTION:
                throw new IllegalStateException("The last message delivery must be completed with an afterDeliver before beforeDeliver can be called again");
        }

        // call beforeDelivery on the container
        try {
            instance = instanceManager.getInstance(new ThreadContext(deployment, null));
            container.beforeDelivery(deployment, instance, method, xaResource);
        } catch (final SystemException se) {
            final Throwable throwable = se.getRootCause() != null ? se.getRootCause() : se;
            throw new ApplicationServerInternalException(throwable);
        } catch (OpenEJBException oe) {
            throw new ApplicationServerInternalException(oe);
        }

        // before completed successfully we are now ready to invoke bean
        state = State.BEFORE_CALLED;
    }

    @Override
    protected void recreateInstance(boolean exceptionAlreadyThrown) throws UnavailableException {

    }


    public void afterDelivery() throws ApplicationServerInternalException, UnavailableException {
        // verify current state
        switch (state) {
            case RELEASED:
                throw new IllegalStateException("Message endpoint factory has been released");
            case NONE:
                throw new IllegalStateException("afterDelivery may only be called if message delivery began with a beforeDelivery call");
        }


        // call afterDelivery on the container
        boolean exceptionThrown = false;
        try {
            container.afterDelivery(instance);
        } catch (final SystemException se) {
            exceptionThrown = true;

            final Throwable throwable = se.getRootCause() != null ? se.getRootCause() : se;
            throwable.printStackTrace();
            throw new ApplicationServerInternalException(throwable);
        } finally {
            if (state == State.SYSTEM_EXCEPTION) {
//                recreateInstance(exceptionThrown);
            }
            // we are now in the default NONE state
            state = State.NONE;
        }
    }


    @Override
    public void release() {
        if (state == State.RELEASED) {
            return;
        }
        state = State.RELEASED;

        // notify the container
        try {
            container.release(deployment, instance);
        } finally {
            try {
                instanceManager.poolInstance(callContext, instance);
            } catch (OpenEJBException e) {
                e.printStackTrace();
            }
            instance = null;
        }
    }
}
