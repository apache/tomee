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
public class ServiceRefHandler {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "display-name", required = true)
    protected List<Text> displayName;
    @XmlElement(required = true)
    protected List<Icon> icon;
    @XmlElement(name = "handler-name", required = true)
    protected String handlerName;
    @XmlElement(name = "handler-class", required = true)
    protected String handlerClass;
    @XmlElement(name = "init-param", required = true)
    protected List<ParamValue> initParam;
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

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public List<Text> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<Text>();
        }
        return this.displayName;
    }

    public List<Icon> getIcon() {
        if (icon == null) {
            icon = new ArrayList<Icon>();
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

    public List<ParamValue> getInitParam() {
        if (initParam == null) {
            initParam = new ArrayList<ParamValue>();
        }
        return this.initParam;
    }

    public List<String> getSoapHeader() {
        if (soapHeader == null) {
            soapHeader = new ArrayList<String>();
        }
        return this.soapHeader;
    }

    public List<String> getSoapRole() {
        if (soapRole == null) {
            soapRole = new ArrayList<String>();
        }
        return this.soapRole;
    }

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
