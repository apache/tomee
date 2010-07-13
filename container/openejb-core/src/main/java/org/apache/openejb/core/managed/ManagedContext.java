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
package org.apache.openejb.core.managed;

import org.apache.openejb.core.BaseSessionContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;

import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;


/**
 * @version $Rev$ $Date$
 */
public class ManagedContext extends BaseSessionContext {

    public ManagedContext(SecurityService securityService, UserTransaction userTransaction) {
        super(securityService, userTransaction);
    }

    @Override
    public void check(Call call) {
        final Operation operation = ThreadContext.getThreadContext().getCurrentOperation();
        switch (call) {
            case getCallerPrincipal:
            case isCallerInRole:
            case getUserTransaction:
            case getTimerService:
            case getEJBLocalObject:
            case getEJBObject:
            case getBusinessObject:
                switch (operation) {
                    case INJECTION:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case setRollbackOnly:
            case getRollbackOnly:
            case timerMethod:
                switch (operation) {
                    case INJECTION:
                    case CREATE:
                    case AFTER_COMPLETION:
                    case PRE_DESTROY:
                    case REMOVE:
                    case POST_CONSTRUCT:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case getInvokedBusinessInterface:
                switch (operation) {
                    case INJECTION:
                    case CREATE:
                    case AFTER_BEGIN:
                    case BEFORE_COMPLETION:
                    case AFTER_COMPLETION:
                    case TIMEOUT:
                    case PRE_DESTROY:
                    case REMOVE:
                    case POST_CONSTRUCT:
                        throw illegal(call, operation);
                    default:
                        return;
                }

            case UserTransactionMethod:
                switch (operation) {
                    case INJECTION:
                    case AFTER_COMPLETION:
                        throw illegal(call, operation);
                    default:
                        return;
                }

        }
    }

    @Override
    public MessageContext getMessageContext() throws IllegalStateException {
        throw new IllegalStateException("@ManagedBeans do not support Web Service interfaces");
    }
}
