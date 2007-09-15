/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The messagelistenerType specifies information about a
 * specific message listener supported by the messaging
 * resource adapter. It contains information on the Java type
 * of the message listener interface and an activation
 * specification.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "messagelistenerType", propOrder = {
        "messageListenerType",
        "activationSpec"
})
public class MessageListener {

    @XmlElement(name = "messagelistener-type", required = true)
    protected String messageListenerType;
    @XmlElement(name = "activationspec", required = true)
    protected ActivationSpec activationSpec;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getMessageListenerType() {
        return messageListenerType;
    }

    public void setMessageListenerType(String value) {
        this.messageListenerType = value;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public void setActivationSpec(ActivationSpec value) {
        this.activationSpec = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
