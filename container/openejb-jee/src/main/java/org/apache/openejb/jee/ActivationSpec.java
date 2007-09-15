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
 * The activationspecType specifies an activation
 * specification.  The information includes fully qualified
 * Java class name of an activation specification and a set of
 * required configuration property names.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activationspecType", propOrder = {
        "activationSpecClass",
        "requiredConfigProperty"
})
public class ActivationSpec {

    @XmlElement(name = "activationspec-class", required = true)
    protected String activationSpecClass;
    @XmlElement(name = "required-config-property")
    protected List<RequiredConfigProperty> requiredConfigProperty;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getActivationSpecClass() {
        return activationSpecClass;
    }

    public void setActivationSpecClass(String value) {
        this.activationSpecClass = value;
    }

    public List<RequiredConfigProperty> getRequiredConfigProperty() {
        if (requiredConfigProperty == null) {
            requiredConfigProperty = new ArrayList<RequiredConfigProperty>();
        }
        return this.requiredConfigProperty;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
