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
package org.apache.openejb;

import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;

import org.apache.openejb.transaction.EjbTransactionContext;

/**
 * Specialization of Invocation to define attributes specific to the
 * invocation of an EJB. This provides a type-safe mechanism for Interceptors
 * to access EJB specific information; it is the responsibility of the
 * original to ensure the Invocation implementation supports this interface
 * if it is going to be processed by an EJBContainer.
 *
 * @version $Revision$ $Date$
 */
public interface EjbInvocation extends Invocation {

    /**
     * The index of the virtual EJB operation
     * @return the index of the EJB operation being performed
     */
    int getMethodIndex();

    /**
     * The type of invocation, indicating which interface was invoked or
     * which 'special' callback was should be invoked (e.g. ejbTimeout).
     * @return the type of invocation
     */
    EJBInterfaceType getType();

    /**
     * Any arguments to the invocation (e.g. Method parameters).
     * @return the arguments to the invocation; null indicates no arguments (equivalent to Object[0])
     */
    Object[] getArguments();

    /**
     * The identity of the instance being invoked; for example, the primary
     * key of an Entity EJB.
     * @return the identity of the instance to invoke; may be null for 'class' level operations
     */
    Object getId();

    /**
     * The context representing the actual instance to use for processing this
     * request. Is transient, not valid on the client side, and will not be
     * valid on the server side until a suitable instance has been located
     * by an Interceptor
     * @return the context representing the instance to invoke
     */
    EJBInstanceContext getEJBInstanceContext();

    /**
     * Set the instance context to use
     * @param instanceContext the instance context to use
     */
    void setEJBInstanceContext(EJBInstanceContext instanceContext);

    /**
     * Gets the transaction context to use.  Eventhough the tx context is available from a
     * thread local we carry it in the invocation context to avoid the extra tx cost.
     * @return the transaction context to use
     */
    EjbTransactionContext getEjbTransactionData();

    /**
     * Setx the transaction context to use.  Eventhough the tx context is available from a
     * thread local we carry it in the invocation context to avoid the extra tx cost.
     * @param ejbTransactionContext the transaction context to use
     */
    void setEjbTransactionData(EjbTransactionContext ejbTransactionContext);

    InvocationResult createResult(Object object);

    InvocationResult createExceptionResult(Exception exception);

    ExtendedEjbDeployment getEjbDeployment();

    void setEjbDeployment(ExtendedEjbDeployment ejbDeployment);
}
