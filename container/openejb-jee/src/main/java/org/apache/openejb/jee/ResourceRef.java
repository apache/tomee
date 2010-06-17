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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * javaee6.xsd
 *
 * <p>Java class for resource-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="res-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="res-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="res-auth" type="{http://java.sun.com/xml/ns/javaee}res-authType" minOccurs="0"/>
 *         &lt;element name="res-sharing-scope" type="{http://java.sun.com/xml/ns/javaee}res-sharing-scopeType" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
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
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public ResourceRef() {
    }

    public ResourceRef(String resRefName, String resType) {
        this.resRefName = resRefName;
        this.resType = resType;
    }

    public ResourceRef(String resRefName, String resType, ResAuth resAuth, ResSharingScope resSharingScope) {
        this.resRefName = resRefName;
        this.resType = resType;
        this.resAuth = resAuth;
        this.resSharingScope = resSharingScope;
    }

    @XmlTransient
    public String getName() {
        return getResRefName();
    }

    public String getKey() {
        return getName();
    }

    @XmlTransient
    public String getType() {
        return getResType();
    }

    public void setName(String name) {
        setResRefName(name);
    }

    public void setType(String type) {
        setResType(type);
    }


    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getResRefName() {
        return resRefName;
    }

    public void setResRefName(String value) {
        this.resRefName = value;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String value) {
        this.resType = value;
    }

    public ResAuth getResAuth() {
        return resAuth;
    }

    public void setResAuth(ResAuth value) {
        this.resAuth = value;
    }

    public ResSharingScope getResSharingScope() {
        return resSharingScope;
    }

    public void setResSharingScope(ResSharingScope value) {
        this.resSharingScope = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    public String getLookupName() {
        return lookupName;
    }

    public void setLookupName(String lookupName) {
        this.lookupName = lookupName;
    }

    public List<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
