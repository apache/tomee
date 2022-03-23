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

import org.apache.openejb.config.SystemProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}SystemProperty" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Container" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}JndiProvider" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}SecurityService" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}TransactionManager" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}ConnectionManager" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}ProxyFactory" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Connector" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Resource" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Deployments" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"systemProperties", "container", "jndiProvider", "securityService", "transactionManager", "connectionManager", "proxyFactory", "connector", "resource", "deployments", "services"})
@XmlRootElement(name = "openejb")
public class Openejb {

    @XmlElement(name = "Container", required = true)
    protected List<Container> container;
    @XmlElement(name = "JndiProvider")
    protected List<JndiProvider> jndiProvider;
    @XmlElement(name = "SecurityService")
    protected SecurityService securityService;
    @XmlElement(name = "TransactionManager")
    protected TransactionManager transactionManager;
    @XmlElement(name = "ConnectionManager")
    protected ConnectionManager connectionManager;
    @XmlElement(name = "ProxyFactory")
    protected ProxyFactory proxyFactory;
    @XmlElement(name = "Connector")
    protected List<Connector> connector;
    @XmlElement(name = "Resource")
    protected List<Resource> resource;
    @XmlElement(name = "Deployments")
    protected List<Deployments> deployments;
    @XmlElement(name = "Service")
    protected List<Service> services;
    @XmlElement(name = "System-Property")
    protected List<SystemProperty> systemProperties;

    /**
     * Gets the value of the container property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the container property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContainer().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Container }
     */
    public List<Container> getContainer() {
        if (container == null) {
            container = new ArrayList<>();
        }
        return this.container;
    }

    /**
     * Gets the value of the jndiProvider property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jndiProvider property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJndiProvider().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JndiProvider }
     */
    public List<JndiProvider> getJndiProvider() {
        if (jndiProvider == null) {
            jndiProvider = new ArrayList<>();
        }
        return this.jndiProvider;
    }

    /**
     * Gets the value of the securityService property.
     *
     * @return possible object is
     * {@link SecurityService }
     */
    public SecurityService getSecurityService() {
        return securityService;
    }

    /**
     * Sets the value of the securityService property.
     *
     * @param value allowed object is
     *              {@link SecurityService }
     */
    public void setSecurityService(final SecurityService value) {
        this.securityService = value;
    }

    /**
     * Gets the value of the transactionManager property.
     *
     * @return possible object is
     * {@link TransactionManager }
     */
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Sets the value of the transactionManager property.
     *
     * @param value allowed object is
     *              {@link TransactionManager }
     */
    public void setTransactionManager(final TransactionManager value) {
        this.transactionManager = value;
    }

    /**
     * Gets the value of the connectionManager property.
     *
     * @return possible object is
     * {@link ConnectionManager }
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Sets the value of the connectionManager property.
     *
     * @param value allowed object is
     *              {@link ConnectionManager }
     */
    public void setConnectionManager(final ConnectionManager value) {
        this.connectionManager = value;
    }

    /**
     * Gets the value of the proxyFactory property.
     *
     * @return possible object is
     * {@link ProxyFactory }
     */
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    /**
     * Sets the value of the proxyFactory property.
     *
     * @param value allowed object is
     *              {@link ProxyFactory }
     */
    public void setProxyFactory(final ProxyFactory value) {
        this.proxyFactory = value;
    }

    /**
     * Gets the value of the connector property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnector().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Connector }
     */
    public List<Connector> getConnector() {
        if (connector == null) {
            connector = new ArrayList<>();
        }
        return this.connector;
    }

    /**
     * Gets the value of the resource property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resource property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResource().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Resource }
     */
    public List<Resource> getResource() {
        if (resource == null) {
            resource = new ArrayList<>();
        }

        final List<Connector> connectors = getConnector();
        if (connectors.size() > 0) {
            for (final Connector connector : connectors) {
                final Resource resource = new Resource();
                resource.setJar(connector.getJar());
                resource.setId(connector.getId());
                resource.setType(connector.getType());
                resource.setProvider(connector.getProvider());
                resource.getProperties().putAll(connector.getProperties());
                this.resource.add(resource);
            }
            connectors.clear();
        }
        return this.resource;
    }

    /**
     * Gets the value of the deployments property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deployments property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeployments().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Deployments }
     */
    public List<Deployments> getDeployments() {
        if (deployments == null) {
            deployments = new ArrayList<>();
        }
        return this.deployments;
    }

    public List<Service> getServices() {
        if (services == null) {
            services = new ArrayList<>();
        }
        return this.services;
    }

    public List<SystemProperty> getSystemProperties() {
        if (systemProperties == null) {
            systemProperties = new LinkedList<>();
        }
        return systemProperties;
    }

    public void add(final Object service) {
        if (service instanceof Container) {
            getContainer().add((Container) service);
        } else if (service instanceof Connector) {
            getConnector().add((Connector) service);
        } else if (service instanceof Resource) {
            getResource().add((Resource) service);
        } else if (service instanceof JndiProvider) {
            getJndiProvider().add((JndiProvider) service);
        } else if (service instanceof ConnectionManager) {
            setConnectionManager((ConnectionManager) service);
        } else if (service instanceof ProxyFactory) {
            setProxyFactory((ProxyFactory) service);
        } else if (service instanceof TransactionManager) {
            setTransactionManager((TransactionManager) service);
        } else if (service instanceof SecurityService) {
            setSecurityService((SecurityService) service);
        } else if (service instanceof Deployments) {
            getDeployments().add((Deployments) service);
        } else if (service instanceof Service) {
            getServices().add((Service) service);
        } else if (service instanceof SystemProperty) {
            getSystemProperties().add((SystemProperty) service);
        }
    }
}
