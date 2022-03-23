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
 * <p>Java class for resource-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="res-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="res-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/&gt;
 *         &lt;element name="res-auth" type="{http://java.sun.com/xml/ns/javaee}res-authType" minOccurs="0"/&gt;
 *         &lt;element name="res-sharing-scope" type="{http://java.sun.com/xml/ns/javaee}res-sharing-scopeType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-refType", propOrder = {
    "descriptions",
    "resRefName",
    "resType",
    "resAuth",
    "resSharingScope",
    "mappedName",
    "injectionTarget",
    "lookupName"
})
public class ResourceRef implements JndiReference {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "res-ref-name", required = true)
    protected String resRefName;
    @XmlElement(name = "res-type")
    protected String resType;
    @XmlElement(name = "res-auth")
    protected ResAuth resAuth;
    @XmlElement(name = "res-sharing-scope")
    protected ResSharingScope resSharingScope = ResSharingScope.SHAREABLE;
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
    @XmlTransient
    protected String origin;

    public ResourceRef() {
    }

    public ResourceRef(final String resRefName, final String resType) {
        this.resRefName = resRefName;
        this.resType = resType;
    }

    public ResourceRef(final String resRefName, final String resType, final ResAuth resAuth, final ResSharingScope resSharingScope) {
        this.resRefName = resRefName;
        this.resType = resType;
        this.resAuth = resAuth;
        this.resSharingScope = resSharingScope;
    }

//  pbpaste | grep protected | perl -pe 's/.*protected ([^ ]+) ([^ ]+);/public ResourceRef $2($1 $2) { this.$2 = $2; return this; }/'

    public ResourceRef name(final String resRefName) {
        this.resRefName = resRefName;
        return this;
    }

    public ResourceRef type(final String resType) {
        this.resType = resType;
        return this;
    }

    public ResourceRef auth(final ResAuth resAuth) {
        this.resAuth = resAuth;
        return this;
    }

    public ResourceRef mappedName(final String mappedName) {
        this.mappedName = mappedName;
        return this;
    }

    public ResourceRef lookup(final String lookupName) {
        this.lookupName = lookupName;
        return this;
    }

    public ResourceRef injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        if (this.resRefName == null) {
            this.resRefName = "java:comp/env/" + className + "/" + property;
        }

        return this;
    }

    public ResourceRef injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    @XmlTransient
    public String getName() {
        return getResRefName();
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    @XmlTransient
    public String getType() {
        return getResType();
    }

    public void setName(final String name) {
        setResRefName(name);
    }

    public void setType(final String type) {
        setResType(type);
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

    public String getResRefName() {
        return resRefName;
    }

    public void setResRefName(final String value) {
        this.resRefName = value;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(final String value) {
        this.resType = value;
    }

    public ResAuth getResAuth() {
        return resAuth;
    }

    public void setResAuth(final ResAuth value) {
        this.resAuth = value;
    }

    public ResSharingScope getResSharingScope() {
        return resSharingScope;
    }

    public void setResSharingScope(final ResSharingScope value) {
        this.resSharingScope = value;
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

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(final String origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "ResourceRef{" +
            "name='" + resRefName + '\'' +
            ", type='" + resType + '\'' +
            ", mappedName='" + mappedName + '\'' +
            ", lookupName='" + lookupName + '\'' +
            '}';
    }
}
