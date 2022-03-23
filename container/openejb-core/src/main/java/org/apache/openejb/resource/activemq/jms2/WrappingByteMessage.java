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

import org.apache.activemq.command.ActiveMQBytesMessage;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

public class WrappingByteMessage extends DelegateMessage implements BytesMessage {
    private final BytesMessage message;

    public WrappingByteMessage(final BytesMessage message) {
        super(message);
        this.message = message;
    }

    @Override
    public boolean isBodyAssignableTo(final Class c) throws JMSException {
        return byte[].class == c;
    }

    @Override
    public <T> T getBody(final Class<T> c) throws JMSException {
        final int len = (int) getBodyLength();
        if (len == 0) {
            return null;
        }
        final byte[] dst = new byte[len];
        if (ActiveMQBytesMessage.class.isInstance(message)) {
            System.arraycopy(ActiveMQBytesMessage.class.cast(message).getContent().getData(), 0, dst, 0, len);
        }
        return c.cast(dst);
    }

    @Override
    public long getBodyLength() throws JMSException {
        return message.getBodyLength();
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
    public int readUnsignedByte() throws JMSException {
        return message.readUnsignedByte();
    }

    @Override
    public short readShort() throws JMSException {
        return message.readShort();
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        return message.readUnsignedShort();
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
    public String readUTF() throws JMSException {
        return message.readUTF();
    }

    @Override
    public int readBytes(final byte[] value) throws JMSException {
        return message.readBytes(value);
    }

    @Override
    public int readBytes(final byte[] value, final int length) throws JMSException {
        return message.readBytes(value, length);
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
    public void writeFloat(final float value) throws JMSException {
        message.writeFloat(value);
    }

    @Override
    public void writeDouble(final double value) throws JMSException {
        message.writeDouble(value);
    }

    @Override
    public void writeUTF(final String value) throws JMSException {
        message.writeUTF(value);
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
