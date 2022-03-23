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
 * <p>Java class for ejb-relationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ejb-relationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ejb-relation-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="many-to-many-table-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ejb-relationship-role" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}ejb-relationship-roleType" maxOccurs="2"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-relationType", propOrder = {
    "ejbRelationName",
    "manyToManyTableName",
    "ejbRelationshipRole"
})
public class EjbRelationType {

    @XmlElement(name = "ejb-relation-name")
    protected String ejbRelationName;
    @XmlElement(name = "many-to-many-table-name")
    protected String manyToManyTableName;
    @XmlElement(name = "ejb-relationship-role", required = true)
    protected List<EjbRelationshipRoleType> ejbRelationshipRole;

    /**
     * Gets the value of the ejbRelationName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEjbRelationName() {
        return ejbRelationName;
    }

    /**
     * Sets the value of the ejbRelationName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbRelationName(final String value) {
        this.ejbRelationName = value;
    }

    /**
     * Gets the value of the manyToManyTableName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManyToManyTableName() {
        return manyToManyTableName;
    }

    /**
     * Sets the value of the manyToManyTableName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManyToManyTableName(final String value) {
        this.manyToManyTableName = value;
    }

    /**
     * Gets the value of the ejbRelationshipRole property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRelationshipRole property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRelationshipRole().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRelationshipRoleType }
     */
    public List<EjbRelationshipRoleType> getEjbRelationshipRole() {
        if (ejbRelationshipRole == null) {
            ejbRelationshipRole = new ArrayList<EjbRelationshipRoleType>();
        }
        return this.ejbRelationshipRole;
    }

}
