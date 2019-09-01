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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p class="changed_added_2_2">A parameter to pass when calling the method
 * identified in the "method" element that is a sibling of this element.<p>
 *
 *
 *
 *
 *
 * <p>Java class for faces-config-flow-definition-flow-call-parameterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-flow-definition-flow-call-parameterType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="class" type="{http://xmlns.jcp.org/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="value" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-parameter-valueType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definition-flow-call-parameterType", propOrder = {
    "clazz",
    "value"
})
public class FacesConfigFlowDefinitionFlowCallParameter {

    @XmlElement(name = "class")
    protected String clazz;
    @XmlElement(required = true)
    protected FacesConfigFlowDefinitionParameterValue value;

    /**
     * Gets the value of the clazz property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClazz(final String value) {
        this.clazz = value;
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
