/*
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
package org.apache.openejb.config.sys;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Container" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}JndiProvider" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}ConnectionManager" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Connector" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Resource" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"container", "jndiProvider", "connectionManager", "resource"})
@XmlRootElement(name = "resources")
public class Resources {

    @XmlElement(name = "Container", required = true)
    protected List<Container> container;

    @XmlElement(name = "JndiProvider")
    protected List<JndiProvider> jndiProvider;

    @XmlElement(name = "ConnectionManager")
    protected ConnectionManager connectionManager;

    @XmlElement(name = "Resource")
    protected List<Resource> resource;

    /**
     * Gets the value of the container property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the container property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContainer().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link org.apache.openejb.config.sys.Container }
     */
    public List<Container> getContainer() {
        if (container == null) {
            container = new ArrayList<Container>();
        }
        return this.container;
    }

    /**
     * Gets the value of the jndiProvider property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jndiProvider property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJndiProvider().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link org.apache.openejb.config.sys.JndiProvider }
     */
    public List<JndiProvider> getJndiProvider() {
        if (jndiProvider == null) {
            jndiProvider = new ArrayList<JndiProvider>();
        }
        return this.jndiProvider;
    }

    /**
     * Gets the value of the connectionManager property.
     *
     * @return possible object is
     *         {@link org.apache.openejb.config.sys.ConnectionManager }
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Sets the value of the connectionManager property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.config.sys.ConnectionManager }
     */
    public void setConnectionManager(ConnectionManager value) {
        this.connectionManager = value;
    }

    /**
     * Gets the value of the resource property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resource property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResource().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link org.apache.openejb.config.sys.Resource }
     */
    public List<Resource> getResource() {
        if (resource == null) {
            resource = new ArrayList<Resource>();
        }

        return this.resource;
    }

    public void add(Object service) {
        if (service instanceof Container) {
            getContainer().add((Container) service);
        } else if (service instanceof Resource) {
            getResource().add((Resource) service);
        } else if (service instanceof JndiProvider) {
            getJndiProvider().add((JndiProvider) service);
        } else if (service instanceof ConnectionManager) {
            setConnectionManager((ConnectionManager) service);
        }
    }
}
