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
package org.apache.openejb.resource.activemq.jms2;

import jakarta.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.apache.activemq.ra.ActiveMQConnectionFactory;
import org.apache.activemq.ra.ActiveMQConnectionRequestInfo;
import org.apache.activemq.ra.ActiveMQManagedConnectionFactory;

import jakarta.jms.JMSContext;
import jakarta.resource.spi.ConnectionManager;

public class TomEERAConnectionFactory extends ActiveMQConnectionFactory {
    private static final long serialVersionUID = 1L;
    private TransactionSupportLevel transactionSupportLevel = TransactionSupportLevel.XATransaction;

    public TomEERAConnectionFactory(final ActiveMQManagedConnectionFactory factory, final ConnectionManager manager,
                                    final ActiveMQConnectionRequestInfo connectionRequestInfo) {
        super(factory, manager, connectionRequestInfo);
    }

    @Override
    public JMSContext createContext() {
        // See notes here. We _do_ allow the user to override session mode at the
        // connectionFactory level, otherwise we follow the spec.
        // https://docs.oracle.com/javaee/7/api/javax/jms/ConnectionFactory.html#createContext-int-
        int mode;
        boolean xa;
        switch (transactionSupportLevel) {
            case XATransaction:
                if (JMS2.inTx()) {
                    mode = -1;
                    xa = true;
                    break;
                }
            case NoTransaction:
                mode = JMSContext.AUTO_ACKNOWLEDGE;
                xa = false;
                break;
            case LocalTransaction:
                mode = JMSContext.SESSION_TRANSACTED;
                xa = false;
                break;
            default:
                throw new IllegalStateException("transactionSupportLevel mode not supported:" + transactionSupportLevel);
        }
        return new JMSContextImpl(this, mode, null, null, xa);
    }

    @Override
    public JMSContext createContext(final int sessionMode) {
        int mode;
        boolean xa;
        switch (transactionSupportLevel) {
            case XATransaction:
                if (JMS2.inTx()) {
                    mode = -1;
                    xa = true;
                    break;
                }
            case NoTransaction:
                mode = sessionMode;
                xa = false;
                break;
            case LocalTransaction:
                mode = JMSContext.SESSION_TRANSACTED;
                xa = false;
                break;
            default:
                throw new IllegalStateException("transactionSupportLevel mode not supported:" + transactionSupportLevel);
        }
        return new JMSContextImpl(this, mode, null, null, xa);
    }

    @Override
    public JMSContext createContext(final String userName, final String password) {
        int mode;
        boolean xa;
        switch (transactionSupportLevel) {
            case XATransaction:
                if (JMS2.inTx()) {
                    mode = -1;
                    xa = true;
                    break;
                }
            case NoTransaction:
                mode = JMSContext.AUTO_ACKNOWLEDGE;
                xa = false;
                break;
            case LocalTransaction:
                mode = JMSContext.SESSION_TRANSACTED;
                xa = false;
                break;
            default:
                throw new IllegalStateException("transactionSupportLevel mode not supported:" + transactionSupportLevel);
        }
        return new JMSContextImpl(this, mode, userName, password, xa);
    }

    @Override
    public JMSContext createContext(final String userName, final String password, final int sessionMode) {
        int mode;
        boolean xa;
        switch (transactionSupportLevel) {
            case XATransaction:
                if (JMS2.inTx()) {
                    mode = -1;
                    xa = true;
                    break;
                }
            case NoTransaction:
                mode = sessionMode;
                xa = false;
                break;
            case LocalTransaction:
                mode = JMSContext.SESSION_TRANSACTED;
                xa = false;
                break;
            default:
                throw new IllegalStateException("transactionSupportLevel mode not supported:" + transactionSupportLevel);
        }
        return new JMSContextImpl(this, mode, userName, password, xa);
    }

    public TransactionSupportLevel getTransactionSupport() {
        return transactionSupportLevel;
    }

    public void setTransactionSupport(TransactionSupportLevel transactionSupportLevel) {
        if (transactionSupportLevel == null) {
            throw new IllegalArgumentException("transactionSupportLevel cannot be null");
        } else {
            this.transactionSupportLevel = transactionSupportLevel;
        }
    }
}
