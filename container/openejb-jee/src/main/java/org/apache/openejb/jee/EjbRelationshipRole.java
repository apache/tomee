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
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for ejb-relationship-roleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ejb-relationship-roleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-relationship-role-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;element name="multiplicity" type="{http://java.sun.com/xml/ns/javaee}multiplicityType"/>
 *         &lt;element name="cascade-delete" type="{http://java.sun.com/xml/ns/javaee}emptyType" minOccurs="0"/>
 *         &lt;element name="relationship-role-source" type="{http://java.sun.com/xml/ns/javaee}relationship-role-sourceType"/>
 *         &lt;element name="cmr-field" type="{http://java.sun.com/xml/ns/javaee}cmr-fieldType" minOccurs="0"/>
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
@XmlType(name = "ejb-relationship-roleType", propOrder = {
        "descriptions",
        "ejbRelationshipRoleName",
        "multiplicity",
        "cascadeDelete",
        "relationshipRoleSource",
        "cmrField"
        })
public class EjbRelationshipRole {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "ejb-relationship-role-name")
    protected String ejbRelationshipRoleName;
    @XmlElement(required = true)
    protected Multiplicity multiplicity;
    @XmlElement(name = "cascade-delete")
    protected Empty cascadeDelete;
    @XmlElement(name = "relationship-role-source", required = true)
    protected RelationshipRoleSource relationshipRoleSource;
    @XmlElement(name = "cmr-field")
    protected CmrField cmrField;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

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

    public String getEjbRelationshipRoleName() {
        return ejbRelationshipRoleName;
    }

    public void setEjbRelationshipRoleName(String value) {
        this.ejbRelationshipRoleName = value;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Multiplicity value) {
        this.multiplicity = value;
    }

    public boolean getCascadeDelete() {
        return cascadeDelete != null;
    }

    public void setCascadeDelete(boolean value) {
        this.cascadeDelete = value ? new Empty() : null;
    }

    public RelationshipRoleSource getRelationshipRoleSource() {
        return relationshipRoleSource;
    }

    public void setRelationshipRoleSource(RelationshipRoleSource value) {
        this.relationshipRoleSource = value;
    }

    public CmrField getCmrField() {
        return cmrField;
    }

    public void setCmrField(CmrField value) {
        this.cmrField = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
