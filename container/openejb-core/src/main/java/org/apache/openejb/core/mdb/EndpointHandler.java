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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreDeploymentInfo;

import javax.ejb.EJBException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

public class EndpointHandler implements InvocationHandler, MessageEndpoint {
    private static enum State {
        /**
         * The handler has been initialized and is ready for invoation
         */
        NONE,

        /**
         * The beforeDelivery method has been called, and the next method called must be a message delivery method
         * or release.
         */
        BEFORE_CALLED,

        /**
         * The message delivery method has been called successfully, and the next method called must be afterDelivery
         * or release.
         */
        METHOD_CALLED,

        /**
         * The message delivery threw a system exception, and the next method called must be afterDelivery
         * or release.  This state notified the afterDelivery method that the instace must be replaced with a new
         * instance.
         */
        SYSTEM_EXCEPTION,

        /**
         * This message endpoint handler has been released and can no longer be used.
         */
        RELEASED
    }

    private final MdbContainer container;
    private final CoreDeploymentInfo deployment;
    private final MdbInstanceFactory instanceFactory;
    private final XAResource xaResource;

    private State state = State.NONE;
    private Object instance;

    public EndpointHandler(MdbContainer container, CoreDeploymentInfo deployment, MdbInstanceFactory instanceFactory, XAResource xaResource) throws UnavailableException {
        this.container = container;
        this.deployment = deployment;
        this.instanceFactory = instanceFactory;
        this.xaResource = xaResource;
        instance = instanceFactory.createInstance(false);
    }

//    private static void logTx() {
//        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
//        Transaction transaction = null;
//        String status = "ERROR";
//        try {
//            transaction = transactionManager.getTransaction();
//            int txStatus;
//            if (transaction != null) {
//                txStatus = transaction.getStatus();
//            } else {
//                txStatus = Status.STATUS_NO_TRANSACTION;
//            }
//            switch (txStatus) {
//                case Status.STATUS_ACTIVE:
//                    status = "STATUS_ACTIVE";
//                    break;
//                case Status.STATUS_MARKED_ROLLBACK:
//                    status = "MARKED_ROLLBACK";
//                    break;
//                case Status.STATUS_PREPARED:
//                    status = "PREPARED";
//                    break;
//                case Status.STATUS_COMMITTED:
//                    status = "COMMITTED";
//                    break;
//                case Status.STATUS_ROLLEDBACK:
//                    status = "ROLLEDBACK";
//                    break;
//                case Status.STATUS_UNKNOWN:
//                    status = "UNKNOWN";
//                    break;
//                case Status.STATUS_NO_TRANSACTION:
//                    status = "NO_TRANSACTION";
//                    break;
//                case Status.STATUS_PREPARING:
//                    status = "PREPARING";
//                    break;
//                case Status.STATUS_COMMITTING:
//                    status = "COMMITTING";
//                    break;
//                case Status.STATUS_ROLLING_BACK:
//                    status = "ROLLING_BACK";
//                    break;
//                default:
//                    status = "UNKNOWN " + txStatus;
//            }
//        } catch (javax.transaction.SystemException e) {
//        }
//        System.out.println("\n" +
//                "***************************************\n" +
//                "transaction " + transaction + "\n" +
//                "     status " + status + "\n" +
//                "***************************************\n\n");
//
//    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        System.out.println("\n" +
//                "***************************************\n" +
//                "Endpoint invoked " + method + "\n" +
//                "***************************************\n\n");

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

//        try {
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
//        } finally { logTx(); }
    }

    public void beforeDelivery(Method method) throws ApplicationServerInternalException {
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
        } catch (SystemException se) {
            Throwable throwable = (se.getRootCause() != null) ? se.getRootCause() : se;
            throw new ApplicationServerInternalException(throwable);
        }

        // before completed successfully we are now ready to invoke bean
        state = State.BEFORE_CALLED;
    }

    public Object deliverMessage(Method method, Object[] args) throws Throwable {
        // verify current state
        switch (state) {
            case RELEASED:
                throw new IllegalStateException("Message endpoint factory has been released");
            case BEFORE_CALLED:
                state = State.METHOD_CALLED;
                break;
            case METHOD_CALLED:
            case SYSTEM_EXCEPTION:
                throw new IllegalStateException("The last message delivery must be completed with an afterDeliver before another message can be delivered");
        }


        // if beforeDelivery was not called, call it now
        boolean callBeforeAfter = (state == State.NONE);
        if (callBeforeAfter) {
            try {
                beforeDelivery(method);
            } catch (ApplicationServerInternalException e) {
                throw (EJBException) new EJBException().initCause(e.getCause());
            }
        }

        Throwable throwable = null;
        Object value = null;
        try {
            // deliver the message
            value = container.invoke(instance, method, args);
        } catch (SystemException se) {
            throwable = (se.getRootCause() != null) ? se.getRootCause() : se;
            state = State.SYSTEM_EXCEPTION;
        } catch (ApplicationException ae) {
            throwable = (ae.getRootCause() != null) ? ae.getRootCause() : ae;
        } finally {
            // if the adapter is not using before/after, we must call afterDelivery to clean up
            if (callBeforeAfter) {
                try {
                    afterDelivery();
                } catch (ApplicationServerInternalException e) {
                    throwable = throwable == null ? e.getCause() : throwable;
                } catch (UnavailableException e) {
                    throwable = throwable == null ? e : throwable;
                }
            }
        }

        if (throwable != null) {
            throwable.printStackTrace();
            if (isValidException(method, throwable)) {
                throw throwable;
            } else {
                throw new EJBException().initCause(throwable);
            }
        }
        return value;
    }

    public void afterDelivery() throws ApplicationServerInternalException, UnavailableException {
        // verify current state
        switch (state) {
            case RELEASED:
                throw new IllegalStateException("Message endpoint factory has been released");
            case BEFORE_CALLED:
                throw new IllegalStateException("Exactally one message must be delivered between beforeDelivery and afterDelivery");
            case NONE:
                throw new IllegalStateException("afterDelivery may only be called if message delivery began with a beforeDelivery call");
        }


        // call afterDelivery on the container
        boolean exceptionThrown = false;
        try {
            container.afterDelivery(instance);
        } catch (SystemException se) {
            exceptionThrown = true;

            Throwable throwable = (se.getRootCause() != null) ? se.getRootCause() : se;
            throwable.printStackTrace();
            throw new ApplicationServerInternalException(throwable);
        } finally {
            if (state == State.SYSTEM_EXCEPTION) {
                recreateInstance(exceptionThrown);
            }
            // we are now in the default NONE state
            state = State.NONE;
        }
    }

    private void recreateInstance(boolean exceptionAlreadyThrown) throws UnavailableException {
        try {
            instance = instanceFactory.recreateInstance(instance);
        } catch (UnavailableException e) {
            // an error occured wile attempting to create the replacement instance
            // this endpoint is now failed
            state = State.RELEASED;

            // if bean threw an exception, do not override that exception
            if (!exceptionAlreadyThrown) {
                throw e;
            }
        }
    }

    public void release() {
        if (state == State.RELEASED) return;
        state = State.RELEASED;

        // notify the container
        try {
            container.release(deployment, instance);
        } finally {
            instanceFactory.freeInstance(instance, false);
            instance = null;
        }
    }

    private boolean isValidException(Method method, Throwable throwable) {
        if (throwable instanceof RuntimeException || throwable instanceof Error) return true;

        Class<?>[] exceptionTypes = method.getExceptionTypes();
        for (Class<?> exceptionType : exceptionTypes) {
            if (exceptionType.isInstance(throwable)) {
                return true;
            }
        }
        return false;
    }
}
