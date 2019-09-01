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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * javaee6.xsd
 *
 * <p>Java class for persistence-context-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-context-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="persistence-context-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="persistence-unit-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="persistence-context-type" type="{http://java.sun.com/xml/ns/javaee}persistence-context-typeType" minOccurs="0"/&gt;
 *         &lt;element name="persistence-property" type="{http://java.sun.com/xml/ns/javaee}propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceBaseGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence-context-refType", propOrder = {
    "descriptions",
    "persistenceContextRefName",
    "persistenceUnitName",
    "persistenceContextType",
    "persistenceContextSynchronization",
    "persistenceProperty",
    "mappedName",
    "injectionTarget",
    //TODO lookupName not in schema ??
    "lookupName"
})
public class PersistenceContextRef implements JndiReference, PersistenceRef {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "persistence-context-ref-name", required = true)
    protected String persistenceContextRefName;
    @XmlElement(name = "persistence-unit-name")
    protected String persistenceUnitName;
    @XmlElement(name = "persistence-context-type")
    protected PersistenceContextType persistenceContextType;
    @XmlElement(name = "persistence-context-synchronization")
    protected PersistenceContextSynchronization persistenceContextSynchronization;
    @XmlElement(name = "persistence-property", required = true)
    protected List<Property> persistenceProperty;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    //TODO lookupName not in schema ??
    @XmlElement(name = "lookup-name")
    protected String lookupName;
    @XmlElement(name = "injection-target", required = true)
    protected Set<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public PersistenceContextRef() {
    }

    public PersistenceContextRef(final String persistenceContextRefName, final String persistenceUnitName) {
        this.persistenceContextRefName = persistenceContextRefName;
        this.persistenceUnitName = persistenceUnitName;
    }

    public PersistenceContextRef(final String persistenceContextRefName, final String persistenceUnitName, final PersistenceContextType persistenceContextType, final List<Property> persistenceProperty) {
        this.persistenceContextRefName = persistenceContextRefName;
        this.persistenceUnitName = persistenceUnitName;
        this.persistenceContextType = persistenceContextType;
        this.persistenceProperty = persistenceProperty;
    }

    public PersistenceContextRef name(final String persistenceContextRefName) {
        this.persistenceContextRefName = persistenceContextRefName;
        return this;
    }

    public PersistenceContextRef unit(final String persistenceUnit) {
        this.persistenceUnitName = persistenceUnit;
        return this;
    }

    public PersistenceContextRef type(final PersistenceContextType persistenceContextType) {
        this.persistenceContextType = persistenceContextType;
        return this;
    }

    public PersistenceContextRef mappedName(final String mappedName) {
        this.mappedName = mappedName;
        return this;
    }

    public PersistenceContextRef lookup(final String lookupName) {
        this.lookupName = lookupName;
        return this;
    }

    public PersistenceContextRef injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        // TODO move this to getKey()
        if (this.persistenceContextRefName == null) {
            this.persistenceContextRefName = "java:comp/env/" + className + "/" + property;
        }

        return this;
    }

    public PersistenceContextRef injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    public PersistenceContextRef property(final String name, final String value) {
        getPersistenceProperty().add(new Property(name, value));
        return this;
    }

    public String getName() {
        return getPersistenceContextRefName();
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    public String getType() {
        return getPersistenceContextType().name();
    }

    public void setName(final String name) {
        setPersistenceContextRefName(name);
    }

    public void setType(final String type) {
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getPersistenceContextRefName() {
        return persistenceContextRefName;
    }

    public void setPersistenceContextRefName(final String value) {
        this.persistenceContextRefName = value;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(final String value) {
        this.persistenceUnitName = value;
    }

    public PersistenceContextType getPersistenceContextType() {
        return persistenceContextType;
    }

    public void setPersistenceContextType(final PersistenceContextType value) {
        this.persistenceContextType = value;
    }

    public PersistenceContextSynchronization getPersistenceContextSynchronization() {
        return persistenceContextSynchronization;
    }

    public void setPersistenceContextSynchronization(PersistenceContextSynchronization persistenceContextSynchronization) {
        this.persistenceContextSynchronization = persistenceContextSynchronization;
    }

    public List<Property> getPersistenceProperty() {
        if (persistenceProperty == null) {
            persistenceProperty = new ArrayList<Property>();
        }
        return this.persistenceProperty;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(final String value) {
        this.mappedName = value;
    }

    public String getLookupName() {
        return lookupName;
    }

    public void setLookupName(final String lookupName) {
        this.lookupName = lookupName;
    }

    public Set<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new HashSet<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    public void setPersistenceProperty(final List<Property> persistenceProperty) {
        this.persistenceProperty = persistenceProperty;
    }

    @Override
    public String toString() {
        return "PersistenceContextRef{" +
            "name='" + persistenceContextRefName + '\'' +
            ", unit='" + persistenceUnitName + '\'' +
            ", context=" + persistenceContextType +
            ", mappedName='" + mappedName + '\'' +
            ", lookupName='" + lookupName + '\'' +
            '}';
    }
}
