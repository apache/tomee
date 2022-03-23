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

import org.apache.xbean.propertyeditor.PropertyEditorException;
import org.apache.xbean.propertyeditor.PropertyEditorRegistry;

import jakarta.jms.BytesMessage;
import jakarta.jms.CompletionListener;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageFormatRuntimeException;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.openejb.resource.activemq.jms2.JMS2.toRuntimeException;
import static org.apache.openejb.resource.activemq.jms2.JMS2.wrap;

@SuppressWarnings("deprecation")
class JMSProducerImpl implements JMSProducer {
    private final JMSContextImpl context;
    private final MessageProducer producer;

    private final Map<String, Object> properties = new HashMap<>();

    private volatile CompletionListener completionListener;

    private Destination jmsHeaderReplyTo;
    private String jmsHeaderCorrelationID;
    private byte[] jmsHeaderCorrelationIDAsBytes;
    private String jmsHeaderType;
    private PropertyEditorRegistry propertyEditorRegistry = new PropertyEditorRegistry();

    JMSProducerImpl(final JMSContextImpl jmsContext, final MessageProducer innerProducer) {
        this.context = jmsContext;
        this.producer = innerProducer;
        this.propertyEditorRegistry.registerDefaults();
    }

    private <T> T getProperty(final String key, final Class<T> type) {
        final Object val = properties.get(key);
        if (val == null || type.isInstance(val)) {
            return type.cast(val);
        }
        try {
            return type.cast(propertyEditorRegistry.getValue(type, val.toString()));
        } catch (final PropertyEditorException pee) {
            throw new MessageFormatRuntimeException(pee.getMessage());
        }
    }

