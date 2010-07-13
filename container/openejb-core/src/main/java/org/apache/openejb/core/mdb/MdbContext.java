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
package org.apache.openejb.core.mdb;

import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.MessageDrivenContext;

/**
 * @version $Rev$ $Date$
 */
public class MdbContext extends BaseContext implements MessageDrivenContext {

    public MdbContext(SecurityService securityService) {
        super(securityService);
    }

    @Override
    public EJBHome getEJBHome() {
        throw new IllegalStateException();
    }

    @Override
    public EJBLocalHome getEJBLocalHome() {
        throw new IllegalStateException();
    }

    @Override
    public void check(Call call) {
        final Operation operation = ThreadContext.getThreadContext().getCurrentOperation();

        switch (call) {
            case getUserTransaction:
            case getTimerService:
                switch (operation) {
                    case INJECTION:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case getCallerPrincipal:
            case isCallerInRole:
            case timerMethod:
            case setRollbackOnly:
            case getRollbackOnly:
                switch (operation) {
                    case INJECTION:
                    case CREATE:
                    case POST_CONSTRUCT:
                    case PRE_DESTROY:
                        throw illegal(call, operation);
                    default:
                        return;
                }
        }
    }
}
