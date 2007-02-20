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

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.apache.openejb.core.BaseSessionContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;


/**
 * @version $Rev$ $Date$
 */
public class StatefulContext extends BaseSessionContext {

    protected final static State[] states = new State[Operation.values().length];

    public StatefulContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    public StatefulContext(TransactionManager transactionManager, SecurityService securityService, UserTransaction userTransaction) {
        super(transactionManager, securityService, userTransaction);
    }

    protected State getState() {
        Operation operation = ThreadContext.getCurrentOperation();
        State state = states[operation.ordinal()];

        if (state == null) throw new IllegalArgumentException("Invalid operation " + operation + " for this context");

        return state;
    }

    static {
        states[Operation.INJECTION.ordinal()] = new InjectionStatelessState();
        states[Operation.LIFECYCLE.ordinal()] = new LifecycleStatelessState();
        states[Operation.BUSINESS.ordinal()] = new BusinessStatelessState();
        states[Operation.AFTER_BEGIN.ordinal()] = new BeforeCommitState();
        states[Operation.TIMEOUT.ordinal()] = new TimeoutStatelessState();
    }

    /**
     * afterBegin
     * beforeCompletion State
     */
    public static class BeforeCommitState extends StatelessState {

        public Class getInvokedBusinessInterface() {
            throw new IllegalStateException();
        }

        public MessageContext getMessageContext() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }
    }
}
