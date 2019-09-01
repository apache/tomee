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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.oejb2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for activation-config-propertyType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="activation-config-propertyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="activation-config-property-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="activation-config-property-value" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activation-config-propertyType", propOrder = {
    "activationConfigPropertyName",
    "activationConfigPropertyValue"
})
public class ActivationConfigPropertyType {

    @XmlElement(name = "activation-config-property-name", required = true)
    protected String activationConfigPropertyName;
    @XmlElement(name = "activation-config-property-value", required = true)
    protected String activationConfigPropertyValue;

    /**
     * Gets the value of the activationConfigPropertyName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getActivationConfigPropertyName() {
        return activationConfigPropertyName;
    }

    /**
     * Sets the value of the activationConfigPropertyName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setActivationConfigPropertyName(final String value) {
        this.activationConfigPropertyName = value;
    }

    /**
     * Gets the value of the activationConfigPropertyValue property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getActivationConfigPropertyValue() {
        return activationConfigPropertyValue;
    }

    /**
     * Sets the value of the activationConfigPropertyValue property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setActivationConfigPropertyValue(final String value) {
        this.activationConfigPropertyValue = value;
    }

}
