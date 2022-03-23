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

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQXASslConnectionFactory;
import org.apache.activemq.management.JMSStatsImpl;
import org.apache.activemq.transport.Transport;

import jakarta.jms.JMSContext;
import jakarta.resource.spi.TransactionSupport;

public class TomEEConnectionFactory extends ActiveMQXASslConnectionFactory {
    private final TransactionSupport.TransactionSupportLevel transactionSupportLevel;

    public TomEEConnectionFactory(TransactionSupport.TransactionSupportLevel transactionSupportLevel) {
        this.transactionSupportLevel = transactionSupportLevel;
    }

    @Override
    protected ActiveMQConnection createActiveMQConnection(final Transport transport, final JMSStatsImpl stats) throws Exception {
        if (TransactionSupport.TransactionSupportLevel.NoTransaction.equals(transactionSupportLevel)) {
            return new TomEEConnection(transport, getClientIdGenerator(), getConnectionIdGenerator(), stats);
        } else {
            return new TomEEXAConnection(transport, getClientIdGenerator(), getConnectionIdGenerator(), stats);
        }
    }

    @Override
    public JMSContext createContext() {
        boolean inTx = JMS2.inTx();
        int mode;
        if (inTx) {
            mode = -1;
        } else {
            mode = JMSContext.AUTO_ACKNOWLEDGE;
        }
        return new JMSContextImpl(this, mode, null, null, inTx);
    }

    @Override
    public JMSContext createContext(final int sessionMode) {
        boolean inTx = JMS2.inTx();
        int mode;
        if (inTx) {
            mode = -1;
        } else {
            mode = sessionMode;
        }
        return new JMSContextImpl(this, mode, null, null, inTx);
    }

    @Override
    public JMSContext createContext(final String userName, final String password) {
        boolean inTx = JMS2.inTx();
        int mode;
        if (inTx) {
            mode = -1;
        } else {
            mode = JMSContext.AUTO_ACKNOWLEDGE;
        }
        return new JMSContextImpl(this, mode, userName, password, inTx);
    }

    @Override
    public JMSContext createContext(final String userName, final String password, final int sessionMode) {
        boolean inTx = JMS2.inTx();
        int mode;
        if (inTx) {
            mode = -1;
        } else {
            mode = sessionMode;
        }
        return new JMSContextImpl(this, mode, userName, password, inTx);
    }
}
