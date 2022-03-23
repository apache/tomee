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
package org.apache.openejb.jee.wls;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for method complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="method"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.bea.com/ns/weblogic/90}description" minOccurs="0"/&gt;
 *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="method-intf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="method-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="method-params" type="{http://www.bea.com/ns/weblogic/90}method-params" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "method", propOrder = {
    "description",
    "ejbName",
    "methodIntf",
    "methodName",
    "methodParams"
})
public class Method {

    protected Description description;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "method-intf")
    protected String methodIntf;
    @XmlElement(name = "method-name", required = true)
    protected String methodName;
    @XmlElement(name = "method-params")
    protected MethodParams methodParams;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link Description }
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link Description }
     */
    public void setDescription(final Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the ejbName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbName(final String value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the methodIntf property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMethodIntf() {
        return methodIntf;
    }

    /**
     * Sets the value of the methodIntf property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMethodIntf(final String value) {
        this.methodIntf = value;
    }

    /**
     * Gets the value of the methodName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the value of the methodName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMethodName(final String value) {
        this.methodName = value;
    }

    /**
     * Gets the value of the methodParams property.
     *
     * @return possible object is
     * {@link MethodParams }
     */
    public MethodParams getMethodParams() {
        return methodParams;
    }

    /**
     * Sets the value of the methodParams property.
     *
     * @param value allowed object is
     *              {@link MethodParams }
     */
    public void setMethodParams(final MethodParams value) {
        this.methodParams = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
