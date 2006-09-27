/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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


/**
 * The ejb-relationship-roleType describes a role within a
 * relationship. There are two roles in each relationship.
 * <p/>
 * The ejb-relationship-roleType contains an optional
 * description; an optional name for the relationship role; a
 * specification of the multiplicity of the role; an optional
 * specification of cascade-delete functionality for the role;
 * the role source; and a declaration of the cmr-field, if any,
 * by means of which the other side of the relationship is
 * accessed from the perspective of the role source.
 * <p/>
 * The multiplicity and role-source element are mandatory.
 * <p/>
 * The relationship-role-source element designates an entity
 * bean by means of an ejb-name element. For bidirectional
 * relationships, both roles of a relationship must declare a
 * relationship-role-source element that specifies a cmr-field
 * in terms of which the relationship is accessed. The lack of
 * a cmr-field element in an ejb-relationship-role specifies
 * that the relationship is unidirectional in navigability and
 * the entity bean that participates in the relationship is
 * "not aware" of the relationship.
 * <p/>
 * Example:
 * <p/>
 * <ejb-relation>
 * <ejb-relation-name>Product-LineItem</ejb-relation-name>
 * <ejb-relationship-role>
 * <ejb-relationship-role-name>product-has-lineitems
 * </ejb-relationship-role-name>
 * <multiplicity>One</multiplicity>
 * <relationship-role-source>
 * <ejb-name>ProductEJB</ejb-name>
 * </relationship-role-source>
 * </ejb-relationship-role>
 * </ejb-relation>
 */
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

    public EmptyType getCascadeDelete() {
        return cascadeDelete;
    }

    public void setCascadeDelete(EmptyType value) {
        this.cascadeDelete = value;
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
