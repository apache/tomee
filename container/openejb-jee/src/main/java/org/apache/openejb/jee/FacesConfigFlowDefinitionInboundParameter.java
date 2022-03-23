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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p class="changed_added_2_2">A named parameter whose value will be populated
 * with a correspondingly named parameter within an "outbound-parameter" element.<p>
 *
 *
 *
 *
 *
 * <p>Java class for faces-config-flow-definition-inbound-parameterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-flow-definition-inbound-parameterType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://xmlns.jcp.org/xml/ns/javaee}java-identifierType"/&gt;
 *         &lt;element name="value" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-parameter-valueType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definition-inbound-parameterType", propOrder = {
    "name",
    "value"
})
public class FacesConfigFlowDefinitionInboundParameter {

    @XmlElement(required = true)
    protected JavaIdentifier name;
    @XmlElement(required = true)
    protected FacesConfigFlowDefinitionParameterValue value;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link JavaIdentifier }
     */
    public JavaIdentifier getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link JavaIdentifier }
     */
    public void setName(final JavaIdentifier value) {
        this.name = value;
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is
     * {@link FacesConfigFlowDefinitionParameterValue }
     */
    public FacesConfigFlowDefinitionParameterValue getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link FacesConfigFlowDefinitionParameterValue }
     */
    public void setValue(final FacesConfigFlowDefinitionParameterValue value) {
        this.value = value;
    }

}
