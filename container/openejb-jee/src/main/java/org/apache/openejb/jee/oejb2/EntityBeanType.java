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

import org.apache.openejb.jee.oejb3.PropertiesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
 * &lt;complexType name="entity-beanType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="local-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;group ref="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}tssGroup" minOccurs="0"/&gt;
 *         &lt;sequence minOccurs="0"&gt;
 *           &lt;element name="table-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;element name="static-sql" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType" minOccurs="0"/&gt;
 *           &lt;element name="cmp-field-mapping" maxOccurs="unbounded"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence&gt;
 *                     &lt;element name="cmp-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                     &lt;element name="cmp-field-class" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                     &lt;element name="table-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                     &lt;element name="sql-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                     &lt;element name="type-converter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;/sequence&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="primkey-field" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *           &lt;element ref="{http://tomee.apache.org/xml/ns/pkgen-2.1}key-generator" minOccurs="0"/&gt;
 *           &lt;element name="prefetch-group" minOccurs="0"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence&gt;
 *                     &lt;element name="group" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}groupType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                     &lt;element name="entity-group-mapping" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}entity-group-mappingType" minOccurs="0"/&gt;
 *                     &lt;element name="cmp-field-group-mapping" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}cmp-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                     &lt;element name="cmr-field-group-mapping" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}cmr-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;/sequence&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="select-for-update" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType" minOccurs="0"/&gt;
 *         &lt;/sequence&gt;
 *         &lt;element name="cache" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="isolation-level"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="read-uncommitted"/&gt;
 *                         &lt;enumeration value="read-committed"/&gt;
 *                         &lt;enumeration value="repeatable-read"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;group ref="{http://geronimo.apache.org/xml/ns/naming-1.2}jndiEnvironmentRefsGroup"/&gt;
 *         &lt;element name="query" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}queryType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity-beanType", propOrder = {
    "ejbName",
    "jndiName",
    "localJndiName",
    "jndi",
    "properties",
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
    "persistenceContextRef",
    "persistenceUnitRef",
    "ejbRef",
    "ejbLocalRef",
    "serviceRef",
    "resourceRef",
    "resourceEnvRef",
    "query"
})
public class EntityBeanType implements EnterpriseBean, RpcBean {

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;

    @XmlElement(name = "jndi-name")
    protected List<String> jndiName;

    @XmlElement(name = "local-jndi-name")
    protected List<String> localJndiName;

    @XmlElement(name = "jndi")
    protected List<Jndi> jndi;

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

