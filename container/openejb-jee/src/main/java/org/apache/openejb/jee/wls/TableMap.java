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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for table-map complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="table-map">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="table-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="field-map" type="{http://www.bea.com/ns/weblogic/90}field-map" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="verify-rows" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="verify-columns" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="optimistic-column" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="trigger-updates-optimistic-column" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="version-column-initial-value" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
@XmlType(name = "table-map", propOrder = {
    "tableName",
    "fieldMap",
    "verifyRows",
    "verifyColumns",
    "optimisticColumn",
    "triggerUpdatesOptimisticColumn",
    "versionColumnInitialValue"
})
public class TableMap {

    @XmlElement(name = "table-name", required = true)
    protected String tableName;
    @XmlElement(name = "field-map")
    protected List<FieldMap> fieldMap;
    @XmlElement(name = "verify-rows")
    protected String verifyRows;
    @XmlElement(name = "verify-columns")
    protected String verifyColumns;
    @XmlElement(name = "optimistic-column")
    protected String optimisticColumn;
    @XmlElement(name = "trigger-updates-optimistic-column")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean triggerUpdatesOptimisticColumn;
    @XmlElement(name = "version-column-initial-value")
    protected BigInteger versionColumnInitialValue;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the tableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableName(String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the fieldMap property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldMap property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldMap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldMap }
     * 
     * 
     */
    public List<FieldMap> getFieldMap() {
        if (fieldMap == null) {
            fieldMap = new ArrayList<FieldMap>();
        }
        return this.fieldMap;
    }

    /**
     * Gets the value of the verifyRows property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVerifyRows() {
        return verifyRows;
    }

    /**
     * Sets the value of the verifyRows property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVerifyRows(String value) {
        this.verifyRows = value;
    }

    /**
     * Gets the value of the verifyColumns property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVerifyColumns() {
        return verifyColumns;
    }

    /**
     * Sets the value of the verifyColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVerifyColumns(String value) {
        this.verifyColumns = value;
    }

    /**
     * Gets the value of the optimisticColumn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOptimisticColumn() {
        return optimisticColumn;
    }

    /**
     * Sets the value of the optimisticColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOptimisticColumn(String value) {
        this.optimisticColumn = value;
    }

    /**
     * Gets the value of the triggerUpdatesOptimisticColumn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getTriggerUpdatesOptimisticColumn() {
        return triggerUpdatesOptimisticColumn;
    }

    /**
     * Sets the value of the triggerUpdatesOptimisticColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTriggerUpdatesOptimisticColumn(Boolean value) {
        this.triggerUpdatesOptimisticColumn = value;
    }

    /**
     * Gets the value of the versionColumnInitialValue property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getVersionColumnInitialValue() {
        return versionColumnInitialValue;
    }

    /**
     * Sets the value of the versionColumnInitialValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setVersionColumnInitialValue(BigInteger value) {
        this.versionColumnInitialValue = value;
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
