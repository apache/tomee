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
package org.apache.openejb.jee.jpa.unit;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.Properties;

/**
 *
 *                 Configuration of a persistence unit.
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
public class PersistenceUnit {

    @XmlTransient
    private String id;

    protected String description;
    protected String provider;
    @XmlElement(name = "jta-data-source")
    protected String jtaDataSource;
    @XmlElement(name = "non-jta-data-source")
    protected String nonJtaDataSource;
    @XmlElement(name = "mapping-file", required = true)
    protected List<String> mappingFile;
    @XmlElement(name = "jar-file", required = true)
    protected List<String> jarFile;
    @XmlElement(name = "class", required = true)
    protected List<String> clazz;
    @XmlElement(name = "exclude-unlisted-classes", defaultValue = "false")
    protected Boolean excludeUnlistedClasses;
    @XmlElement(name = "shared-cache-mode")
    protected SharedCacheMode sharedCacheMode;
    @XmlElement(name = "validation-mode")
    protected ValidationMode validationMode;
    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected java.util.Properties properties;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "transaction-type")
    protected TransactionType transactionType;

    public PersistenceUnit(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }

    public PersistenceUnit() {
    }

    public PersistenceUnit(String unitName) {
        this.name = unitName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {
        this.provider = value;
    }

    public void setProvider(Class value) {
        setProvider(value == null ? null : value.getName());
    }

    public String getJtaDataSource() {
        return jtaDataSource;
    }

    public void setJtaDataSource(String value) {
        this.jtaDataSource = value;
    }

    public String getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(String value) {
        this.nonJtaDataSource = value;
    }

    public List<String> getMappingFile() {
        if (mappingFile == null) {
            mappingFile = new ArrayList<String>();
        }
        return this.mappingFile;
    }

    public List<String> getJarFile() {
        if (jarFile == null) {
            jarFile = new ArrayList<String>();
        }
        return this.jarFile;
    }

    public List<String> getClazz() {
        if (clazz == null) {
            clazz = new ArrayList<String>();
        }
        return this.clazz;
    }

    public boolean addClass(String s) {
        return getClazz().add(s);
    }

    public boolean addClass(Class clazz) {
        return addClass(clazz.getName());
    }

    public Boolean isExcludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(Boolean value) {
        this.excludeUnlistedClasses = value;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public Object setProperty(String key, String value) {
        return getProperties().setProperty(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public TransactionType getTransactionType() {
        // TODO: Is this the correct default?
        return (transactionType == null)? TransactionType.JTA: transactionType;
    }

    public void setTransactionType(TransactionType value) {
        this.transactionType = value;
    }

    public SharedCacheMode getSharedCacheMode() {
        return (sharedCacheMode == null) ? SharedCacheMode.UNSPECIFIED : sharedCacheMode;
    }

    public ValidationMode getValidationMode() {
        return (validationMode == null) ? ValidationMode.AUTO : validationMode;
    }

}
