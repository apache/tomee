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

package org.apache.openejb.jee.jba.cmp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://jboss.org}ejb-relationship-role-name"/>
 *         &lt;element ref="{http://jboss.org}fk-constraint" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}key-fields" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-ahead" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}batch-cascade-delete" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ejbRelationshipRoleName",
    "fkConstraint",
    "keyFields",
    "readAhead",
    "batchCascadeDelete"
})
@XmlRootElement(name = "ejb-relationship-role")
public class EjbRelationshipRole {

    @XmlElement(name = "ejb-relationship-role-name", required = true)
    protected EjbRelationshipRoleName ejbRelationshipRoleName;
    @XmlElement(name = "fk-constraint")
    protected FkConstraint fkConstraint;
    @XmlElement(name = "key-fields")
    protected KeyFields keyFields;
    @XmlElement(name = "read-ahead")
    protected ReadAhead readAhead;
    @XmlElement(name = "batch-cascade-delete")
    protected BatchCascadeDelete batchCascadeDelete;

    /**
     * Gets the value of the ejbRelationshipRoleName property.
     * 
     * @return
     *     possible object is
     *     {@link EjbRelationshipRoleName }
     *     
     */
    public EjbRelationshipRoleName getEjbRelationshipRoleName() {
        return ejbRelationshipRoleName;
    }

    /**
     * Sets the value of the ejbRelationshipRoleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link EjbRelationshipRoleName }
     *     
     */
    public void setEjbRelationshipRoleName(EjbRelationshipRoleName value) {
        this.ejbRelationshipRoleName = value;
    }

    /**
     * Gets the value of the fkConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link FkConstraint }
     *     
     */
    public FkConstraint getFkConstraint() {
        return fkConstraint;
    }

    /**
     * Sets the value of the fkConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link FkConstraint }
     *     
     */
    public void setFkConstraint(FkConstraint value) {
        this.fkConstraint = value;
    }

    /**
     * Gets the value of the keyFields property.
     * 
     * @return
     *     possible object is
     *     {@link KeyFields }
     *     
     */
    public KeyFields getKeyFields() {
        return keyFields;
    }

    /**
     * Sets the value of the keyFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyFields }
     *     
     */
    public void setKeyFields(KeyFields value) {
        this.keyFields = value;
    }

    /**
     * Gets the value of the readAhead property.
     * 
     * @return
     *     possible object is
     *     {@link ReadAhead }
     *     
     */
    public ReadAhead getReadAhead() {
        return readAhead;
    }

    /**
     * Sets the value of the readAhead property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReadAhead }
     *     
     */
    public void setReadAhead(ReadAhead value) {
        this.readAhead = value;
    }

    /**
     * Gets the value of the batchCascadeDelete property.
     * 
     * @return
     *     possible object is
     *     {@link BatchCascadeDelete }
     *     
     */
    public BatchCascadeDelete getBatchCascadeDelete() {
        return batchCascadeDelete;
    }

    /**
     * Sets the value of the batchCascadeDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link BatchCascadeDelete }
     *     
     */
    public void setBatchCascadeDelete(BatchCascadeDelete value) {
        this.batchCascadeDelete = value;
    }

}
