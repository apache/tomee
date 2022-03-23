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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * javaee6.xsd
 *
 * <p>Java class for persistence-unit-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-unit-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="persistence-unit-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="persistence-unit-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceBaseGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence-unit-refType", propOrder = {
    "descriptions",
    "persistenceUnitRefName",
    "persistenceUnitName",
    "mappedName",
    "injectionTarget",
    //TODO lookupName not in schema ??
    "lookupName"
})
public class PersistenceUnitRef implements JndiReference, PersistenceRef {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "persistence-unit-ref-name", required = true)
    protected String persistenceUnitRefName;
    @XmlElement(name = "persistence-unit-name")
    protected String persistenceUnitName;
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


    public PersistenceUnitRef() {
    }

    public PersistenceUnitRef(final String persistenceUnitRefName, final String persistenceUnitName) {
        this.persistenceUnitRefName = persistenceUnitRefName;
        this.persistenceUnitName = persistenceUnitName;
    }

    public PersistenceUnitRef name(final String persistenceUnitRefName) {
        this.persistenceUnitRefName = persistenceUnitRefName;
        return this;
    }

    public PersistenceUnitRef unit(final String persistenceUnit) {
        this.persistenceUnitName = persistenceUnit;
        return this;
    }

    public PersistenceUnitRef mappedName(final String mappedName) {
        this.mappedName = mappedName;
        return this;
    }

    public PersistenceUnitRef lookup(final String lookupName) {
        this.lookupName = lookupName;
        return this;
    }

    public PersistenceUnitRef injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        if (this.persistenceUnitRefName == null) {
            this.persistenceUnitRefName = "java:comp/env/" + className + "/" + property;
        }

        return this;
    }

    public PersistenceUnitRef injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    public String getName() {
        return getPersistenceUnitRefName();
    }

    public String getType() {
        return getPersistenceUnitName();
    }

    public void setName(final String name) {
        setPersistenceUnitRefName(name);
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
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

    public String getPersistenceUnitRefName() {
        return persistenceUnitRefName;
    }

    public void setPersistenceUnitRefName(final String value) {
        this.persistenceUnitRefName = value;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(final String value) {
        this.persistenceUnitName = value;
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

    @Override
    public String toString() {
        return "PersistenceUnitRef{" +
            "name='" + persistenceUnitRefName + '\'' +
            ", unit='" + persistenceUnitName + '\'' +
            ", mappedName='" + mappedName + '\'' +
            ", lookupName='" + lookupName + '\'' +
            '}';
    }
}
