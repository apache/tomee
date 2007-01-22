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

package org.apache.openejb.jee.oej2;

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
 * <p>Java class for entity-beanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="entity-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="local-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}tssGroup" minOccurs="0"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="table-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="static-sql" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType" minOccurs="0"/>
 *           &lt;element name="cmp-field-mapping" maxOccurs="unbounded">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="cmp-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="cmp-field-class" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                     &lt;element name="table-column" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="sql-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                     &lt;element name="type-converter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="primkey-field" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element ref="{http://openejb.apache.org/xml/ns/pkgen-2.1}key-generator" minOccurs="0"/>
 *           &lt;element name="prefetch-group" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="group" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}groupType" maxOccurs="unbounded" minOccurs="0"/>
 *                     &lt;element name="entity-group-mapping" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}entity-group-mappingType" minOccurs="0"/>
 *                     &lt;element name="cmp-field-group-mapping" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}cmp-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/>
 *                     &lt;element name="cmr-field-group-mapping" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}cmr-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="select-for-update" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element name="cache" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="isolation-level">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="read-uncommitted"/>
 *                         &lt;enumeration value="read-committed"/>
 *                         &lt;enumeration value="repeatable-read"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;group ref="{http://geronimo.apache.org/xml/ns/naming-1.2}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="query" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}queryType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "entity-beanType", propOrder = {
    "ejbName",
    "jndiName",
    "localJndiName",
    "tssLink",
    "tss",
    "tableName",
    "staticSql",
    "cmpFieldMapping",
    "primkeyField",
    "keyGenerator",
    "prefetchGroup",
    "selectForUpdate",
    "cache",
    "abstractNamingEntry",
    "ejbRef",
    "ejbLocalRef",
    "serviceRef",
    "resourceRef",
    "resourceEnvRef",
    "query"
})
public class EntityBeanType implements EnterpriseBean {

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;

    @XmlElement(name = "jndi-name")
    protected List<String> jndiName;

    @XmlElement(name = "local-jndi-name")
    protected List<String> localJndiName;

    @XmlElement(name = "tss-link")
    protected String tssLink;

    @XmlElement()
    protected PatternType tss;

    @XmlElement(name = "table-name")
    protected String tableName;

    @XmlElement(name = "static-sql")
    protected EmptyType staticSql;

    @XmlElement(name = "cmp-field-mapping")
    protected List<EntityBeanType.CmpFieldMapping> cmpFieldMapping;

    @XmlElement(name = "primkey-field")
    protected String primkeyField;

