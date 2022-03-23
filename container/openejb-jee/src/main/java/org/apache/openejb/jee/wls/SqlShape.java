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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sql-shape complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="sql-shape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.bea.com/ns/weblogic/90}description" minOccurs="0"/&gt;
 *         &lt;element name="sql-shape-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="table" type="{http://www.bea.com/ns/weblogic/90}table" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="pass-through-columns" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="ejb-relation-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sql-shape", propOrder = {
    "description",
    "sqlShapeName",
    "table",
    "passThroughColumns",
    "ejbRelationName"
})
public class SqlShape {

    protected Description description;
    @XmlElement(name = "sql-shape-name", required = true)
    protected String sqlShapeName;
    protected List<Table> table;
    @XmlElement(name = "pass-through-columns")
    protected BigInteger passThroughColumns;
    @XmlElement(name = "ejb-relation-name")
    protected List<String> ejbRelationName;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link Description }
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link Description }
     */
    public void setDescription(final Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the sqlShapeName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSqlShapeName() {
        return sqlShapeName;
    }

    /**
     * Sets the value of the sqlShapeName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSqlShapeName(final String value) {
        this.sqlShapeName = value;
    }

    /**
     * Gets the value of the table property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the table property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTable().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Table }
     */
    public List<Table> getTable() {
        if (table == null) {
            table = new ArrayList<Table>();
        }
        return this.table;
    }

    /**
     * Gets the value of the passThroughColumns property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getPassThroughColumns() {
        return passThroughColumns;
    }

    /**
     * Sets the value of the passThroughColumns property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setPassThroughColumns(final BigInteger value) {
        this.passThroughColumns = value;
    }

    /**
     * Gets the value of the ejbRelationName property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRelationName property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRelationName().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getEjbRelationName() {
        if (ejbRelationName == null) {
            ejbRelationName = new ArrayList<String>();
        }
        return this.ejbRelationName;
    }

}
