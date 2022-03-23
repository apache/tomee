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
import org.apache.openejb.SystemException;

import jakarta.resource.spi.ApplicationServerInternalException;
import jakarta.resource.spi.UnavailableException;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;

public class EndpointHandler extends AbstractEndpointHandler {


    private final BeanContext deployment;
    private final MdbInstanceFactory instanceFactory;
    private final XAResource xaResource;


    public EndpointHandler(final BaseMdbContainer container, final BeanContext deployment, final MdbInstanceFactory instanceFactory, final XAResource xaResource) throws UnavailableException {
        super(container);
        this.deployment = deployment;
        this.instanceFactory = instanceFactory;
        this.xaResource = xaResource;
        instance = instanceFactory.createInstance(false);
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
            container.beforeDelivery(deployment, instance, method, xaResource);
        } catch (final SystemException se) {
            final Throwable throwable = se.getRootCause() != null ? se.getRootCause() : se;
            throw new ApplicationServerInternalException(throwable);
        }

        // before completed successfully we are now ready to invoke bean
        state = State.BEFORE_CALLED;
    }

    @Override
    protected void recreateInstance(final boolean exceptionAlreadyThrown) throws UnavailableException {
        try {
            instance = instanceFactory.recreateInstance(instance);
        } catch (final UnavailableException e) {
            // an error occured wile attempting to create the replacement instance
            // this endpoint is now failed
            state = State.RELEASED;

            // if bean threw an exception, do not override that exception
            if (!exceptionAlreadyThrown) {
                throw e;
            }
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
            instanceFactory.freeInstance((Instance) instance, false);
            instance = null;
        }
    }
}
