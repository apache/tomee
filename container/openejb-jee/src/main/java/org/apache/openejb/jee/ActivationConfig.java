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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The activation-configType defines information about the
 * expected configuration properties of the message-driven bean
 * in its operational environment. This may include information
 * about message acknowledgement, message selector, expected
 * destination type, etc.
 * <p/>
 * The configuration information is expressed in terms of
 * name/value configuration properties.
 * <p/>
 * The properties that are recognized for a particular
 * message-driven bean are determined by the messaging type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activation-configType", propOrder = {
        "description",
        "activationConfigProperty"
        })
public class ActivationConfig {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "activation-config-property", required = true)
    protected List<ActivationConfigProperty> activationConfigProperty;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public void addProperty(String name, String value) {
        getActivationConfigProperty().add(new ActivationConfigProperty(name, value));
    }
    
    public List<ActivationConfigProperty> getActivationConfigProperty() {
        if (activationConfigProperty == null) {
            activationConfigProperty = new ArrayList<ActivationConfigProperty>();
        }
        return this.activationConfigProperty;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public Properties toProperties() {
        Properties properties = new Properties();
        for (ActivationConfigProperty property : getActivationConfigProperty()) {
            String name = property.getActivationConfigPropertyName();
            String value = property.getActivationConfigPropertyValue();
            properties.put(name, value);
        }
        return properties;
    }
}
