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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * javaee6.xsd
 *
 * <p>Java class for resource-env-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-env-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="resource-env-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="resource-env-ref-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-env-refType", propOrder = {
    "descriptions",
    "resourceEnvRefName",
    "resourceEnvRefType",
    "mappedName",
    "injectionTarget",
    "lookupName"
})
public class ResourceEnvRef implements JndiReference {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "resource-env-ref-name", required = true)
    protected String resourceEnvRefName;
    @XmlElement(name = "resource-env-ref-type")
    protected String resourceEnvRefType;
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

    public ResourceEnvRef() {
    }

    public ResourceEnvRef(final String resourceEnvRefName, final String resourceEnvRefType) {
        this.resourceEnvRefName = resourceEnvRefName;
        this.resourceEnvRefType = resourceEnvRefType;
    }

    public ResourceEnvRef(final String resourceEnvRefName, final Class resourceEnvRefType) {
        this(resourceEnvRefName, resourceEnvRefType.getName());
    }

    public ResourceEnvRef name(final String resourceEnvRefName) {
        this.resourceEnvRefName = resourceEnvRefName;
        return this;
    }

    public ResourceEnvRef type(final String resourceEnvRefType) {
        this.resourceEnvRefType = resourceEnvRefType;
        return this;
    }

    public ResourceEnvRef type(final Class<?> resourceEnvRefType) {
        return type(resourceEnvRefType.getName());
    }

    public ResourceEnvRef mappedName(final String mappedName) {
        this.mappedName = mappedName;
        return this;
    }

    public ResourceEnvRef lookup(final String lookupName) {
        this.lookupName = lookupName;
        return this;
    }

    public ResourceEnvRef injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        if (this.resourceEnvRefName == null) {
            this.resourceEnvRefName = "java:comp/env/" + className + "/" + property;
        }

        return this;
    }

    public ResourceEnvRef injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    @XmlTransient
    public String getName() {
        return getResourceEnvRefName();
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    @XmlTransient
    public String getType() {
        return getResourceEnvRefType();
    }

    public void setName(final String name) {
        setResourceEnvRefName(name);
    }

    public void setType(final String type) {
        setResourceEnvRefType(type);
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

    public String getResourceEnvRefName() {
        return resourceEnvRefName;
    }

    public void setResourceEnvRefName(final String value) {
        this.resourceEnvRefName = value;
    }

    public String getResourceEnvRefType() {
        return resourceEnvRefType;
    }

    public void setResourceEnvRefType(final String value) {
        this.resourceEnvRefType = value;
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
        return "ResourceEnvRef{" +
            "name='" + resourceEnvRefName + '\'' +
            ", type='" + resourceEnvRefType + '\'' +
            ", mappedName='" + mappedName + '\'' +
            ", lookupName='" + lookupName + '\'' +
            '}';
    }
}
