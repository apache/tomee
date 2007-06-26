/**
 *
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
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}SecurityService" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}TransactionManager" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}ConnectionManager" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}ProxyFactory" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Connector" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Resource" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Deployments" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"container", "jndiProvider", "securityService", "transactionManager", "connectionManager", "proxyFactory", "connector", "resource", "deployments"})
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
     * {@link Container }
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
     * {@link JndiProvider }
     */
    public List<JndiProvider> getJndiProvider() {
        if (jndiProvider == null) {
            jndiProvider = new ArrayList<JndiProvider>();
        }
        return this.jndiProvider;
    }

    /**
     * Gets the value of the securityService property.
     *
     * @return possible object is
     *         {@link SecurityService }
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
    public void setSecurityService(SecurityService value) {
        this.securityService = value;
    }

    /**
     * Gets the value of the transactionManager property.
     *
     * @return possible object is
     *         {@link TransactionManager }
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
    public void setTransactionManager(TransactionManager value) {
        this.transactionManager = value;
    }

    /**
     * Gets the value of the connectionManager property.
     *
     * @return possible object is
     *         {@link ConnectionManager }
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
    public void setConnectionManager(ConnectionManager value) {
        this.connectionManager = value;
    }

    /**
     * Gets the value of the proxyFactory property.
     *
     * @return possible object is
     *         {@link ProxyFactory }
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
    public void setProxyFactory(ProxyFactory value) {
        this.proxyFactory = value;
    }

    /**
     * Gets the value of the connector property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnector().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Connector }
     */
    public List<Connector> getConnector() {
        if (connector == null) {
            connector = new ArrayList<Connector>();
        }
        return this.connector;
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
     * {@link Resource }
     */
    public List<Resource> getResource() {
        if (resource == null) {
            resource = new ArrayList<Resource>();
        }
        return this.resource;
    }

    /**
     * Gets the value of the deployments property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deployments property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeployments().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Deployments }
     */
    public List<Deployments> getDeployments() {
        if (deployments == null) {
            deployments = new ArrayList<Deployments>();
        }
        return this.deployments;
    }


    /**
     * @deprecated use collections version
     */
    public void addConnector(Connector connector) throws IndexOutOfBoundsException {
        this.connector.add(connector);
    }

    /**
     * @deprecated use collections version
     */
    public Connector[] getConnectorArray() {
        return getConnector().toArray(new Connector[connector.size()]);
    }

    /**
     * @deprecated use collections version
     */
    public void addContainer(Container container) throws IndexOutOfBoundsException {
        this.container.add(container);
    }

    /**
     * @deprecated use collections version
     */
    public Container[] getContainerArray() {
        return getContainer().toArray(new Container[container.size()]);
    }

    /**
     * @deprecated use collections version
     */
    public void addDeployments(Deployments deployments) throws IndexOutOfBoundsException {
        this.deployments.add(deployments);
    }

    /**
     * @deprecated use collections version
     */
    public Deployments[] getDeploymentsArray() {
        return getDeployments().toArray(new Deployments[deployments.size()]);
    }

    /**
     * @deprecated use collections version
     */
    public void addJndiProvider(JndiProvider jndiProvider) throws IndexOutOfBoundsException {
        this.jndiProvider.add(jndiProvider);
    }

    /**
     * @deprecated use collections version
     */
    public JndiProvider[] getJndiProviderArray() {
        return getJndiProvider().toArray(new JndiProvider[jndiProvider.size()]);
    }

    /**
     * @deprecated use collections version
     */
    public void addResource(Resource resource) throws IndexOutOfBoundsException {
        this.resource.add(resource);
    }

    /**
     * @deprecated use collections version
     */
    public Resource[] getResourceArray() {
        return getResource().toArray(new Resource[resource.size()]);
    }

}
