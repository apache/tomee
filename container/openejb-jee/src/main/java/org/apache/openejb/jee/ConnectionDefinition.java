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
 * The connection-definitionType defines a set of connection
 * interfaces and classes pertaining to a particular connection
 * type. This also includes configurable properties for
 * ManagedConnectionFactory instances that may be produced out
 * of this set.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connection-definitionType", propOrder = {
        "managedConnectionFactoryClass",
        "configProperty",
        "connectionFactoryInterface",
        "connectionFactoryImplClass",
        "connectionInterface",
        "connectionImplClass"
})
public class ConnectionDefinition {

    @XmlElement(name = "managedconnectionfactory-class", required = true)
    protected String managedConnectionFactoryClass;
    @XmlElement(name = "config-property")
    protected List<ConfigProperty> configProperty;
    @XmlElement(name = "connectionfactory-interface", required = true)
    protected String connectionFactoryInterface;
    @XmlElement(name = "connectionfactory-impl-class", required = true)
    protected String connectionFactoryImplClass;
    @XmlElement(name = "connection-interface", required = true)
    protected String connectionInterface;
    @XmlElement(name = "connection-impl-class", required = true)
    protected String connectionImplClass;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    public void setManagedConnectionFactoryClass(String value) {
        this.managedConnectionFactoryClass = value;
    }

    public List<ConfigProperty> getConfigProperty() {
        if (configProperty == null) {
            configProperty = new ArrayList<ConfigProperty>();
        }
        return this.configProperty;
    }

    public String getConnectionFactoryInterface() {
        return connectionFactoryInterface;
    }

    public void setConnectionFactoryInterface(String value) {
        this.connectionFactoryInterface = value;
    }

    public String getConnectionFactoryImplClass() {
        return connectionFactoryImplClass;
    }

    public void setConnectionFactoryImplClass(String value) {
        this.connectionFactoryImplClass = value;
    }

    public String getConnectionInterface() {
        return connectionInterface;
    }

    public void setConnectionInterface(String value) {
        this.connectionInterface = value;
    }

    public String getConnectionImplClass() {
        return connectionImplClass;
    }

    public void setConnectionImplClass(String value) {
        this.connectionImplClass = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