    @XmlElement(name = "key-generator", namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1")
    protected KeyGeneratorType keyGenerator;

    @XmlElement(name = "prefetch-group")
    protected EntityBeanType.PrefetchGroup prefetchGroup;

    @XmlElement(name = "select-for-update")
    protected EmptyType selectForUpdate;

    @XmlElement()
    protected EntityBeanType.Cache cache;

    @XmlElementRef(name = "abstract-naming-entry", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractNamingEntryType>> abstractNamingEntry;

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

    @XmlElement()
    protected List<QueryType> query;

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

    /**
     * Gets the value of the tableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableName(String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the staticSql property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getStaticSql() {
        return staticSql;
    }

    /**
     * Sets the value of the staticSql property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setStaticSql(EmptyType value) {
        this.staticSql = value;
    }

    /**
     * Gets the value of the cmpFieldMapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmpFieldMapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmpFieldMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EntityBeanType.CmpFieldMapping }
     * 
     * 
     */
    public List<EntityBeanType.CmpFieldMapping> getCmpFieldMapping() {
        if (cmpFieldMapping == null) {
            cmpFieldMapping = new ArrayList<EntityBeanType.CmpFieldMapping>();
        }
        return this.cmpFieldMapping;
    }

    /**
     * Gets the value of the primkeyField property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrimkeyField() {
        return primkeyField;
    }

    /**
     * Sets the value of the primkeyField property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrimkeyField(String value) {
        this.primkeyField = value;
    }

    /**
     * Gets the value of the keyGenerator property.
     * 
     * @return
     *     possible object is
     *     {@link KeyGeneratorType }
     *     
     */
    public KeyGeneratorType getKeyGenerator() {
        return keyGenerator;
    }

    /**
     * Sets the value of the keyGenerator property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyGeneratorType }
     *     
     */
    public void setKeyGenerator(KeyGeneratorType value) {
        this.keyGenerator = value;
    }

    /**
     * Gets the value of the prefetchGroup property.
     * 
     * @return
     *     possible object is
     *     {@link EntityBeanType.PrefetchGroup }
     *     
     */
    public EntityBeanType.PrefetchGroup getPrefetchGroup() {
        return prefetchGroup;
    }

    /**
     * Sets the value of the prefetchGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityBeanType.PrefetchGroup }
     *     
     */
    public void setPrefetchGroup(EntityBeanType.PrefetchGroup value) {
        this.prefetchGroup = value;
    }

    /**
     * Gets the value of the selectForUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getSelectForUpdate() {
        return selectForUpdate;
    }

    /**
     * Sets the value of the selectForUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setSelectForUpdate(EmptyType value) {
        this.selectForUpdate = value;
    }

    /**
     * Gets the value of the cache property.
     * 
     * @return
     *     possible object is
     *     {@link EntityBeanType.Cache }
     *     
     */
    public EntityBeanType.Cache getCache() {
        return cache;
    }

    /**
     * Sets the value of the cache property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityBeanType.Cache }
     *     
     */
    public void setCache(EntityBeanType.Cache value) {
        this.cache = value;
    }

    /**
     * Gets the value of the abstractNamingEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractNamingEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractNamingEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AbstractNamingEntryType }{@code >}
     * {@link JAXBElement }{@code <}{@link PersistenceContextRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link EntityManagerFactoryRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link GbeanRefType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractNamingEntryType>> getAbstractNamingEntry() {
        if (abstractNamingEntry == null) {
            abstractNamingEntry = new ArrayList<JAXBElement<? extends AbstractNamingEntryType>>();
        }
        return this.abstractNamingEntry;
    }

    /**
     * Gets the value of the ejbRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRefType }
     * 
     * 
     */
    public List<EjbRefType> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRefType>();
        }
        return this.ejbRef;
    }

