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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * The required-config-propertyType contains a declaration
 * of a single configuration property used for specifying a
 * required configuration property name. It is used
 * by required-config-property elements.
 * <p/>
 * Example:
 * <p/>
 * <required-config-property>Destination</required-config-property>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "required-config-propertyType", propOrder = {
        "descriptions",
        "configPropertyName"
})
public class RequiredConfigProperty {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "config-property-name", required = true)
    protected String configPropertyName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public RequiredConfigProperty() {
    }

    public RequiredConfigProperty(String configPropertyName) {
        this.configPropertyName = configPropertyName;
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getConfigPropertyName() {
        return configPropertyName;
    }

    public void setConfigPropertyName(String value) {
        this.configPropertyName = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
