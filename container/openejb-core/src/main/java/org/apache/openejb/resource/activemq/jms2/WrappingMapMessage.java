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

import org.apache.activemq.command.ActiveMQMapMessage;

import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.MessageFormatException;
import java.util.Enumeration;
import java.util.Map;

public class WrappingMapMessage extends DelegateMessage implements MapMessage {
    private final MapMessage message;

    public WrappingMapMessage(final MapMessage message) {
        super(message);
        this.message = message;
    }

    @Override
    public boolean isBodyAssignableTo(final Class c) throws JMSException {
        return c == Map.class || Object.class == c;
    }

    @Override
    public <T> T getBody(final Class<T> c) throws JMSException {
        if (isBodyAssignableTo(c)) {
            return c.cast(ActiveMQMapMessage.class.isInstance(message) ?
                ActiveMQMapMessage.class.cast(message).getContentMap() : message /* unlikely */);
        }
        throw new MessageFormatException("Can't get the body with type " + c);
    }

    @Override
    public boolean getBoolean(final String name) throws JMSException {
        return message.getBoolean(name);
    }

    @Override
    public byte getByte(final String name) throws JMSException {
        return message.getByte(name);
    }

    @Override
    public short getShort(final String name) throws JMSException {
        return message.getShort(name);
    }

    @Override
    public char getChar(final String name) throws JMSException {
        return message.getChar(name);
    }

    @Override
    public int getInt(final String name) throws JMSException {
        return message.getInt(name);
    }

    @Override
    public long getLong(final String name) throws JMSException {
        return message.getLong(name);
    }

    @Override
    public float getFloat(final String name) throws JMSException {
        return message.getFloat(name);
    }

    @Override
    public double getDouble(final String name) throws JMSException {
        return message.getDouble(name);
    }

    @Override
    public String getString(final String name) throws JMSException {
        return message.getString(name);
    }

    @Override
    public byte[] getBytes(final String name) throws JMSException {
        return message.getBytes(name);
    }

    @Override
    public Object getObject(final String name) throws JMSException {
        return message.getObject(name);
    }

    @Override
    public Enumeration getMapNames() throws JMSException {
        return message.getMapNames();
    }

    @Override
    public void setBoolean(final String name, final boolean value) throws JMSException {
        message.setBoolean(name, value);
    }

    @Override
    public void setByte(final String name, final byte value) throws JMSException {
        message.setByte(name, value);
    }

    @Override
    public void setShort(final String name, final short value) throws JMSException {
        message.setShort(name, value);
    }

    @Override
    public void setChar(final String name, final char value) throws JMSException {
        message.setChar(name, value);
    }

    @Override
    public void setInt(final String name, final int value) throws JMSException {
        message.setInt(name, value);
    }

    @Override
    public void setLong(final String name, final long value) throws JMSException {
        message.setLong(name, value);
    }

    @Override
    public void setFloat(final String name, final float value) throws JMSException {
        message.setFloat(name, value);
    }

    @Override
    public void setDouble(final String name, final double value) throws JMSException {
        message.setDouble(name, value);
    }

    @Override
    public void setString(final String name, final String value) throws JMSException {
        message.setString(name, value);
    }

    @Override
    public void setBytes(final String name, final byte[] value) throws JMSException {
        message.setBytes(name, value);
    }

    @Override
    public void setBytes(final String name, final byte[] value, final int offset, final int length) throws JMSException {
        message.setBytes(name, value, offset, length);
    }

    @Override
    public void setObject(final String name, final Object value) throws JMSException {
        message.setObject(name, value);
    }

    @Override
    public boolean itemExists(final String name) throws JMSException {
        return message.itemExists(name);
    }
}
