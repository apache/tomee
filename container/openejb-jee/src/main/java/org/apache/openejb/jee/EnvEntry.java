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
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashSet;
import java.util.Set;

/**
 * javaee6.xsd
 *
 * <p>Java class for env-entryType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="env-entryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="env-entry-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="env-entry-type" type="{http://java.sun.com/xml/ns/javaee}env-entry-type-valuesType" minOccurs="0"/&gt;
 *         &lt;element name="env-entry-value" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "env-entryType", propOrder = {
    "descriptions",
    "envEntryName",
    "envEntryType",
    "envEntryValue",
    "mappedName",
    "injectionTarget",
    "lookupName"
})
public class EnvEntry implements JndiReference {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "env-entry-name", required = true)
    protected String envEntryName;
    @XmlElement(name = "env-entry-type")
    protected String envEntryType;

    @XmlJavaTypeAdapter(StringAdapter.class)
    @XmlElement(name = "env-entry-value")
    protected String envEntryValue;

    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "lookup-name")
    protected String lookupName;
    @XmlElement(name = "injection-target", required = true)
    protected Set<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public EnvEntry() {
    }

    public EnvEntry(final String envEntryName, final String envEntryType, final String envEntryValue) {
        this.setEnvEntryName(envEntryName);
        this.setEnvEntryType(envEntryType);
        this.setEnvEntryValue(envEntryValue);
    }

    public EnvEntry(final String envEntryName, final Class<?> envEntryType, final String envEntryValue) {
        this(envEntryName, envEntryType.getName(), envEntryValue);
    }

    public EnvEntry name(final String envEntryName) {
        this.setEnvEntryName(envEntryName);
        return this;
    }

    public EnvEntry type(final String envEntryType) {
        this.setEnvEntryType(envEntryType);
        return this;
    }

    public EnvEntry type(final Class<?> envEntryType) {
        return type(envEntryType.getName());
    }

    public EnvEntry value(final String envEntryValue) {
        this.setEnvEntryValue(envEntryValue);
        return this;
    }

    public EnvEntry mappedName(final String mappedName) {
        this.setMappedName(mappedName);
        return this;
    }

    public EnvEntry lookup(final String lookupName) {
        this.setLookupName(lookupName);
        return this;
    }

    public EnvEntry injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        if (this.getEnvEntryName() == null) {
            this.setEnvEntryName("java:comp/env/" + className + "/" + property);
        }

        return this;
    }

    public EnvEntry injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    @XmlTransient
    public String getName() {
        return getEnvEntryName();
    }

    @XmlTransient
    public String getType() {
        return getEnvEntryType();
    }

    public void setName(final String name) {
        setEnvEntryName(name);
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    public void setType(final String type) {
        setEnvEntryType(type);
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

    public String getEnvEntryName() {
        return envEntryName;
    }

    public void setEnvEntryName(final String value) {
        this.envEntryName = value;
    }

    /**
     * Gets the value of the envEntryType property.
     */
    public String getEnvEntryType() {
        return envEntryType;
    }

    public void setEnvEntryType(final String value) {
        this.envEntryType = value;
    }

    public String getEnvEntryValue() {
        return envEntryValue;
    }

    public void setEnvEntryValue(final String value) {
        this.envEntryValue = value;
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
        return "EnvEntry{" +
            "name='" + getEnvEntryName() + '\'' +
            ", type='" + getEnvEntryType() + '\'' +
            ", value='" + getEnvEntryValue() + '\'' +
            ", mappedName='" + getMappedName() + '\'' +
            ", lookupName='" + getLookupName() + '\'' +
            '}';
    }
}
