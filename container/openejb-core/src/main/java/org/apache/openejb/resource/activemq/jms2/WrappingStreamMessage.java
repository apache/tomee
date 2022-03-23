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

import jakarta.jms.JMSException;
import jakarta.jms.MessageFormatException;
import jakarta.jms.StreamMessage;

public class WrappingStreamMessage extends DelegateMessage implements StreamMessage {
    private final StreamMessage message;

    public WrappingStreamMessage(final StreamMessage message) {
        super(message);
        this.message = message;
    }

    @Override
    public <T> T getBody(final Class<T> c) throws JMSException {
        throw new MessageFormatException("Can't getBody on a stream message");
    }

    @Override
    public boolean isBodyAssignableTo(final Class c) throws JMSException {
        return false;
    }

    @Override
    public boolean readBoolean() throws JMSException {
        return message.readBoolean();
    }

    @Override
    public byte readByte() throws JMSException {
        return message.readByte();
    }

    @Override
    public short readShort() throws JMSException {
        return message.readShort();
    }

    @Override
    public char readChar() throws JMSException {
        return message.readChar();
    }

    @Override
    public int readInt() throws JMSException {
        return message.readInt();
    }

    @Override
    public long readLong() throws JMSException {
        return message.readLong();
    }

    @Override
    public float readFloat() throws JMSException {
        return message.readFloat();
    }

    @Override
    public double readDouble() throws JMSException {
        return message.readDouble();
    }

    @Override
    public String readString() throws JMSException {
        return message.readString();
    }

    @Override
    public int readBytes(byte[] value) throws JMSException {
        return message.readBytes(value);
    }

    @Override
    public Object readObject() throws JMSException {
        return message.readObject();
    }

    @Override
    public void writeBoolean(final boolean value) throws JMSException {
        message.writeBoolean(value);
    }

    @Override
    public void writeByte(final byte value) throws JMSException {
        message.writeByte(value);
    }

    @Override
    public void writeShort(final short value) throws JMSException {
        message.writeShort(value);
    }

    @Override
    public void writeChar(final char value) throws JMSException {
        message.writeChar(value);
    }

    @Override
    public void writeInt(final int value) throws JMSException {
        message.writeInt(value);
    }

    @Override
    public void writeLong(final long value) throws JMSException {
        message.writeLong(value);
    }

    @Override
    public void writeFloat(float value) throws JMSException {
        message.writeFloat(value);
    }

    @Override
    public void writeDouble(final double value) throws JMSException {
        message.writeDouble(value);
    }

    @Override
    public void writeString(final String value) throws JMSException {
        message.writeString(value);
    }

    @Override
    public void writeBytes(final byte[] value) throws JMSException {
        message.writeBytes(value);
    }

    @Override
    public void writeBytes(final byte[] value, final int offset, final int length) throws JMSException {
        message.writeBytes(value, offset, length);
    }

    @Override
    public void writeObject(final Object value) throws JMSException {
        message.writeObject(value);
    }

    @Override
    public void reset() throws JMSException {
        message.reset();
    }
}