    @Override
    public JMSProducer send(final Destination destination, final Message message) {
        if (message == null) {
            throw new MessageFormatRuntimeException("null message");
        }

        try {
            if (jmsHeaderCorrelationID != null) {
                message.setJMSCorrelationID(jmsHeaderCorrelationID);
            }
            if (jmsHeaderCorrelationIDAsBytes != null && jmsHeaderCorrelationIDAsBytes.length > 0) {
                message.setJMSCorrelationIDAsBytes(jmsHeaderCorrelationIDAsBytes);
            }
            if (jmsHeaderReplyTo != null) {
                message.setJMSReplyTo(jmsHeaderReplyTo);
            }
            if (jmsHeaderType != null) {
                message.setJMSType(jmsHeaderType);
            }

            setProperties(message);
            if (completionListener != null) {
                producer.send(destination, message, completionListener);
            } else {
                producer.send(destination, message);
            }
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
        return this;
    }

    private void setProperties(final Message message) throws JMSException {
        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            message.setObjectProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public JMSProducer send(final Destination destination, final String body) {
        send(destination, wrap(context.createTextMessage(body)));
        return this;
    }

    @Override
    public JMSProducer send(final Destination destination, final Map<String, Object> body) {
        final MapMessage message = wrap(context.createMapMessage());
        if (body != null) {
            try {
                for (final Map.Entry<String, Object> entry : body.entrySet()) {
                    final String name = entry.getKey();
                    final Object v = entry.getValue();
                    if (v instanceof String) {
                        message.setString(name, (String) v);
                    } else if (v instanceof Long) {
                        message.setLong(name, (Long) v);
                    } else if (v instanceof Double) {
                        message.setDouble(name, (Double) v);
                    } else if (v instanceof Integer) {
                        message.setInt(name, (Integer) v);
                    } else if (v instanceof Character) {
                        message.setChar(name, (Character) v);
                    } else if (v instanceof Short) {
                        message.setShort(name, (Short) v);
                    } else if (v instanceof Boolean) {
                        message.setBoolean(name, (Boolean) v);
                    } else if (v instanceof Float) {
                        message.setFloat(name, (Float) v);
                    } else if (v instanceof Byte) {
                        message.setByte(name, (Byte) v);
                    } else if (v instanceof byte[]) {
                        byte[] array = (byte[]) v;
                        message.setBytes(name, array, 0, array.length);
                    } else {
                        message.setObject(name, v);
                    }
                }
            } catch (final JMSException e) {
                throw new MessageFormatRuntimeException(e.getMessage());
            }
        }
        send(destination, message);
        return this;
    }

    @Override
    public JMSProducer send(final Destination destination, final byte[] body) {
        final BytesMessage message = wrap(context.createBytesMessage());
        if (body != null) {
            try {
                message.writeBytes(body);
            } catch (final JMSException e) {
                throw new MessageFormatRuntimeException(e.getMessage());
            }
        }
        send(destination, message);
        return this;
    }

    @Override
    public JMSProducer send(final Destination destination, final Serializable body) {
        final ObjectMessage message = wrap(context.createObjectMessage(body));
        send(destination, message);
        return this;
    }

    @Override
    public JMSProducer setDisableMessageID(final boolean value) {
        try {
            producer.setDisableMessageID(value);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
        return this;
    }

    @Override
    public boolean getDisableMessageID() {
        try {
            return producer.getDisableMessageID();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSProducer setDisableMessageTimestamp(final boolean value) {
        try {
            producer.setDisableMessageTimestamp(value);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
        return this;
    }

    @Override
    public boolean getDisableMessageTimestamp() {
        try {
            return producer.getDisableMessageTimestamp();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSProducer setDeliveryMode(final int deliveryMode) {
        try {
            producer.setDeliveryMode(deliveryMode);
        } catch (final JMSException e) {
            final JMSRuntimeException e2 = new JMSRuntimeException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
        return this;
    }

    @Override
    public int getDeliveryMode() {
        try {
            return producer.getDeliveryMode();
        } catch (final JMSException e) {
            final JMSRuntimeException e2 = new JMSRuntimeException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
    }

    @Override
    public JMSProducer setPriority(final int priority) {
        try {
            producer.setPriority(priority);
        } catch (final JMSException e) {
            final JMSRuntimeException e2 = new JMSRuntimeException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
        return this;
    }

    @Override
    public int getPriority() {
        try {
            return producer.getPriority();
        } catch (final JMSException e) {
            final JMSRuntimeException e2 = new JMSRuntimeException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
    }

    @Override
    public JMSProducer setTimeToLive(final long timeToLive) {
        try {
            producer.setTimeToLive(timeToLive);
            return this;
        } catch (final JMSException e) {
            final JMSRuntimeException e2 = new JMSRuntimeException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
    }

    @Override
    public long getTimeToLive() {
        try {
            return producer.getTimeToLive();
        } catch (final JMSException e) {
            final JMSRuntimeException e2 = new JMSRuntimeException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
    }

    @Override
    public JMSProducer setDeliveryDelay(final long deliveryDelay) {
        try {
            producer.setDeliveryDelay(deliveryDelay);
            return this;
        } catch (final JMSException e) {
            JMSRuntimeException e2 = new JMSRuntimeException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }
    }

    @Override
    public long getDeliveryDelay() {
        try {
            return producer.getDeliveryDelay();
        } catch (final Exception ignored) {
            // no-op
        }
        return 0;
    }

    @Override
    public JMSProducer setAsync(final CompletionListener completionListener) {
        this.completionListener = completionListener;
        return this;
    }

    @Override
    public CompletionListener getAsync() {
        return completionListener;
    }

    @Override
    public JMSProducer setProperty(final String name, final boolean value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(final String name, final byte value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(final String name, final short value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(final String name, final int value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(final String name, final long value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(final String name, final float value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(final String name, final double value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(final String name, final String value) {
        validName(name);
        properties.put(name, value);
        return this;
    }

    @Override
    public JMSProducer clearProperties() {
        properties.clear();
        return this;
    }

    @Override
    public boolean propertyExists(final String name) {
        return properties.containsKey(name);
    }

    @Override
    public JMSProducer setProperty(final String name, final Object value) {
        validName(name);
        if (value != null && !Boolean.class.isInstance(value) && !Byte.class.isInstance(value) && !Character.class.isInstance(value)
            && !Short.class.isInstance(value) && !Integer.class.isInstance(value) && !Long.class.isInstance(value)
            && !Float.class.isInstance(value) && !Double.class.isInstance(value) && !String.class.isInstance(value)
            && !byte[].class.isInstance(value)) {
            throw new MessageFormatRuntimeException("Unsupported type: " + value);
        }
        properties.put(name, value);
        return this;
    }

    @Override
    public boolean getBooleanProperty(final String name) {
        return getProperty(name, Boolean.class);
    }

    @Override
    public byte getByteProperty(final String name) {
        return getProperty(name, Byte.class);
    }

    @Override
    public short getShortProperty(final String name) {
        return getProperty(name, Short.class);
    }

    @Override
    public int getIntProperty(final String name) {
        return getProperty(name, Integer.class);
    }

    @Override
    public long getLongProperty(final String name) {
        return getProperty(name, Long.class);
    }

    @Override
    public float getFloatProperty(final String name) {
        return getProperty(name, Float.class);
    }

    @Override
    public double getDoubleProperty(final String name) {
        return getProperty(name, Double.class);
    }

    @Override
    public String getStringProperty(final String name) {
        return getProperty(name, String.class);
    }

    @Override
    public Object getObjectProperty(final String name) {
        return getProperty(name, Object.class);
    }

    @Override
    public Set<String> getPropertyNames() {
        return new HashSet<>(properties.keySet());
    }

    @Override
    public JMSProducer setJMSCorrelationIDAsBytes(final byte[] correlationID) {
        if (correlationID == null || correlationID.length == 0) {
            throw new JMSRuntimeException("Please specify a non-zero length byte[]");
        }
        jmsHeaderCorrelationIDAsBytes = Arrays.copyOf(correlationID, correlationID.length);
        return this;
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        return Arrays.copyOf(jmsHeaderCorrelationIDAsBytes, jmsHeaderCorrelationIDAsBytes.length);
    }

    @Override
    public JMSProducer setJMSCorrelationID(final String correlationID) {
        jmsHeaderCorrelationID = correlationID;
        return this;
    }

    @Override
    public String getJMSCorrelationID() {
        return jmsHeaderCorrelationID;
    }

    @Override
    public JMSProducer setJMSType(final String type) {
        jmsHeaderType = type;
        return this;
    }

    @Override
    public String getJMSType() {
        return jmsHeaderType;
    }

    @Override
    public JMSProducer setJMSReplyTo(final Destination replyTo) {
        jmsHeaderReplyTo = replyTo;
        return this;
    }

    @Override
    public Destination getJMSReplyTo() {
        return jmsHeaderReplyTo;
    }

    private void validName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name can't be blank");
        }
    }
}
