/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

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
 * Declares the handler for a port-component. Handlers can access the
 * init-param name/value pairs using the HandlerInfo interface. If
 * port-name is not specified, the handler is assumed to be associated
 * with all ports of the service.
 * <p/>
 * Used in: service-ref
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-ref_handlerType", propOrder = {
        "description",
        "displayName",
        "icon",
        "handlerName",
        "handlerClass",
        "initParam",
        "soapHeader",
        "soapRole",
        "portName"
        })
public class ServiceRefHandlerType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "display-name", required = true)
    protected List<Text> displayName;
    @XmlElement(required = true)
    protected List<IconType> icon;
    @XmlElement(name = "handler-name", required = true)
    protected String handlerName;
    @XmlElement(name = "handler-class", required = true)
    protected String handlerClass;
    @XmlElement(name = "init-param", required = true)
    protected List<ParamValueType> initParam;
    @XmlElement(name = "soap-header", required = true)
    protected List<String> soapHeader;
    @XmlElement(name = "soap-role", required = true)
    protected List<String> soapRole;
    @XmlElement(name = "port-name", required = true)
    protected List<String> portName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the displayName property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDisplayName().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<Text>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getIcon().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link IconType }
     */
    public List<IconType> getIcon() {
        if (icon == null) {
            icon = new ArrayList<IconType>();
        }
        return this.icon;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String value) {
        this.handlerName = value;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String value) {
        this.handlerClass = value;
    }

    /**
     * Gets the value of the initParam property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the initParam property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getInitParam().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ParamValueType }
     */
    public List<ParamValueType> getInitParam() {
        if (initParam == null) {
            initParam = new ArrayList<ParamValueType>();
        }
        return this.initParam;
    }

    /**
     * Gets the value of the soapHeader property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the soapHeader property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getSoapHeader().add(newItem);
     * <p/>
     */
    public List<String> getSoapHeader() {
        if (soapHeader == null) {
            soapHeader = new ArrayList<String>();
        }
        return this.soapHeader;
    }

    /**
     * Gets the value of the soapRole property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the soapRole property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getSoapRole().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getSoapRole() {
        if (soapRole == null) {
            soapRole = new ArrayList<String>();
        }
        return this.soapRole;
    }

    /**
     * Gets the value of the portName property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the portName property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPortName().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getPortName() {
        if (portName == null) {
            portName = new ArrayList<String>();
        }
        return this.portName;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
