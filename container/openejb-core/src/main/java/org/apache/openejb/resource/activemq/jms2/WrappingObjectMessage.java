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
import jakarta.jms.ObjectMessage;
import java.io.Serializable;

public class WrappingObjectMessage extends DelegateMessage implements ObjectMessage {
    private final ObjectMessage message;

    public WrappingObjectMessage(final ObjectMessage message) {
        super(message);
        this.message = message;
    }

    @Override
    public boolean isBodyAssignableTo(final Class c) throws JMSException {
        return c.isInstance(message.getObject());
    }

    @Override
    public <T> T getBody(final Class<T> c) throws JMSException {
        return c.cast(message.getObject());
    }

    @Override
    public void setObject(final Serializable object) throws JMSException {
        message.setObject(object);
    }

    @Override
    public Serializable getObject() throws JMSException {
        return message.getObject();
    }
}
