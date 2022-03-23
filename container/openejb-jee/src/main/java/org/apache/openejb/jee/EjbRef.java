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
 * <p>Java class for ejb-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ejb-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ejb-ref-name" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-nameType"/&gt;
 *         &lt;element name="ejb-ref-type" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-typeType" minOccurs="0"/&gt;
 *         &lt;element name="home" type="{http://java.sun.com/xml/ns/javaee}homeType" minOccurs="0"/&gt;
 *         &lt;element name="remote" type="{http://java.sun.com/xml/ns/javaee}remoteType" minOccurs="0"/&gt;
 *         &lt;element name="ejb-link" type="{http://java.sun.com/xml/ns/javaee}ejb-linkType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-refType", propOrder = {
    "descriptions",
    "ejbRefName",
    "ejbRefType",
    "home",
    "remote",
    "ejbLink",
    "mappedName",
    "injectionTarget",
    "lookupName"
})
public class EjbRef implements EjbReference {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "ejb-ref-name", required = true)
    protected String ejbRefName;
    @XmlElement(name = "ejb-ref-type")
    protected EjbRefType ejbRefType;
    protected String home;
    protected String remote;
    @XmlElement(name = "ejb-link")
    protected String ejbLink;
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
    protected Type refType = Type.REMOTE;


    public EjbRef name(final String ejbRefName) {
        this.ejbRefName = ejbRefName;
        return this;
    }

    public EjbRef type(final EjbRefType ejbRefType) {
        this.ejbRefType = ejbRefType;
        return this;
    }

    public EjbRef link(final String link) {
        this.ejbLink = link;
        return this;
    }

    public EjbRef remote(final String remote) {
        this.remote = remote;
        return this;
    }

    public EjbRef remote(final Class<?> remote) {
        return remote(remote.getName());
    }

    public EjbRef home(final String home) {
        this.home = home;
        return this;
    }

    public EjbRef home(final Class<?> home) {
        return home(home.getName());
    }

    public EjbRef mappedName(final String mappedName) {
        this.mappedName = mappedName;
        return this;
    }

    public EjbRef lookup(final String lookupName) {
        this.lookupName = lookupName;
        return this;
    }

    public EjbRef injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        if (this.ejbRefName == null) {
            this.ejbRefName = "java:comp/env/" + className + "/" + property;
        }

        return this;
    }

    public EjbRef injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    public Type getRefType() {
        return refType;
    }

    public void setRefType(final Type refType) {
        this.refType = refType;
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

    public String getEjbRefName() {
        return ejbRefName;
    }

    public String getName() {
        return getEjbRefName();
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    public String getType() {
        return getEjbRefType().name();
    }

    public void setName(final String name) {
        setEjbRefName(name);
    }

    public void setType(final String type) {
    }

    public void setEjbRefName(final String value) {
        this.ejbRefName = value;
    }

    public EjbRefType getEjbRefType() {
        return ejbRefType;
    }

    public void setEjbRefType(final EjbRefType value) {
        this.ejbRefType = value;
    }

    public String getHome() {
        return home;
    }

    public void setHome(final String value) {
        this.home = value;
    }

    public String getRemote() {
        return remote;
    }

    public String getInterface() {
        return getRemote();
    }

    public void setRemote(final String value) {
        this.remote = value;
    }

    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbLink(final String value) {
        this.ejbLink = value;
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
        return "EjbRef{" +
            "name='" + ejbRefName + '\'' +
            ", remote='" + remote + '\'' +
            ", link='" + ejbLink + '\'' +
            ", mappedName='" + mappedName + '\'' +
            ", lookupName='" + lookupName + '\'' +
            '}';
    }
}
