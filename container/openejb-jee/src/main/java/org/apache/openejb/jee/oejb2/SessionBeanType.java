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

package org.apache.openejb.jee.oejb2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for session-beanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="session-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="local-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="cache-size" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;group ref="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}tssGroup" minOccurs="0"/>
 *         &lt;group ref="{http://geronimo.apache.org/xml/ns/naming-1.2}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="web-service-address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="web-service-virtual-host" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="web-service-security" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}web-service-securityType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "session-beanType", propOrder = {
    "ejbName",
    "jndiName",
    "localJndiName",
    "cacheSize",
    "tssLink",
    "tss",
    "abstractNamingEntry",
    "persistenceContextRef",
    "persistenceUnitRef",
    "ejbRef",
    "ejbLocalRef",
    "serviceRef",
    "resourceRef",
    "resourceEnvRef",
    "webServiceAddress",
    "webServiceVirtualHost",
    "webServiceSecurity"
})
public class SessionBeanType implements EnterpriseBean {

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "jndi-name")
    protected List<String> jndiName;
    @XmlElement(name = "local-jndi-name")
    protected List<String> localJndiName;
    @XmlElement(name = "cache-size")
    protected Integer cacheSize;
    @XmlElement(name = "tss-link")
    protected String tssLink;
    @XmlElement(name="tss", namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2")
    protected PatternType tss;

    @XmlElementRef(name = "abstract-naming-entry", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractNamingEntryType>> abstractNamingEntry;

    @XmlElement(name = "persistence-contenxt-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<PersistenceContextRefType> persistenceContextRef;

    @XmlElement(name = "persistence-unit-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<PersistenceUnitRefType> persistenceUnitRef;

    @XmlElement(name = "ejb-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbRefType> ejbRef;

    @XmlElement(name = "ejb-local-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbLocalRefType> ejbLocalRef;

    @XmlElement(name = "service-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ServiceRefType> serviceRef;

    @XmlElement(name = "resource-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceRefType> resourceRef;

    @XmlElement(name = "resource-env-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceEnvRefType> resourceEnvRef;

    @XmlElement(name = "web-service-address")
    protected String webServiceAddress;

    @XmlElement(name = "web-service-virtual-host")
    protected List<String> webServiceVirtualHost;

    @XmlElement(name = "web-service-security")
    protected WebServiceSecurityType webServiceSecurity;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjbName(String value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the jndiName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jndiName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJndiName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getJndiName() {
        if (jndiName == null) {
            jndiName = new ArrayList<String>();
        }
        return this.jndiName;
    }

    /**
     * Gets the value of the localJndiName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localJndiName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalJndiName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLocalJndiName() {
        if (localJndiName == null) {
            localJndiName = new ArrayList<String>();
        }
        return this.localJndiName;
    }

    /**
     * Gets the value of the cacheSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the value of the cacheSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCacheSize(Integer value) {
        this.cacheSize = value;
    }

    /**
     * Gets the value of the tssLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTssLink() {
        return tssLink;
    }

    /**
     * Sets the value of the tssLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTssLink(String value) {
        this.tssLink = value;
    }

    /**
     * Gets the value of the tss property.
     * 
     * @return
     *     possible object is
     *     {@link PatternType }
     *     
     */
    public PatternType getTss() {
        return tss;
    }

    /**
     * Sets the value of the tss property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatternType }
     *     
     */
    public void setTss(PatternType value) {
        this.tss = value;
    }

    public List<JAXBElement<? extends AbstractNamingEntryType>> getAbstractNamingEntry() {
        if (abstractNamingEntry == null) {
            abstractNamingEntry = new ArrayList<JAXBElement<? extends AbstractNamingEntryType>>();
        }
        return this.abstractNamingEntry;
    }

    public List<PersistenceContextRefType> getPersistenceContextRef() {
        if (persistenceContextRef == null){
            persistenceContextRef = new ArrayList<PersistenceContextRefType>();
        }
        return persistenceContextRef;
    }

    public List<PersistenceUnitRefType> getPersistenceUnitRef() {
        if (persistenceUnitRef == null){
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

    /**
     * Gets the value of the webServiceAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebServiceAddress() {
        return webServiceAddress;
    }

    /**
     * Sets the value of the webServiceAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebServiceAddress(String value) {
        this.webServiceAddress = value;
    }

    /**
     * Gets the value of the webServiceVirtualHost property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the webServiceVirtualHost property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWebServiceVirtualHost().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getWebServiceVirtualHost() {
        if (webServiceVirtualHost == null) {
            webServiceVirtualHost = new ArrayList<String>();
        }
        return this.webServiceVirtualHost;
    }

    /**
     * Gets the value of the webServiceSecurity property.
     * 
     * @return
     *     possible object is
     *     {@link WebServiceSecurityType }
     *     
     */
    public WebServiceSecurityType getWebServiceSecurity() {
        return webServiceSecurity;
    }

    /**
     * Sets the value of the webServiceSecurity property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebServiceSecurityType }
     *     
     */
    public void setWebServiceSecurity(WebServiceSecurityType value) {
        this.webServiceSecurity = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
