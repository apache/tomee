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
package org.apache.openejb.core.mdb;

import org.apache.openejb.DeploymentInfo;

import javax.ejb.EJBException;
import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Container for the local interface of a Message Driven Bean.
 * This container owns implementations of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 * <p/>
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * The J2EE connector and EJB specifications are not clear on what happens when beforeDelivery or
 * afterDelivery throw an exception, so here is what we have decided:
 * <p/>
 * Exception from beforeDelivery:
 * if container started TX, roll it back
 * reset class loader to adapter classloader
 * reset state to STATE_NONE
 * <p/>
 * Exception from delivery method:
 * if container started TX, roll it back
 * reset class loader to adapter classloader
 * if state was STATE_BEFORE_CALLED, set state to STATE_ERROR so after can still be called
 * <p/>
 * Exception from afterDelivery:
 * if container started TX, roll it back
 * reset class loader to adapter classloader
 * reset state to STATE_NONE
 * <p/>
 * One subtle side effect of this is if the adapter ignores an exception from beforeDelivery and
 * continues with delivery and afterDelivery, the delivery will be treated as a single standalone
 * delivery and the afterDelivery will throw an IllegalStateException.
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class EndpointHandler implements InvocationHandler {
    private static enum State {
        NONE, BEFORE_CALLED, METHOD_CALLED
    }

    private final MdbContainer container;
    private final DeploymentInfo deployment;
    private final Object instance;

    private boolean released = false;
    private State state = State.NONE;
    private ClassLoader adapterClassLoader;

    public EndpointHandler(MdbContainer container, DeploymentInfo deployment, Object instance, XAResource xaResource) {
        this.container = container;
        this.deployment = deployment;
        this.instance = instance;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (method.getDeclaringClass() == Object.class) {
            if ("toString".equals(methodName) && parameterTypes.length == 0) {
                return toString();
            } else if ("equals".equals(methodName) && parameterTypes.length == 1) {
                return equals(args[0]);
            } else if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
                return hashCode();
            } else {
                throw new UnsupportedOperationException("Unkown method: " + method);
            }
        }

        if ("beforeDelivery".equals(methodName) && Arrays.deepEquals(new Class[] {Method.class}, parameterTypes)) {
            beforeDelivery((Method) args[0]);
            return null;
        } else if ("afterDelivery".equals(methodName) && parameterTypes.length == 0) {
            afterDelivery();
            return null;
        } else if ("release".equals(methodName) && parameterTypes.length == 0) {
            release();
            return null;
        } else {
            Object value = deliverMessage(method, args);
            return value;
        }

    }

    public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
        // verify current state
        if (released) throw new IllegalStateException("Proxy has been released");
        switch (state) {
            case BEFORE_CALLED:
                throw new IllegalStateException("beforeDelivery can not be called again until message is delivered and afterDelivery is called");
            case METHOD_CALLED:
                throw new IllegalStateException("The last message delivery must be completed with an afterDeliver before beforeDeliver can be called again");
        }

        // call afterDelivery on the container
        installAppClassLoader();
        try {
            container.beforeDelivery(deployment.getDeploymentID(), instance, method);
        } catch (NoSuchMethodException e) {
            restoreAdapterClassLoader();
            throw e;
        } catch (ResourceException e) {
            restoreAdapterClassLoader();
            throw e;
        } catch (Throwable throwable) {
            restoreAdapterClassLoader();
            throw new ResourceException(throwable);
        }

        // before completed successfully we are now ready to invoke bean
        state = State.BEFORE_CALLED;
    }

    public Object deliverMessage(Method method, Object[] args) throws Throwable {
        // verify current state
        if (released) throw new IllegalStateException("Proxy has been released");
        switch (state) {
            case BEFORE_CALLED:
                state = State.METHOD_CALLED;
            case METHOD_CALLED:
                throw new IllegalStateException("The last message delivery must be completed with an afterDeliver before another message can be delivered");
        }


        // if beforeDelivery was not called, call it now
        if (state == State.NONE) {
            try {
                container.beforeDelivery(deployment.getDeploymentID(), instance, method);
            } catch (Throwable throwable) {
                if (throwable instanceof EJBException) {
                    throw (EJBException) throwable;
                }
                throw (EJBException) new EJBException().initCause(throwable);
            }
        }

        boolean exceptionThrown = false;
        try {
            Object value = container.invoke(instance, method, args);
            return value;
        } catch (Throwable throwable) {
            exceptionThrown = true;
            throw throwable;
        } finally {
            // if the adapter is not using before/after, we must call afterDelivery to clean up
            if (state == State.NONE) {
                try {
                    container.afterDelivery(instance);
                } catch (Throwable throwable) {
                    // if bean threw an exception, do not override that exception
                    if (!exceptionThrown) {
                        EJBException ejbException;
                        if (throwable instanceof EJBException) {
                            ejbException = (EJBException) throwable;
                        } else {
                            ejbException = new EJBException();
                            ejbException.initCause(throwable);
                        }
                        throw ejbException;
                    }
                }
            }
        }
    }

    public void afterDelivery() throws ResourceException {
        // verify current state
        if (released) throw new IllegalStateException("Proxy has been released");
        switch (state) {
            case BEFORE_CALLED:
                throw new IllegalStateException("Exactally one message must be delivered between beforeDelivery and afterDelivery");
            case NONE:
                throw new IllegalStateException("afterDelivery may only be called if message delivery began with a beforeDelivery call");
        }


        // call afterDelivery on the container
        try {
            container.afterDelivery(instance);
        } catch (ResourceException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ResourceException(throwable);
        } finally {
            // we are now in the default NONE state
            state = State.NONE;
            restoreAdapterClassLoader();
        }
    }

    public void release() {
        if (released) return;
        released = true;

        // notify the container
        try {
            container.release(instance);
        } finally {
            restoreAdapterClassLoader();
        }
    }

    private void installAppClassLoader() {
        Thread currentThread = Thread.currentThread();

        adapterClassLoader = currentThread.getContextClassLoader();
        if (adapterClassLoader != deployment.getClassLoader()) {
            currentThread.setContextClassLoader(deployment.getClassLoader());
        }
    }

    private void restoreAdapterClassLoader() {
        if (adapterClassLoader != deployment.getClassLoader()) {
            Thread.currentThread().setContextClassLoader(adapterClassLoader);
        }
        adapterClassLoader = null;
    }
}
