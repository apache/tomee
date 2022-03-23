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

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.Properties;

/**
 * Configuration of a persistence unit.
 *
 *
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="provider" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="jta-data-source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="non-jta-data-source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="mapping-file" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="jar-file" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="class" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="exclude-unlisted-classes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="properties" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="property" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="transaction-type" type="{http://java.sun.com/xml/ns/persistence}persistence-unit-transaction-type" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
    @XmlTransient
    protected boolean scanned = false;

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(final boolean scanned) {
        this.scanned = scanned;
    }


    public PersistenceUnit(final String name, final String provider) {
        this.name = name;
        this.provider = provider;
    }

    public PersistenceUnit() {
    }

    public PersistenceUnit(final String unitName) {
        this.name = unitName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String value) {
        this.description = value;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(final String value) {
        this.provider = value;
    }

    public void setProvider(final Class value) {
        setProvider(value == null ? null : value.getName());
    }

    public String getJtaDataSource() {
        return jtaDataSource;
    }

    public void setJtaDataSource(final String value) {
        this.jtaDataSource = value;
    }

    public String getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(final String value) {
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

    public boolean addClass(final String s) {
        return getClazz().add(s);
    }

    public boolean addClass(final Class clazz) {
        return addClass(clazz.getName());
    }

    public Boolean isExcludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(final Boolean value) {
        this.excludeUnlistedClasses = value;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public String getProperty(final String key) {
        return getProperties().getProperty(key);
    }

    public String getProperty(final String key, final String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public Object setProperty(final String key, final String value) {
        return getProperties().setProperty(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public TransactionType getTransactionType() {
        // TODO: Is this the correct default?
        return (transactionType == null) ? TransactionType.JTA : transactionType;
    }

    public void setTransactionType(final TransactionType value) {
        this.transactionType = value;
    }

    public SharedCacheMode getSharedCacheMode() {
        return (sharedCacheMode == null) ? SharedCacheMode.UNSPECIFIED : sharedCacheMode;
    }

    public ValidationMode getValidationMode() {
        if (validationMode == null) {
            final String propConfig = getProperty("jakarta.persistence.validation.mode");
            if (propConfig != null) {
                try {
                    validationMode = ValidationMode.valueOf(propConfig.toUpperCase());
                } catch (final IllegalArgumentException iae) { // can happen since some provider allow more than the enum
                    // no-op
                }
            }
        }
        return (validationMode == null) ? ValidationMode.AUTO : validationMode;
    }

    public void setValidationMode(final ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public void setSharedCacheMode(final SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }
}
