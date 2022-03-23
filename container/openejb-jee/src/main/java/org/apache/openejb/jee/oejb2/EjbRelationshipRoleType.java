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

package org.apache.openejb.jee.oejb2;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ejb-relationship-roleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ejb-relationship-roleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ejb-relationship-role-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="relationship-role-source"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="cmr-field" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="cmr-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="foreign-key-column-on-source" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="role-mapping"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="cmr-field-mapping" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="key-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                             &lt;element name="foreign-key-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-relationship-roleType", propOrder = {
    "ejbRelationshipRoleName",
    "relationshipRoleSource",
    "cmrField",
    "foreignKeyColumnOnSource",
    "roleMapping"
})
public class EjbRelationshipRoleType {

    @XmlElement(name = "ejb-relationship-role-name")
    protected String ejbRelationshipRoleName;
    @XmlElement(name = "relationship-role-source", required = true)
    protected EjbRelationshipRoleType.RelationshipRoleSource relationshipRoleSource;
    @XmlElement(name = "cmr-field")
    protected EjbRelationshipRoleType.CmrField cmrField;
    @XmlElement(name = "foreign-key-column-on-source")
    protected EmptyType foreignKeyColumnOnSource;
    @XmlElement(name = "role-mapping", required = true)
    protected EjbRelationshipRoleType.RoleMapping roleMapping;

    /**
     * Gets the value of the ejbRelationshipRoleName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEjbRelationshipRoleName() {
        return ejbRelationshipRoleName;
    }

    /**
     * Sets the value of the ejbRelationshipRoleName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbRelationshipRoleName(final String value) {
        this.ejbRelationshipRoleName = value;
    }

    /**
     * Gets the value of the relationshipRoleSource property.
     *
     * @return possible object is
     * {@link EjbRelationshipRoleType.RelationshipRoleSource }
     */
    public EjbRelationshipRoleType.RelationshipRoleSource getRelationshipRoleSource() {
        return relationshipRoleSource;
    }

    /**
     * Sets the value of the relationshipRoleSource property.
     *
     * @param value allowed object is
     *              {@link EjbRelationshipRoleType.RelationshipRoleSource }
     */
    public void setRelationshipRoleSource(final EjbRelationshipRoleType.RelationshipRoleSource value) {
        this.relationshipRoleSource = value;
    }

    /**
     * Gets the value of the cmrField property.
     *
     * @return possible object is
     * {@link EjbRelationshipRoleType.CmrField }
     */
    public EjbRelationshipRoleType.CmrField getCmrField() {
        return cmrField;
    }

    /**
     * Sets the value of the cmrField property.
     *
     * @param value allowed object is
     *              {@link EjbRelationshipRoleType.CmrField }
     */
    public void setCmrField(final EjbRelationshipRoleType.CmrField value) {
        this.cmrField = value;
    }

    /**
     * Gets the value of the foreignKeyColumnOnSource property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isForeignKeyColumnOnSource() {
        return foreignKeyColumnOnSource != null;
    }

    /**
     * Sets the value of the foreignKeyColumnOnSource property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setForeignKeyColumnOnSource(final boolean value) {
        this.foreignKeyColumnOnSource = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the roleMapping property.
     *
     * @return possible object is
     * {@link EjbRelationshipRoleType.RoleMapping }
     */
    public EjbRelationshipRoleType.RoleMapping getRoleMapping() {
        return roleMapping;
    }

    /**
     * Sets the value of the roleMapping property.
     *
     * @param value allowed object is
     *              {@link EjbRelationshipRoleType.RoleMapping }
     */
    public void setRoleMapping(final EjbRelationshipRoleType.RoleMapping value) {
        this.roleMapping = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="cmr-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cmrFieldName"
    })
    public static class CmrField {

        @XmlElement(name = "cmr-field-name", required = true)
        protected String cmrFieldName;

        /**
         * Gets the value of the cmrFieldName property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCmrFieldName() {
            return cmrFieldName;
        }

        /**
         * Sets the value of the cmrFieldName property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCmrFieldName(final String value) {
            this.cmrFieldName = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "ejbName"
    })
    public static class RelationshipRoleSource {

        @XmlElement(name = "ejb-name", required = true)
        protected String ejbName;

        /**
         * Gets the value of the ejbName property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getEjbName() {
            return ejbName;
        }

        /**
         * Sets the value of the ejbName property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setEjbName(final String value) {
            this.ejbName = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="cmr-field-mapping" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="key-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                   &lt;element name="foreign-key-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cmrFieldMapping"
    })
    public static class RoleMapping {

        @XmlElement(name = "cmr-field-mapping", required = true)
        protected List<EjbRelationshipRoleType.RoleMapping.CmrFieldMapping> cmrFieldMapping;

        /**
         * Gets the value of the cmrFieldMapping property.
         *
         *
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cmrFieldMapping property.
         *
         *
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCmrFieldMapping().add(newItem);
         * </pre>
         *
         *
         *
         * Objects of the following type(s) are allowed in the list
         * {@link EjbRelationshipRoleType.RoleMapping.CmrFieldMapping }
         */
        public List<EjbRelationshipRoleType.RoleMapping.CmrFieldMapping> getCmrFieldMapping() {
            if (cmrFieldMapping == null) {
                cmrFieldMapping = new ArrayList<EjbRelationshipRoleType.RoleMapping.CmrFieldMapping>();
            }
            return this.cmrFieldMapping;
        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="key-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *         &lt;element name="foreign-key-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "keyColumn",
            "foreignKeyColumn"
        })
        public static class CmrFieldMapping {

            @XmlElement(name = "key-column", required = true)
            protected String keyColumn;
            @XmlElement(name = "foreign-key-column", required = true)
            protected String foreignKeyColumn;

            /**
             * Gets the value of the keyColumn property.
             *
             * @return possible object is
             * {@link String }
             */
            public String getKeyColumn() {
                return keyColumn;
            }

            /**
             * Sets the value of the keyColumn property.
             *
             * @param value allowed object is
             *              {@link String }
             */
            public void setKeyColumn(final String value) {
                this.keyColumn = value;
            }

            /**
             * Gets the value of the foreignKeyColumn property.
             *
             * @return possible object is
             * {@link String }
             */
            public String getForeignKeyColumn() {
                return foreignKeyColumn;
            }

            /**
             * Sets the value of the foreignKeyColumn property.
             *
             * @param value allowed object is
             *              {@link String }
             */
            public void setForeignKeyColumn(final String value) {
                this.foreignKeyColumn = value;
            }

        }

    }

}
