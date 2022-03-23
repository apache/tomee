/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.wsclient;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.openejb.jee.was.v6.common.CompatibilityDescriptionGroup;
import org.apache.openejb.jee.was.v6.common.ParamValue;
import org.apache.openejb.jee.was.v6.common.QName;
import org.apache.openejb.jee.was.v6.java.JavaClass;

/**
 * Declares the handler for a port-component. Handlers can access the init-param
 * name/value pairs using the HandlerInfo interface. If port-name is not
 * specified, the handler is assumed to be associated with all ports of the
 * service.
 *
 * Used in: service-ref
 *
 *
 * Java class for Handler complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="Handler"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{common.xmi}CompatibilityDescriptionGroup"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="soapRoles" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="portNames" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="handlerClass" type="{java.xmi}JavaClass"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="initParams" type="{common.xmi}ParamValue"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="soapHeaders" type="{common.xmi}QName"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="handlerClass" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="handlerName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Handler", propOrder = {"soapRoles", "portNames",
    "handlerClasses", "initParams", "soapHeaders"})
public class Handler extends CompatibilityDescriptionGroup {

    @XmlElement(nillable = true)
    protected List<String> soapRoles;
    @XmlElement(nillable = true)
    protected List<String> portNames;
    @XmlElement(name = "handlerClass")
    protected List<JavaClass> handlerClasses;
    protected List<ParamValue> initParams;
    protected List<QName> soapHeaders;
    @XmlAttribute(name = "handlerClass")
    protected String handlerClassString;
    @XmlAttribute
    protected String handlerName;

    /**
     * Gets the value of the soapRoles property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the soapRoles property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSoapRoles().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getSoapRoles() {
        if (soapRoles == null) {
            soapRoles = new ArrayList<String>();
        }
        return this.soapRoles;
    }

    /**
     * Gets the value of the portNames property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the portNames property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getPortNames().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getPortNames() {
        if (portNames == null) {
            portNames = new ArrayList<String>();
        }
        return this.portNames;
    }

    /**
     * Gets the value of the handlerClasses property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the handlerClasses property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getHandlerClasses().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getHandlerClasses() {
        if (handlerClasses == null) {
            handlerClasses = new ArrayList<JavaClass>();
        }
        return this.handlerClasses;
    }

    /**
     * Gets the value of the initParams property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the initParams property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getInitParams().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ParamValue }
     */
    public List<ParamValue> getInitParams() {
        if (initParams == null) {
            initParams = new ArrayList<ParamValue>();
        }
        return this.initParams;
    }

    /**
     * Gets the value of the soapHeaders property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the soapHeaders property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSoapHeaders().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link QName }
     */
    public List<QName> getSoapHeaders() {
        if (soapHeaders == null) {
            soapHeaders = new ArrayList<QName>();
        }
        return this.soapHeaders;
    }

    /**
     * Gets the value of the handlerClassString property.
     *
     * @return possible object is {@link String }
     */
    public String getHandlerClassString() {
        return handlerClassString;
    }

    /**
     * Sets the value of the handlerClassString property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHandlerClassString(final String value) {
        this.handlerClassString = value;
    }

    /**
     * Gets the value of the handlerName property.
     *
     * @return possible object is {@link String }
     */
    public String getHandlerName() {
        return handlerName;
    }

    /**
     * Sets the value of the handlerName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHandlerName(final String value) {
        this.handlerName = value;
    }

}
