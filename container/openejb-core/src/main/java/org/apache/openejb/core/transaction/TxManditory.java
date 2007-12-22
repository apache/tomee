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
package org.apache.openejb.core.transaction;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;

import javax.transaction.TransactionRequiredException;

/**
 * 17.6.2.5 Mandatory
 *
 * The Container must invoke an enterprise Bean method whose transaction
 * attribute is set to Mandatory in a client's transaction context. The client
 * is required to call with a transaction context.
 *
 * * If the client calls with a transaction context, the container invokes the
 *   enterprise Bean's method in the client's transaction context.
 *
 * * If the client calls without a transaction context, the Container throws
 *   the javax.transaction.TransactionRequiredException exception if the
 *   client is a remote client, or the
 *   javax.ejb.TransactionRequiredLocalException if the client is a local
 *   client.
 *
 */
public class TxManditory extends TransactionPolicy {

    public TxManditory(TransactionContainer container) {
        super(Type.Mandatory, container);
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws SystemException, ApplicationException {

        try {

            context.clientTx = context.getTransactionManager().getTransaction();

            if (context.clientTx == null) {

                throw new ApplicationException(new TransactionRequiredException());
            }

            context.currentTx = context.clientTx;

        } catch (javax.transaction.SystemException se) {
            logger.debug("Exception during getTransaction()", se);
            throw new SystemException(se);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws ApplicationException, SystemException {
    }

    public void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException {
        if (rollback && context.currentTx != null) markTxRollbackOnly(context.currentTx);

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {

        /* [1] Log the system exception or error *********/
        logSystemException(sysException);

        /* [2] Mark the transaction for rollback. ********/
        markTxRollbackOnly(context.currentTx);

        /* [3] Discard instance. *************************/
        discardBeanInstance(instance, context.callContext);

        /* [4] TransactionRolledbackException to client **/
        throwTxExceptionToServer(sysException);
    }

}

