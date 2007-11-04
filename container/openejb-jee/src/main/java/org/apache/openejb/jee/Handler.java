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
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Declares the handler for a port-component. Handlers can access the
 * init-param name/value pairs using the HandlerInfo interface.
 * <p/>
 * Used in: port-component
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "port-component_handlerType", propOrder = {
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
public class Handler {
    protected List<String> description;
    @XmlElement(name = "display-name")
    protected List<String> displayName;
    @XmlElement(name = "icon")
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "handler-name", required = true)
    protected String handlerName;
    @XmlElement(name = "handler-class", required = true)
    protected String handlerClass;
    @XmlElement(name = "init-param")
    protected List<ParamValue> initParam;
    @XmlElement(name = "soap-header")
    protected List<QName> soapHeader;
    @XmlElement(name = "soap-role")
    protected List<String> soapRole;
    // only used by service-refs
    @XmlElement(name = "port-name", required = true)
    protected List<String> portName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<String>();
        }
        return this.description;
    }

    public List<String> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<String>();
        }
        return this.displayName;
    }

    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
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

    public List<QName> getSoapHeader() {
        if (soapHeader == null) {
            soapHeader = new ArrayList<QName>();
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
