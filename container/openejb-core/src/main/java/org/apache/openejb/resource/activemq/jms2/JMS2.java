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

import javax.jms.BytesMessage;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.InvalidClientIDException;
import javax.jms.InvalidClientIDRuntimeException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidDestinationRuntimeException;
import javax.jms.InvalidSelectorException;
import javax.jms.InvalidSelectorRuntimeException;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.JMSSecurityException;
import javax.jms.JMSSecurityRuntimeException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageNotWriteableRuntimeException;
import javax.jms.ObjectMessage;
import javax.jms.ResourceAllocationException;
import javax.jms.ResourceAllocationRuntimeException;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.TransactionInProgressException;
import javax.jms.TransactionInProgressRuntimeException;
import javax.jms.TransactionRolledBackException;
import javax.jms.TransactionRolledBackRuntimeException;

public final class JMS2 {
    private JMS2() {
        // no-op
    }

    public static JMSRuntimeException toRuntimeException(final JMSException e) {
        if (e instanceof javax.jms.IllegalStateException) {
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
}
