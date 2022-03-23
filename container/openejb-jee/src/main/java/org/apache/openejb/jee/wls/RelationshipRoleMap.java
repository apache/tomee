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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for relationship-role-map complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="relationship-role-map"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="foreign-key-table" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="primary-key-table" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="column-map" type="{http://www.bea.com/ns/weblogic/90}column-map" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relationship-role-map", propOrder = {
    "foreignKeyTable",
    "primaryKeyTable",
    "columnMap"
})
public class RelationshipRoleMap {

    @XmlElement(name = "foreign-key-table")
    protected String foreignKeyTable;
    @XmlElement(name = "primary-key-table")
    protected String primaryKeyTable;
    @XmlElement(name = "column-map", required = true)
    protected List<ColumnMap> columnMap;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the foreignKeyTable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getForeignKeyTable() {
        return foreignKeyTable;
    }

    /**
     * Sets the value of the foreignKeyTable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setForeignKeyTable(final String value) {
        this.foreignKeyTable = value;
    }

    /**
     * Gets the value of the primaryKeyTable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPrimaryKeyTable() {
        return primaryKeyTable;
    }

    /**
     * Sets the value of the primaryKeyTable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPrimaryKeyTable(final String value) {
        this.primaryKeyTable = value;
    }

    /**
     * Gets the value of the columnMap property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the columnMap property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getColumnMap().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ColumnMap }
     */
    public List<ColumnMap> getColumnMap() {
        if (columnMap == null) {
            columnMap = new ArrayList<ColumnMap>();
        }
        return this.columnMap;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
