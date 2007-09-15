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

/**
 * The config-propertyType contains a declaration of a single
 * configuration property that may be used for providing
 * configuration information.
 * <p/>
 * The declaration consists of an optional description, name,
 * type and an optional value of the configuration property. If
 * the resource adapter provider does not specify a value than
 * the deployer is responsible for providing a valid value for
 * a configuration property.
 * <p/>
 * Any bounds or well-defined values of properties should be
 * described in the description element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "config-propertyType", propOrder = {
        "description",
        "configPropertyName",
        "configPropertyType",
        "configPropertyValue"
})
public class ConfigProperty {

    protected List<Text> description;
    @XmlElement(name = "config-property-name", required = true)
    protected String configPropertyName;
    @XmlElement(name = "config-property-type", required = true)
    protected String configPropertyType;
    @XmlElement(name = "config-property-value")
    protected String configPropertyValue;
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

    public String getConfigPropertyName() {
        return configPropertyName;
    }

    public void setConfigPropertyName(String value) {
        this.configPropertyName = value;
    }

    public String getConfigPropertyType() {
        return configPropertyType;
    }

    public void setConfigPropertyType(String value) {
        this.configPropertyType = value;
    }

    public String getConfigPropertyValue() {
        return configPropertyValue;
    }

    public void setConfigPropertyValue(String value) {
        this.configPropertyValue = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
