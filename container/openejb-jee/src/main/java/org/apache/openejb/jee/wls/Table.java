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

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for table complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="table"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="table-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dbms-column" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="ejb-relationship-role-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "table", propOrder = {
    "tableName",
    "dbmsColumn",
    "ejbRelationshipRoleName"
})
public class Table {

    @XmlElement(name = "table-name", required = true)
    protected String tableName;
    @XmlElement(name = "dbms-column", required = true)
    protected List<String> dbmsColumn;
    @XmlElement(name = "ejb-relationship-role-name")
    protected String ejbRelationshipRoleName;

    /**
     * Gets the value of the tableName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTableName(final String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the dbmsColumn property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dbmsColumn property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDbmsColumn().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getDbmsColumn() {
        if (dbmsColumn == null) {
            dbmsColumn = new ArrayList<String>();
        }
        return this.dbmsColumn;
    }

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

}
