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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-relationship-roleType", propOrder = {
        "description",
        "ejbRelationshipRoleName",
        "multiplicity",
        "cascadeDelete",
        "relationshipRoleSource",
        "cmrField"
        })
public class EjbRelationshipRole {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "ejb-relationship-role-name")
    protected String ejbRelationshipRoleName;
    @XmlElement(required = true)
    protected Multiplicity multiplicity;
    @XmlElement(name = "cascade-delete")
    protected EmptyType cascadeDelete;
    @XmlElement(name = "relationship-role-source", required = true)
    protected RelationshipRoleSource relationshipRoleSource;
    @XmlElement(name = "cmr-field")
    protected CmrField cmrField;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
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
        this.cascadeDelete = value ? new EmptyType() : null;
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
