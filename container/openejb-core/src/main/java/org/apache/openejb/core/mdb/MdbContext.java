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

import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.MessageDrivenContext;
import java.io.Flushable;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class MdbContext extends BaseContext implements MessageDrivenContext, Flushable {
    private Flushable flushable = null;

    public MdbContext(final SecurityService securityService) {
        super(securityService);
    }

    public MdbContext(final SecurityService securityService, final Flushable flushable) {
        super(securityService);
        this.flushable = flushable;
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
    public void check(final ThreadContext context, final Call call) {
        final Operation operation = context.getCurrentOperation();

        switch (call) {
            case getUserTransaction:
            case getTimerService:
            case getContextData:
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

    @Override
    public void flush() throws IOException {
        flushable.flush();
    }
}