    /**
     * Gets the value of the ejbLocalRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbLocalRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbLocalRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbLocalRefType }
     * 
     * 
     */
    public List<EjbLocalRefType> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRefType>();
        }
        return this.ejbLocalRef;
    }

    /**
     * Gets the value of the serviceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRefType }
     * 
     * 
     */
    public List<ServiceRefType> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRefType>();
        }
        return this.serviceRef;
    }

    /**
     * Gets the value of the resourceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRefType }
     * 
     * 
     */
    public List<ResourceRefType> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRefType>();
        }
        return this.resourceRef;
    }

    /**
     * Gets the value of the resourceEnvRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceEnvRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvRefType }
     * 
     * 
     */
    public List<ResourceEnvRefType> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRefType>();
        }
        return this.resourceEnvRef;
    }

    /**
     * Gets the value of the query property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the query property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuery().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QueryType }
     * 
     * 
     */
    public List<QueryType> getQuery() {
        if (query == null) {
            query = new ArrayList<QueryType>();
        }
        return this.query;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="isolation-level">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="read-uncommitted"/>
     *               &lt;enumeration value="read-committed"/>
     *               &lt;enumeration value="repeatable-read"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "isolationLevel",
        "size"
    })
    public static class Cache {

        @XmlElement(name = "isolation-level", required = true)
        protected String isolationLevel;
        @XmlElement()
        protected int size;

        /**
         * Gets the value of the isolationLevel property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIsolationLevel() {
            return isolationLevel;
        }

        /**
         * Sets the value of the isolationLevel property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIsolationLevel(String value) {
            this.isolationLevel = value;
        }

        /**
         * Gets the value of the size property.
         * 
         */
        public int getSize() {
            return size;
        }

        /**
         * Sets the value of the size property.
         * 
         */
        public void setSize(int value) {
            this.size = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="cmp-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="cmp-field-class" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="table-column" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="sql-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="type-converter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cmpFieldName",
        "cmpFieldClass",
        "tableColumn",
        "sqlType",
        "typeConverter"
    })
    public static class CmpFieldMapping {

        @XmlElement(name = "cmp-field-name", required = true)
        protected String cmpFieldName;
        @XmlElement(name = "cmp-field-class")
        protected String cmpFieldClass;
        @XmlElement(name = "table-column", required = true)
        protected String tableColumn;
        @XmlElement(name = "sql-type")
        protected String sqlType;
        @XmlElement(name = "type-converter")
        protected String typeConverter;

        /**
         * Gets the value of the cmpFieldName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCmpFieldName() {
            return cmpFieldName;
        }

        /**
         * Sets the value of the cmpFieldName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCmpFieldName(String value) {
            this.cmpFieldName = value;
        }

        /**
         * Gets the value of the cmpFieldClass property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCmpFieldClass() {
            return cmpFieldClass;
        }

        /**
         * Sets the value of the cmpFieldClass property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCmpFieldClass(String value) {
            this.cmpFieldClass = value;
        }

        /**
         * Gets the value of the tableColumn property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTableColumn() {
            return tableColumn;
        }

        /**
         * Sets the value of the tableColumn property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTableColumn(String value) {
            this.tableColumn = value;
        }

        /**
         * Gets the value of the sqlType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSqlType() {
            return sqlType;
        }

        /**
         * Sets the value of the sqlType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSqlType(String value) {
            this.sqlType = value;
        }

        /**
         * Gets the value of the typeConverter property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTypeConverter() {
            return typeConverter;
        }

        /**
         * Sets the value of the typeConverter property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTypeConverter(String value) {
            this.typeConverter = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="group" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}groupType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="entity-group-mapping" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}entity-group-mappingType" minOccurs="0"/>
     *         &lt;element name="cmp-field-group-mapping" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}cmp-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="cmr-field-group-mapping" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}cmr-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "group",
        "entityGroupMapping",
        "cmpFieldGroupMapping",
        "cmrFieldGroupMapping"
    })
    public static class PrefetchGroup {

        @XmlElement()
        protected List<GroupType> group;
        @XmlElement(name = "entity-group-mapping")
        protected EntityGroupMappingType entityGroupMapping;
        @XmlElement(name = "cmp-field-group-mapping")
        protected List<CmpFieldGroupMappingType> cmpFieldGroupMapping;
        @XmlElement(name = "cmr-field-group-mapping")
        protected List<CmrFieldGroupMappingType> cmrFieldGroupMapping;

        /**
         * Gets the value of the group property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the group property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getGroup().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GroupType }
         * 
         * 
         */
        public List<GroupType> getGroup() {
            if (group == null) {
                group = new ArrayList<GroupType>();
            }
            return this.group;
        }

        /**
         * Gets the value of the entityGroupMapping property.
         * 
         * @return
         *     possible object is
         *     {@link EntityGroupMappingType }
         *     
         */
        public EntityGroupMappingType getEntityGroupMapping() {
            return entityGroupMapping;
        }

        /**
         * Sets the value of the entityGroupMapping property.
         * 
         * @param value
         *     allowed object is
         *     {@link EntityGroupMappingType }
         *     
         */
        public void setEntityGroupMapping(EntityGroupMappingType value) {
            this.entityGroupMapping = value;
        }

        /**
         * Gets the value of the cmpFieldGroupMapping property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cmpFieldGroupMapping property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCmpFieldGroupMapping().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CmpFieldGroupMappingType }
         * 
         * 
         */
        public List<CmpFieldGroupMappingType> getCmpFieldGroupMapping() {
            if (cmpFieldGroupMapping == null) {
                cmpFieldGroupMapping = new ArrayList<CmpFieldGroupMappingType>();
            }
            return this.cmpFieldGroupMapping;
        }

        /**
         * Gets the value of the cmrFieldGroupMapping property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cmrFieldGroupMapping property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCmrFieldGroupMapping().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CmrFieldGroupMappingType }
         * 
         * 
         */
        public List<CmrFieldGroupMappingType> getCmrFieldGroupMapping() {
            if (cmrFieldGroupMapping == null) {
                cmrFieldGroupMapping = new ArrayList<CmrFieldGroupMappingType>();
            }
            return this.cmrFieldGroupMapping;
        }

    }

}
