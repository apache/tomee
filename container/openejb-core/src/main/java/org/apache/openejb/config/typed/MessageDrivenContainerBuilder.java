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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.typed;

import org.apache.openejb.config.typed.util.*;
import org.apache.openejb.config.sys.*;
import javax.xml.bind.annotation.*;
import org.apache.openejb.util.Duration;
import java.util.*;
import java.util.concurrent.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MessageDrivenContainer")
public class MessageDrivenContainerBuilder extends Container {

    @XmlAttribute
    private String resourceAdapter = "Default JMS Resource Adapter";
    @XmlAttribute
    private String messageListenerInterface = "javax.jms.MessageListener";
    @XmlAttribute
    private String activationSpecClass = "org.apache.activemq.ra.ActiveMQActivationSpec";
    @XmlAttribute
    private int instanceLimit = 10;

    public MessageDrivenContainerBuilder() {
        setClassName("org.apache.openejb.core.mdb.MdbContainer");
        setType("MESSAGE");
        setId("MessageDrivenContainer");

        setConstructor("id, securityService, resourceAdapter, messageListenerInterface, activationSpecClass, instanceLimit");

    }

    public MessageDrivenContainerBuilder id(String id) {
        setId(id);
        return this;
    }

    public MessageDrivenContainerBuilder withResourceAdapter(String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
        return this;
    }

    public void setResourceAdapter(String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public String getResourceAdapter() {
        return resourceAdapter;
    }

    public MessageDrivenContainerBuilder withMessageListenerInterface(String messageListenerInterface) {
        this.messageListenerInterface = messageListenerInterface;
        return this;
    }

    public void setMessageListenerInterface(String messageListenerInterface) {
        this.messageListenerInterface = messageListenerInterface;
    }

    public String getMessageListenerInterface() {
        return messageListenerInterface;
    }

    public MessageDrivenContainerBuilder withActivationSpecClass(String activationSpecClass) {
        this.activationSpecClass = activationSpecClass;
        return this;
    }

    public void setActivationSpecClass(String activationSpecClass) {
        this.activationSpecClass = activationSpecClass;
    }

    public String getActivationSpecClass() {
        return activationSpecClass;
    }

    public MessageDrivenContainerBuilder withInstanceLimit(int instanceLimit) {
        this.instanceLimit = instanceLimit;
        return this;
    }

    public void setInstanceLimit(int instanceLimit) {
        this.instanceLimit = instanceLimit;
    }

    public int getInstanceLimit() {
        return instanceLimit;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