    @XmlElement(name = "key-generator", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1")
    protected KeyGeneratorType keyGenerator;

    @XmlElement(name = "prefetch-group")
    protected EntityBeanType.PrefetchGroup prefetchGroup;

    @XmlElement(name = "select-for-update")
    protected EmptyType selectForUpdate;

    @XmlElement()
    protected EntityBeanType.Cache cache;

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

    @XmlElement()
    protected List<QueryType> query;

    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;


    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the ejbName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbName(final String value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the jndiName property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jndiName property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJndiName().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
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
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localJndiName property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalJndiName().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getLocalJndiName() {
        if (localJndiName == null) {
            localJndiName = new ArrayList<String>();
        }
        return this.localJndiName;
    }

    public List<Jndi> getJndi() {
        if (jndi == null) {
            jndi = new ArrayList<Jndi>();
        }
        return this.jndi;
    }

    /**
     * Gets the value of the tssLink property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTssLink() {
        return tssLink;
    }

    /**
     * Sets the value of the tssLink property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTssLink(final String value) {
        this.tssLink = value;
    }

    /**
     * Gets the value of the tss property.
     *
     * @return possible object is
     * {@link PatternType }
     */
    public PatternType getTss() {
        return tss;
    }

    /**
     * Sets the value of the tss property.
     *
     * @param value allowed object is
     *              {@link PatternType }
     */
    public void setTss(final PatternType value) {
        this.tss = value;
    }

    /**
     * Gets the value of the tableName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTableName(final String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the staticSql property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isStaticSql() {
        return staticSql != null;
    }

    /**
     * Sets the value of the staticSql property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setStaticSql(final boolean value) {
        this.staticSql = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the cmpFieldMapping property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmpFieldMapping property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmpFieldMapping().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EntityBeanType.CmpFieldMapping }
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
     * @return possible object is
     * {@link String }
     */
    public String getPrimkeyField() {
        return primkeyField;
    }

    /**
     * Sets the value of the primkeyField property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPrimkeyField(final String value) {
        this.primkeyField = value;
    }

    /**
     * Gets the value of the keyGenerator property.
     *
     * @return possible object is
     * {@link KeyGeneratorType }
     */
    public KeyGeneratorType getKeyGenerator() {
        return keyGenerator;
    }

    /**
     * Sets the value of the keyGenerator property.
     *
     * @param value allowed object is
     *              {@link KeyGeneratorType }
     */
    public void setKeyGenerator(final KeyGeneratorType value) {
        this.keyGenerator = value;
    }

    /**
     * Gets the value of the prefetchGroup property.
     *
     * @return possible object is
     * {@link EntityBeanType.PrefetchGroup }
     */
    public EntityBeanType.PrefetchGroup getPrefetchGroup() {
        return prefetchGroup;
    }

    /**
     * Sets the value of the prefetchGroup property.
     *
     * @param value allowed object is
     *              {@link EntityBeanType.PrefetchGroup }
     */
    public void setPrefetchGroup(final EntityBeanType.PrefetchGroup value) {
        this.prefetchGroup = value;
    }

    /**
     * Gets the value of the selectForUpdate property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isSelectForUpdate() {
        return selectForUpdate != null;
    }

    /**
     * Sets the value of the selectForUpdate property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setSelectForUpdate(final boolean value) {
        this.selectForUpdate = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the cache property.
     *
     * @return possible object is
     * {@link EntityBeanType.Cache }
     */
    public EntityBeanType.Cache getCache() {
        return cache;
    }

    /**
     * Sets the value of the cache property.
     *
     * @param value allowed object is
     *              {@link EntityBeanType.Cache }
     */
    public void setCache(final EntityBeanType.Cache value) {
        this.cache = value;
    }

    /**
     * Gets the value of the abstractNamingEntry property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractNamingEntry property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractNamingEntry().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AbstractNamingEntryType }{@code >}
     * {@link JAXBElement }{@code <}{@link PersistenceContextRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link PersistenceUnitRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link GbeanRefType }{@code >}
     */
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

    /**
     * Gets the value of the ejbRef property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRef property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRef().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRefType }
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
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbLocalRef property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbLocalRef().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EjbLocalRefType }
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
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceRef property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceRef().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRefType }
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
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceRef().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRefType }
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
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvRef property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceEnvRef().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvRefType }
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
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the query property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuery().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link QueryType }
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
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }


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
     *         &lt;element name="isolation-level"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="read-uncommitted"/&gt;
     *               &lt;enumeration value="read-committed"/&gt;
     *               &lt;enumeration value="repeatable-read"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
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
         * @return possible object is
         * {@link String }
         */
        public String getIsolationLevel() {
            return isolationLevel;
        }

        /**
         * Sets the value of the isolationLevel property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIsolationLevel(final String value) {
            this.isolationLevel = value;
        }

        /**
         * Gets the value of the size property.
         */
        public int getSize() {
            return size;
        }

        /**
         * Sets the value of the size property.
         */
        public void setSize(final int value) {
            this.size = value;
        }

    }


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
     *         &lt;element name="cmp-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="cmp-field-class" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="table-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="sql-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="type-converter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
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
         * @return possible object is
         * {@link String }
         */
        public String getCmpFieldName() {
            return cmpFieldName;
        }

        /**
         * Sets the value of the cmpFieldName property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCmpFieldName(final String value) {
            this.cmpFieldName = value;
        }

        /**
         * Gets the value of the cmpFieldClass property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCmpFieldClass() {
            return cmpFieldClass;
        }

        /**
         * Sets the value of the cmpFieldClass property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCmpFieldClass(final String value) {
            this.cmpFieldClass = value;
        }

        /**
         * Gets the value of the tableColumn property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTableColumn() {
            return tableColumn;
        }

        /**
         * Sets the value of the tableColumn property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTableColumn(final String value) {
            this.tableColumn = value;
        }

        /**
         * Gets the value of the sqlType property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getSqlType() {
            return sqlType;
        }

        /**
         * Sets the value of the sqlType property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setSqlType(final String value) {
            this.sqlType = value;
        }

        /**
         * Gets the value of the typeConverter property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTypeConverter() {
            return typeConverter;
        }

        /**
         * Sets the value of the typeConverter property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTypeConverter(final String value) {
            this.typeConverter = value;
        }

    }


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
     *         &lt;element name="group" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}groupType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="entity-group-mapping" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}entity-group-mappingType" minOccurs="0"/&gt;
     *         &lt;element name="cmp-field-group-mapping" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}cmp-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="cmr-field-group-mapping" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}cmr-field-group-mappingType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
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
         *
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the group property.
         *
         *
         * For example, to add a new item, do as follows:
         * <pre>
         *    getGroup().add(newItem);
         * </pre>
         *
         *
         *
         * Objects of the following type(s) are allowed in the list
         * {@link GroupType }
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
         * @return possible object is
         * {@link EntityGroupMappingType }
         */
        public EntityGroupMappingType getEntityGroupMapping() {
            return entityGroupMapping;
        }

        /**
         * Sets the value of the entityGroupMapping property.
         *
         * @param value allowed object is
         *              {@link EntityGroupMappingType }
         */
        public void setEntityGroupMapping(final EntityGroupMappingType value) {
            this.entityGroupMapping = value;
        }

        /**
         * Gets the value of the cmpFieldGroupMapping property.
         *
         *
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cmpFieldGroupMapping property.
         *
         *
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCmpFieldGroupMapping().add(newItem);
         * </pre>
         *
         *
         *
         * Objects of the following type(s) are allowed in the list
         * {@link CmpFieldGroupMappingType }
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
         *
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cmrFieldGroupMapping property.
         *
         *
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCmrFieldGroupMapping().add(newItem);
         * </pre>
         *
         *
         *
         * Objects of the following type(s) are allowed in the list
         * {@link CmrFieldGroupMappingType }
         */
        public List<CmrFieldGroupMappingType> getCmrFieldGroupMapping() {
            if (cmrFieldGroupMapping == null) {
                cmrFieldGroupMapping = new ArrayList<CmrFieldGroupMappingType>();
            }
            return this.cmrFieldGroupMapping;
        }

    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

}
