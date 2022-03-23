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

import jakarta.jms.BytesMessage;
import jakarta.jms.IllegalStateRuntimeException;
import jakarta.jms.InvalidClientIDException;
import jakarta.jms.InvalidClientIDRuntimeException;
import jakarta.jms.InvalidDestinationException;
import jakarta.jms.InvalidDestinationRuntimeException;
import jakarta.jms.InvalidSelectorException;
import jakarta.jms.InvalidSelectorRuntimeException;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.JMSSecurityException;
import jakarta.jms.JMSSecurityRuntimeException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageFormatException;
import jakarta.jms.MessageFormatRuntimeException;
import jakarta.jms.MessageNotWriteableException;
import jakarta.jms.MessageNotWriteableRuntimeException;
import jakarta.jms.ObjectMessage;
import jakarta.jms.ResourceAllocationException;
import jakarta.jms.ResourceAllocationRuntimeException;
import jakarta.jms.StreamMessage;
import jakarta.jms.TextMessage;
import jakarta.jms.TransactionInProgressException;
import jakarta.jms.TransactionInProgressRuntimeException;
import jakarta.jms.TransactionRolledBackException;
import jakarta.jms.TransactionRolledBackRuntimeException;
import jakarta.transaction.SystemException;

import org.apache.openejb.OpenEJB;

public final class JMS2 {
    private JMS2() {
        // no-op
    }

    public static JMSRuntimeException toRuntimeException(final JMSException e) {
        if (e instanceof jakarta.jms.IllegalStateException) {
            return new IllegalStateRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof InvalidClientIDException) {
            return new InvalidClientIDRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof InvalidDestinationException) {
            return new InvalidDestinationRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof InvalidSelectorException) {
            return new InvalidSelectorRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof JMSSecurityException) {
            return new JMSSecurityRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof MessageFormatException) {
            return new MessageFormatRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof MessageNotWriteableException) {
            return new MessageNotWriteableRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof ResourceAllocationException) {
            return new ResourceAllocationRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof TransactionInProgressException) {
            return new TransactionInProgressRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        if (e instanceof TransactionRolledBackException) {
            return new TransactionRolledBackRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
        return new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T wrap(final T message10) {
        if (message10 == null) {
            return null;
        }

        // already wrapped // happens with producer -> context link
        // but since we an switch the context better to ensure we wrap anyway
        if (message10.getClass().getName().startsWith(JMS2.class.getPackage().getName())) {
            return message10;
        }

        // jms -> wrappers
        if (TextMessage.class.isInstance(message10)) {
            return (T) new WrappingTextMessage(TextMessage.class.cast(message10));
        }
        if (ObjectMessage.class.isInstance(message10)) {
            return (T) new WrappingObjectMessage(ObjectMessage.class.cast(message10));
        }
        if (MapMessage.class.isInstance(message10)) {
            return (T) new WrappingMapMessage(MapMessage.class.cast(message10));
        }
        if (BytesMessage.class.isInstance(message10)) {
            return (T) new WrappingByteMessage(BytesMessage.class.cast(message10));
        }
        if (StreamMessage.class.isInstance(message10)) {
            return (T) new WrappingStreamMessage(StreamMessage.class.cast(message10));
        }
        return (T) new DelegateMessage(message10);
    }


    public static boolean inTx() {
        try {
            return OpenEJB.getTransactionManager().getTransaction() != null;
        } catch (SystemException | NullPointerException e) {
            return false;
        }
    }
}
