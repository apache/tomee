/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.activemq.jms2;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.Enumeration;

// used to wrap a JMS 1.0 message and provide a JMS 2.0 impl
public class DelegateMessage implements Message {
    private final Message message;
    private long deliveryTime;

    public DelegateMessage(final Message message) {
        this.message = message;
    }

    // JMS 2.0

    @Override
    public long getJMSDeliveryTime() throws JMSException {
        return deliveryTime;
    }

    @Override
    public void setJMSDeliveryTime(final long deliveryTime) throws JMSException {
        this.deliveryTime = deliveryTime;
    }

    @Override
    public <T> T getBody(final Class<T> c) throws JMSException {
        return message.getBody(c);
    }

    @Override
    public boolean isBodyAssignableTo(final Class c) throws JMSException {
        return message.isBodyAssignableTo(c);
    }

    // delegation to JMS 1.0

    @Override
    public String getJMSMessageID() throws JMSException {
        return message.getJMSMessageID();
    }

    @Override
    public void setJMSMessageID(final String id) throws JMSException {
        message.setJMSMessageID(id);
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return message.getJMSTimestamp();
    }

    @Override
    public void setJMSTimestamp(final long timestamp) throws JMSException {
        message.setJMSTimestamp(timestamp);
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        return message.getJMSCorrelationIDAsBytes();
    }

    @Override
    public void setJMSCorrelationIDAsBytes(final byte[] correlationID) throws JMSException {
        message.setJMSCorrelationIDAsBytes(correlationID);
    }

    @Override
    public void setJMSCorrelationID(final String correlationID) throws JMSException {
        message.setJMSCorrelationID(correlationID);
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        return message.getJMSCorrelationID();
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return message.getJMSReplyTo();
    }

    @Override
    public void setJMSReplyTo(final Destination replyTo) throws JMSException {
        message.setJMSReplyTo(replyTo);
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return message.getJMSDestination();
    }

    @Override
    public void setJMSDestination(final Destination destination) throws JMSException {
        message.setJMSDestination(destination);
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return message.getJMSDeliveryMode();
    }

    @Override
    public void setJMSDeliveryMode(final int deliveryMode) throws JMSException {
        message.setJMSDeliveryMode(deliveryMode);
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return message.getJMSRedelivered();
    }

    @Override
    public void setJMSRedelivered(final boolean redelivered) throws JMSException {
        message.setJMSRedelivered(redelivered);
    }

    @Override
    public String getJMSType() throws JMSException {
        return message.getJMSType();
    }

    @Override
    public void setJMSType(final String type) throws JMSException {
        message.setJMSType(type);
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return message.getJMSExpiration();
    }

    @Override
    public void setJMSExpiration(final long expiration) throws JMSException {
        message.setJMSExpiration(expiration);
    }

    @Override
    public int getJMSPriority() throws JMSException {
        return message.getJMSPriority();
    }

    @Override
    public void setJMSPriority(final int priority) throws JMSException {
        message.setJMSPriority(priority);
    }

    @Override
    public void clearProperties() throws JMSException {
        message.clearProperties();
    }

    @Override
    public boolean propertyExists(final String name) throws JMSException {
        return message.propertyExists(name);
    }

    @Override
    public boolean getBooleanProperty(final String name) throws JMSException {
        return message.getBooleanProperty(name);
    }

    @Override
    public byte getByteProperty(final String name) throws JMSException {
        return message.getByteProperty(name);
    }

    @Override
    public short getShortProperty(final String name) throws JMSException {
        return message.getShortProperty(name);
    }

    @Override
    public int getIntProperty(final String name) throws JMSException {
        return message.getIntProperty(name);
    }

    @Override
    public long getLongProperty(final String name) throws JMSException {
        return message.getLongProperty(name);
    }

    @Override
    public float getFloatProperty(final String name) throws JMSException {
        return message.getFloatProperty(name);
    }

    @Override
    public double getDoubleProperty(final String name) throws JMSException {
        return message.getDoubleProperty(name);
    }

    @Override
    public String getStringProperty(final String name) throws JMSException {
        return message.getStringProperty(name);
    }

    @Override
    public Object getObjectProperty(final String name) throws JMSException {
        return message.getObjectProperty(name);
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        return message.getPropertyNames();
    }

    @Override
    public void setBooleanProperty(final String name, final boolean value) throws JMSException {
        message.setBooleanProperty(name, value);
    }

    @Override
    public void setByteProperty(final String name, final byte value) throws JMSException {
        message.setByteProperty(name, value);
    }

    @Override
    public void setShortProperty(final String name, final short value) throws JMSException {
        message.setShortProperty(name, value);
    }

    @Override
    public void setIntProperty(final String name, final int value) throws JMSException {
        message.setIntProperty(name, value);
    }

    @Override
    public void setLongProperty(final String name, final long value) throws JMSException {
        message.setLongProperty(name, value);
    }

    @Override
    public void setFloatProperty(final String name, final float value) throws JMSException {
        message.setFloatProperty(name, value);
    }

    @Override
    public void setDoubleProperty(final String name, final double value) throws JMSException {
        message.setDoubleProperty(name, value);
    }

    @Override
    public void setStringProperty(final String name, final String value) throws JMSException {
        message.setStringProperty(name, value);
    }

    @Override
    public void setObjectProperty(final String name, final Object value) throws JMSException {
        message.setObjectProperty(name, value);
    }

    @Override
    public void acknowledge() throws JMSException {
        message.acknowledge();
    }

    @Override
    public void clearBody() throws JMSException {
        message.clearBody();
    }

    public Message unwrap() {
        return message;
    }
}
