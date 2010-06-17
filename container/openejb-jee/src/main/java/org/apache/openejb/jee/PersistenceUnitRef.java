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
import java.util.List;


/**
 * javaee6.xsd
 *
 * <p>Java class for persistence-unit-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-unit-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="persistence-unit-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="persistence-unit-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceBaseGroup"/>
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
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;


    public PersistenceUnitRef() {
    }

    public PersistenceUnitRef(String persistenceUnitRefName, String persistenceUnitName) {
        this.persistenceUnitRefName = persistenceUnitRefName;
        this.persistenceUnitName = persistenceUnitName;
    }

    public String getName() {
        return getPersistenceUnitRefName();
    }

    public String getType() {
        return getPersistenceUnitName();
    }

    public void setName(String name) {
        setPersistenceUnitRefName(name);
    }

    public String getKey() {
        return getName();
    }

    public void setType(String type) {
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

    public String getPersistenceUnitRefName() {
        return persistenceUnitRefName;
    }

    public void setPersistenceUnitRefName(String value) {
        this.persistenceUnitRefName = value;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String value) {
        this.persistenceUnitName = value;
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
