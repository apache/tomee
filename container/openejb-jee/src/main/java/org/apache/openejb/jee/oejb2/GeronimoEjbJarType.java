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
package org.apache.openejb.jee.oejb2;

import org.apache.openejb.jee.jpa.unit.Persistence;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Java class for geronimo-ejb-jarType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="geronimo-ejb-jarType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/deployment-1.2}environment" minOccurs="0"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/j2ee/application-2.0}clustering" minOccurs="0"/&gt;
 *         &lt;element name="openejb-jar" type="{http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0}openejb-jarType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://geronimo.apache.org/xml/ns/naming-1.2}jndiEnvironmentRefsGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/naming-1.2}message-destination" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="tss-link" type="{http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0}tss-linkType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="web-service-binding" type="{http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0}web-service-bindingType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/j2ee/application-2.0}security" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://geronimo.apache.org/xml/ns/deployment-1.2}service"/&gt;
 *           &lt;element ref="{http://java.sun.com/xml/ns/persistence}persistence"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "geronimo-ejb-jarType", namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", propOrder = {
    "environment",
    "clustering",
    "openejbJar",
    "abstractNamingEntry",
    "persistenceContextRef",
    "persistenceUnitRef",
    "ejbRef",
    "ejbLocalRef",
    "serviceRef",
    "resourceRef",
    "resourceEnvRef",
    "messageDestination",
    "tssLink",
    "webServiceBinding",
    "security",
    "service",
    "persistence"
})
@XmlRootElement(name = "ejb-jar")
public class GeronimoEjbJarType {

    @XmlElement(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected EnvironmentType environment;

    @XmlElementRef(name = "clustering", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractClusteringType> clustering;

    @XmlAnyElement(lax = true)
    protected Object openejbJar;

    @XmlElementRef(name = "abstract-naming-entry", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractNamingEntryType>> abstractNamingEntry;

    @XmlElement(name = "persistence-context-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<PersistenceContextRefType> persistenceContextRef;

    @XmlElement(name = "persistence-unit-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<PersistenceUnitRefType> persistenceUnitRef;

    @XmlElement(name = "ejb-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbRefType> ejbRef;

    @XmlElement(name = "ejb-local-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbLocalRefType> ejbLocalRef;

    @XmlElement(name = "service-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ServiceRefType> serviceRef;

    @XmlElement(name = "resource-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceRefType> resourceRef;

    @XmlElement(name = "resource-env-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceEnvRefType> resourceEnvRef;

    @XmlElement(name = "message-destination", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<MessageDestinationType> messageDestination;

    @XmlElement(name = "tss-link")
    protected List<TssLinkType> tssLink;

    @XmlElement(name = "web-service-binding")
    protected List<WebServiceBindingType> webServiceBinding;

    @XmlElementRef(name = "security", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractSecurityType> security;

    @XmlElementRef(name = "service", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractServiceType>> service;

    @XmlElementRef(name = "persistence", namespace = "http://java.sun.com/xml/ns/persistence", type = Persistence.class)
    protected List<Persistence> persistence;

    /**
     * Gets the value of the environment property.
     *
     * @return possible object is
     * {@link EnvironmentType }
     */
    public EnvironmentType getEnvironment() {
        return environment;
    }

    /**
     * Sets the value of the environment property.
     *
     * @param value allowed object is
     *              {@link EnvironmentType }
     */
    public void setEnvironment(final EnvironmentType value) {
        this.environment = value;
    }

    /**
     * Reference to abstract clustering element defined in
     * imported "geronimo-application-2.0.xsd"
     *
     * @return possible object is
     * {@link AbstractClusteringType }
     */
    public JAXBElement<? extends AbstractClusteringType> getClustering() {
        return clustering;
    }

    /**
     * Sets the value of the clustering property.
     *
     * @param value allowed object is
     *              {@link AbstractClusteringType }
     */
    public void setClustering(final JAXBElement<? extends AbstractClusteringType> value) {
        this.clustering = value;
    }

    /**
     * Gets the value of the jndiEnvironmentRefsGroup property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jndiEnvironmentRefsGroup property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJndiEnvironmentRefsGroup().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ResourceEnvRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link EjbRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oejb2.PersistenceUnitRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oejb2.GbeanRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link EjbLocalRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link ResourceRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link ServiceRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oejb2.PersistenceContextRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractNamingEntryType }{@code >}
     */
//    public List<Object> getJndiEnvironmentRefsGroup() {
//        if (jndiEnvironmentRefsGroup == null) {
//            jndiEnvironmentRefsGroup = new ArrayList<Object>();
//        }
//        return this.jndiEnvironmentRefsGroup;
//    }
    public List<JAXBElement<? extends AbstractNamingEntryType>> getAbstractNamingEntry() {
        if (abstractNamingEntry == null) {
            abstractNamingEntry = new ArrayList<JAXBElement<? extends AbstractNamingEntryType>>();
        }
        return this.abstractNamingEntry;
    }

    public List<PersistenceContextRefType> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new ArrayList<PersistenceContextRefType>();
        }
        return persistenceContextRef;
    }

    public List<PersistenceUnitRefType> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new ArrayList<PersistenceUnitRefType>();
        }
        return persistenceUnitRef;
    }

    public List<EjbRefType> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRefType>();
        }
        return this.ejbRef;
    }

    public List<EjbLocalRefType> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRefType>();
        }
        return this.ejbLocalRef;
    }

    public List<ServiceRefType> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRefType>();
        }
        return this.serviceRef;
    }

    public List<ResourceRefType> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRefType>();
        }
        return this.resourceRef;
    }

    public List<ResourceEnvRefType> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRefType>();
        }
        return this.resourceEnvRef;
    }

    public Object getOpenejbJar() {
        return openejbJar;
    }

    public void setOpenejbJar(final Object openejbJar) {
        this.openejbJar = openejbJar;
    }


    /**
     * Gets the value of the messageDestination property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDestination property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageDestination().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestinationType }
     */
    public List<MessageDestinationType> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestinationType>();
        }
        return this.messageDestination;
    }

    public List<TssLinkType> getTssLink() {
        if (tssLink == null) {
            tssLink = new ArrayList<TssLinkType>();
        }
        return tssLink;
    }

    public List<WebServiceBindingType> getWebServiceBinding() {
        if (webServiceBinding == null) {
            webServiceBinding = new ArrayList<WebServiceBindingType>();
        }
        return webServiceBinding;
    }

    public JAXBElement<? extends AbstractSecurityType> getSecurity() {
        return security;
    }

    public void setSecurity(final JAXBElement<? extends AbstractSecurityType> security) {
        this.security = security;
    }

    /**
     * Gets the value of the service property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the service property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getService().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AbstractServiceType }{@code >}
     * {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oejb2.GbeanType }{@code >}
     */
    public List<JAXBElement<? extends AbstractServiceType>> getService() {
        if (service == null) {
            service = new ArrayList<JAXBElement<? extends AbstractServiceType>>();
        }
        return this.service;
    }

    public List<Persistence> getPersistence() {
        if (persistence == null) {
            persistence = new ArrayList<Persistence>();
        }
        return persistence;
    }
}
