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

import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.stateful.StatefulInstanceManager.Instance;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionPolicy.TransactionSynchronization;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.ejb.SessionSynchronization;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.lang.reflect.Method;

public class SessionSynchronizationCoordinator implements TransactionSynchronization {
    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final Map<Object,Registration> registry = new HashMap<Object,Registration>();
    private final TransactionPolicy txPolicy;

    private SessionSynchronizationCoordinator(TransactionPolicy txPolicy) {
        this.txPolicy = txPolicy;
    }

    public static void registerSessionSynchronization(Instance instance, ThreadContext callContext)  {
        TransactionPolicy txPolicy = callContext.getTransactionPolicy();
        if (txPolicy == null) {
            throw new IllegalStateException("ThreadContext does not contain a TransactionEnvironment");
        }

        SessionSynchronizationCoordinator coordinator = (SessionSynchronizationCoordinator) txPolicy.getResource(SessionSynchronizationCoordinator.class);
        if (coordinator == null) {
            coordinator = new SessionSynchronizationCoordinator(txPolicy);
            txPolicy.registerSynchronization(coordinator);
            txPolicy.putResource(SessionSynchronizationCoordinator.class, coordinator);
        }

        // SessionSynchronization are only enabled for beans after CREATE that are not bean-managed and implement the SessionSynchronization interface
        boolean sessionSynchronization = callContext.getCurrentOperation() != Operation.CREATE &&
                callContext.getDeploymentInfo().isBeanManagedTransaction() &&
                instance.bean instanceof SessionSynchronization;

        coordinator.registerSessionSynchronization(instance, callContext.getDeploymentInfo(), callContext.getPrimaryKey(), sessionSynchronization);
    }

    private void registerSessionSynchronization(Instance instance, CoreDeploymentInfo deploymentInfo, Object primaryKey, boolean sessionSynchronization) {
        // register
        Registration registration = registry.get(primaryKey);
        if (registration == null) {
            registration = new Registration(deploymentInfo, primaryKey);
            registry.put(primaryKey, registration);
        }

        // check afterBegin has already been invoked or if this is not a session synchronization bean
        if (registration.sessionSynchronization || !sessionSynchronization) {
            return;
        }

        registration.sessionSynchronization = true;

        ThreadContext callContext = new ThreadContext(deploymentInfo, primaryKey);
        Operation currentOperation = callContext.getCurrentOperation();
        callContext.setCurrentOperation(Operation.AFTER_BEGIN);
        BaseContext.State[] originalStates = callContext.setCurrentAllowedStates(StatefulContext.getStates());

        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {

            Method afterBegin = SessionSynchronization.class.getMethod("afterBegin");

            List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(afterBegin);
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, afterBegin, Operation.AFTER_BEGIN, interceptors, instance.interceptors);
            interceptorStack.invoke();

        } catch (Exception e) {
            String message = "An unexpected system exception occured while invoking the afterBegin method on the SessionSynchronization object: " + e.getClass().getName() + " " + e.getMessage();
            logger.error(message, e);
            throw new RuntimeException(message, e);

        } finally {
            callContext.setCurrentOperation(currentOperation);
            callContext.setCurrentAllowedStates(originalStates);
            ThreadContext.exit(oldCallContext);
        }
    }

    public void beforeCompletion() {
        for (Registration registration : registry.values()) {
            // don't call beforeCompletion when transaction is marked rollback only
            if (txPolicy.isRollbackOnly()) return;

            // only call beforeCompletion on beans with session synchronization
            if (!registration.sessionSynchronization) return;

            ThreadContext callContext = new ThreadContext(registration.deploymentInfo, registration.primaryKey);
            ThreadContext oldCallContext = ThreadContext.enter(callContext);

            StatefulInstanceManager instanceManager = null;
            try {
                StatefulContainer container = (StatefulContainer) registration.deploymentInfo.getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operation.BEFORE_COMPLETION);
                callContext.setCurrentAllowedStates(StatefulContext.getStates());

                Instance instance = (Instance) instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);

                Method beforeCompletion = SessionSynchronization.class.getMethod("beforeCompletion");

                List<InterceptorData> interceptors = registration.deploymentInfo.getMethodInterceptors(beforeCompletion);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, beforeCompletion, Operation.BEFORE_COMPLETION, interceptors, instance.interceptors);
                interceptorStack.invoke();

                instanceManager.checkInInstance(callContext);
            } catch (InvalidateReferenceException e) {
            } catch (Exception e) {

                String message = "An unexpected system exception occured while invoking the beforeCompletion method on the SessionSynchronization object: " + e.getClass().getName() + " " + e.getMessage();

                /* [1] Log the exception or error */
                logger.error(message, e);

                /* [2] If the instance is in a transaction, mark the transaction for rollback. */
                txPolicy.setRollbackOnly();

                /* [3] Discard the instance */
                instanceManager.freeInstance(callContext);

                /* [4] throw the java.rmi.RemoteException to the client */
                throw new RuntimeException(message);
            } finally {
                ThreadContext.exit(oldCallContext);
            }
        }
    }

    public void afterCompletion(Status status) {
        for (Registration registration : registry.values()) {

            ThreadContext callContext = new ThreadContext(registration.deploymentInfo, registration.primaryKey);
            ThreadContext oldCallContext = ThreadContext.enter(callContext);

            StatefulInstanceManager instanceManager = null;
            try {
                CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
                StatefulContainer container = (StatefulContainer) deploymentInfo.getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operation.AFTER_COMPLETION);
                callContext.setCurrentAllowedStates(StatefulContext.getStates());

                Instance instance = (Instance) instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);

                if (registration.sessionSynchronization) {
                    Method afterCompletion = SessionSynchronization.class.getMethod("afterCompletion", boolean.class);

                    List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(afterCompletion);
                    InterceptorStack interceptorStack = new InterceptorStack(instance.bean, afterCompletion, Operation.AFTER_COMPLETION, interceptors, instance.interceptors);
                    interceptorStack.invoke(status == Status.COMMITTED);
                }

                instanceManager.poolInstance(callContext, instance);
            } catch (InvalidateReferenceException inv) {

            } catch (Exception e) {

                String message = "An unexpected system exception occured while invoking the afterCompletion method on the SessionSynchronization object: " + e.getClass().getName() + " " + e.getMessage();

                /* [1] Log the exception or error */
                logger.error(message, e);

                /* [2] If the instance is in a transaction, mark the transaction for rollback. */
                txPolicy.setRollbackOnly();

                /* [3] Discard the instance */
                instanceManager.freeInstance(callContext);

                /* [4] throw the java.rmi.RemoteException to the client */

                throw new RuntimeException(message);
            } finally {
                ThreadContext.exit(oldCallContext);
            }
        }
    }

    private static class Registration {
        private final CoreDeploymentInfo deploymentInfo;
        private final Object primaryKey;
        private boolean sessionSynchronization;

        private Registration(CoreDeploymentInfo deploymentInfo, Object primaryKey) {
            this.deploymentInfo = deploymentInfo;
            this.primaryKey = primaryKey;
        }
    }
}
