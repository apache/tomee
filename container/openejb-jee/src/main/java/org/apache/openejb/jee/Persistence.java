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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * persistence_2_0.xsd
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="persistence-unit" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="provider" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="jta-data-source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="non-jta-data-source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="mapping-file" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="jar-file" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="class" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="exclude-unlisted-classes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="shared-cache-mode" type="{http://java.sun.com/xml/ns/persistence}persistence-unit-caching-type" minOccurs="0"/>
 *                   &lt;element name="validation-mode" type="{http://java.sun.com/xml/ns/persistence}persistence-unit-validation-mode-type" minOccurs="0"/>
 *                   &lt;element name="properties" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="property" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="transaction-type" type="{http://java.sun.com/xml/ns/persistence}persistence-unit-transaction-type" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://java.sun.com/xml/ns/persistence}versionType" fixed="2.0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "persistenceUnit"
})
@XmlRootElement(name = "persistence", namespace = "http://java.sun.com/xml/ns/persistence")
public class Persistence {

    @XmlElement(name = "persistence-unit", namespace = "http://java.sun.com/xml/ns/persistence", required = true)
    protected List<Persistence.PersistenceUnit> persistenceUnit;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String version;

    /**
     * Gets the value of the persistenceUnit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the persistenceUnit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersistenceUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Persistence.PersistenceUnit }
     * 
     * 
     */
    public List<Persistence.PersistenceUnit> getPersistenceUnit() {
        if (persistenceUnit == null) {
            persistenceUnit = new ArrayList<Persistence.PersistenceUnit>();
        }
        return this.persistenceUnit;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getVersion() {
        if (version == null) {
            return "2.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setVersion(java.lang.String value) {
        this.version = value;
    }


    /**
     * 
     *                                 Configuration of a persistence unit.
     *                             
     * 
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="provider" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="jta-data-source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="non-jta-data-source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="mapping-file" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="jar-file" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="class" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="exclude-unlisted-classes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="shared-cache-mode" type="{http://java.sun.com/xml/ns/persistence}persistence-unit-caching-type" minOccurs="0"/>
     *         &lt;element name="validation-mode" type="{http://java.sun.com/xml/ns/persistence}persistence-unit-validation-mode-type" minOccurs="0"/>
     *         &lt;element name="properties" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="property" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                           &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="transaction-type" type="{http://java.sun.com/xml/ns/persistence}persistence-unit-transaction-type" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "description",
        "provider",
        "jtaDataSource",
        "nonJtaDataSource",
        "mappingFile",
        "jarFile",
        "clazz",
        "excludeUnlistedClasses",
        "sharedCacheMode",
        "validationMode",
        "properties"
    })
    public static class PersistenceUnit {

        @XmlElement(namespace = "http://java.sun.com/xml/ns/persistence")
        protected java.lang.String description;
        @XmlElement(namespace = "http://java.sun.com/xml/ns/persistence")
        protected java.lang.String provider;
        @XmlElement(name = "jta-data-source", namespace = "http://java.sun.com/xml/ns/persistence")
        protected java.lang.String jtaDataSource;
        @XmlElement(name = "non-jta-data-source", namespace = "http://java.sun.com/xml/ns/persistence")
        protected java.lang.String nonJtaDataSource;
        @XmlElement(name = "mapping-file", namespace = "http://java.sun.com/xml/ns/persistence")
        protected List<java.lang.String> mappingFile;
        @XmlElement(name = "jar-file", namespace = "http://java.sun.com/xml/ns/persistence")
        protected List<java.lang.String> jarFile;
        @XmlElement(name = "class", namespace = "http://java.sun.com/xml/ns/persistence")
        protected List<java.lang.String> clazz;
        @XmlElement(name = "exclude-unlisted-classes", namespace = "http://java.sun.com/xml/ns/persistence", defaultValue = "true")
        protected Boolean excludeUnlistedClasses;
        @XmlElement(name = "shared-cache-mode", namespace = "http://java.sun.com/xml/ns/persistence")
        protected PersistenceUnitCaching sharedCacheMode;
        @XmlElement(name = "validation-mode", namespace = "http://java.sun.com/xml/ns/persistence")
        protected PersistenceUnitValidationMode validationMode;
        @XmlElement(namespace = "http://java.sun.com/xml/ns/persistence")
        protected Persistence.PersistenceUnit.Properties properties;
        @XmlAttribute(required = true)
        protected java.lang.String name;
        @XmlAttribute(name = "transaction-type")
        protected TrackingMode transactionType;

        /**
         * Gets the value of the description property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getDescription() {
            return description;
        }

        /**
         * Sets the value of the description property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setDescription(java.lang.String value) {
            this.description = value;
        }

        /**
         * Gets the value of the provider property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getProvider() {
            return provider;
        }

        /**
         * Sets the value of the provider property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setProvider(java.lang.String value) {
            this.provider = value;
        }

        /**
         * Gets the value of the jtaDataSource property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getJtaDataSource() {
            return jtaDataSource;
        }

        /**
         * Sets the value of the jtaDataSource property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setJtaDataSource(java.lang.String value) {
            this.jtaDataSource = value;
        }

        /**
         * Gets the value of the nonJtaDataSource property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getNonJtaDataSource() {
            return nonJtaDataSource;
        }

        /**
         * Sets the value of the nonJtaDataSource property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setNonJtaDataSource(java.lang.String value) {
            this.nonJtaDataSource = value;
        }

        /**
         * Gets the value of the mappingFile property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the mappingFile property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMappingFile().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link java.lang.String }
         * 
         * 
         */
        public List<java.lang.String> getMappingFile() {
            if (mappingFile == null) {
                mappingFile = new ArrayList<java.lang.String>();
            }
            return this.mappingFile;
        }

        /**
         * Gets the value of the jarFile property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the jarFile property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getJarFile().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link java.lang.String }
         * 
         * 
         */
        public List<java.lang.String> getJarFile() {
            if (jarFile == null) {
                jarFile = new ArrayList<java.lang.String>();
            }
            return this.jarFile;
        }

        /**
         * Gets the value of the clazz property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the clazz property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getClazz().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link java.lang.String }
         * 
         * 
         */
        public List<java.lang.String> getClazz() {
            if (clazz == null) {
                clazz = new ArrayList<java.lang.String>();
            }
            return this.clazz;
        }

        /**
         * Gets the value of the excludeUnlistedClasses property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isExcludeUnlistedClasses() {
            return excludeUnlistedClasses;
        }

        /**
         * Sets the value of the excludeUnlistedClasses property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setExcludeUnlistedClasses(Boolean value) {
            this.excludeUnlistedClasses = value;
        }

        /**
         * Gets the value of the sharedCacheMode property.
         * 
         * @return
         *     possible object is
         *     {@link PersistenceUnitCaching }
         *     
         */
        public PersistenceUnitCaching getSharedCacheMode() {
            return sharedCacheMode;
        }

        /**
         * Sets the value of the sharedCacheMode property.
         * 
         * @param value
         *     allowed object is
         *     {@link PersistenceUnitCaching }
         *     
         */
        public void setSharedCacheMode(PersistenceUnitCaching value) {
            this.sharedCacheMode = value;
        }

        /**
         * Gets the value of the validationMode property.
         * 
         * @return
         *     possible object is
         *     {@link PersistenceUnitValidationMode }
         *     
         */
        public PersistenceUnitValidationMode getValidationMode() {
            return validationMode;
        }

        /**
         * Sets the value of the validationMode property.
         * 
         * @param value
         *     allowed object is
         *     {@link PersistenceUnitValidationMode }
         *     
         */
        public void setValidationMode(PersistenceUnitValidationMode value) {
            this.validationMode = value;
        }

        /**
         * Gets the value of the properties property.
         * 
         * @return
         *     possible object is
         *     {@link Persistence.PersistenceUnit.Properties }
         *     
         */
        public Persistence.PersistenceUnit.Properties getProperties() {
            return properties;
        }

        /**
         * Sets the value of the properties property.
         * 
         * @param value
         *     allowed object is
         *     {@link Persistence.PersistenceUnit.Properties }
         *     
         */
        public void setProperties(Persistence.PersistenceUnit.Properties value) {
            this.properties = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setName(java.lang.String value) {
            this.name = value;
        }

        /**
         * Gets the value of the transactionType property.
         * 
         * @return
         *     possible object is
         *     {@link TrackingMode }
         *     
         */
        public TrackingMode getTransactionType() {
            return transactionType;
        }

        /**
         * Sets the value of the transactionType property.
         * 
         * @param value
         *     allowed object is
         *     {@link TrackingMode }
         *     
         */
        public void setTransactionType(TrackingMode value) {
            this.transactionType = value;
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
         *         &lt;element name="property" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *                 &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
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
            "property"
        })
        public static class Properties {

            @XmlElement(namespace = "http://java.sun.com/xml/ns/persistence")
            protected List<Persistence.PersistenceUnit.Properties.Property> property;

            /**
             * Gets the value of the property property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the property property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getProperty().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Persistence.PersistenceUnit.Properties.Property }
             * 
             * 
             */
            public List<Persistence.PersistenceUnit.Properties.Property> getProperty() {
                if (property == null) {
                    property = new ArrayList<Persistence.PersistenceUnit.Properties.Property>();
                }
                return this.property;
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
             *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
             *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Property {

                @XmlAttribute(required = true)
                protected java.lang.String name;
                @XmlAttribute(required = true)
                protected java.lang.String value;

                /**
                 * Gets the value of the name property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link java.lang.String }
                 *     
                 */
                public java.lang.String getName() {
                    return name;
                }

                /**
                 * Sets the value of the name property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link java.lang.String }
                 *     
                 */
                public void setName(java.lang.String value) {
                    this.name = value;
                }

                /**
                 * Gets the value of the value property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link java.lang.String }
                 *     
                 */
                public java.lang.String getValue() {
                    return value;
                }

                /**
                 * Sets the value of the value property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link java.lang.String }
                 *     
                 */
                public void setValue(java.lang.String value) {
                    this.value = value;
                }

            }

        }

    }

}
