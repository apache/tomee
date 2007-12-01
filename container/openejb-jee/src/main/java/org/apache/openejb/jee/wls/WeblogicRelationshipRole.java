/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.wls;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for weblogic-relationship-role complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="weblogic-relationship-role">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="relationship-role-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="relationship-role-map" type="{http://www.bea.com/ns/weblogic/90}relationship-role-map" minOccurs="0"/>
 *         &lt;element name="db-cascade-delete" type="{http://www.bea.com/ns/weblogic/90}empty" minOccurs="0"/>
 *         &lt;element name="enable-query-caching" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
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
@XmlType(name = "weblogic-relationship-role", propOrder = {
    "relationshipRoleName",
    "groupName",
    "relationshipRoleMap",
    "dbCascadeDelete",
    "enableQueryCaching"
})
public class WeblogicRelationshipRole {

    @XmlElement(name = "relationship-role-name", required = true)
    protected String relationshipRoleName;
    @XmlElement(name = "group-name")
    protected String groupName;
    @XmlElement(name = "relationship-role-map")
    protected RelationshipRoleMap relationshipRoleMap;
    @XmlElement(name = "db-cascade-delete")
    protected Empty dbCascadeDelete;
    @XmlElement(name = "enable-query-caching")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean enableQueryCaching;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the relationshipRoleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelationshipRoleName() {
        return relationshipRoleName;
    }

    /**
     * Sets the value of the relationshipRoleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelationshipRoleName(String value) {
        this.relationshipRoleName = value;
    }

    /**
     * Gets the value of the groupName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupName(String value) {
        this.groupName = value;
    }

    /**
     * Gets the value of the relationshipRoleMap property.
     * 
     * @return
     *     possible object is
     *     {@link RelationshipRoleMap }
     *     
     */
    public RelationshipRoleMap getRelationshipRoleMap() {
        return relationshipRoleMap;
    }

    /**
     * Sets the value of the relationshipRoleMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationshipRoleMap }
     *     
     */
    public void setRelationshipRoleMap(RelationshipRoleMap value) {
        this.relationshipRoleMap = value;
    }

    /**
     * Gets the value of the dbCascadeDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getDbCascadeDelete() {
        return dbCascadeDelete;
    }

    /**
     * Sets the value of the dbCascadeDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setDbCascadeDelete(Empty value) {
        this.dbCascadeDelete = value;
    }

    /**
     * Gets the value of the enableQueryCaching property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getEnableQueryCaching() {
        return enableQueryCaching;
    }

    /**
     * Sets the value of the enableQueryCaching property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableQueryCaching(Boolean value) {
        this.enableQueryCaching = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
