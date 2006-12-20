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

package org.apache.openejb.jee.sun;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ejbName",
    "tableName",
    "cmpFieldMapping",
    "cmrFieldMapping",
    "secondaryTable",
    "consistency"
})
@XmlRootElement(name = "entity-mapping")
public class EntityMapping {

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "table-name", required = true)
    protected String tableName;
    @XmlElement(name = "cmp-field-mapping", required = true)
    protected List<CmpFieldMapping> cmpFieldMapping;
    @XmlElement(name = "cmr-field-mapping")
    protected List<CmrFieldMapping> cmrFieldMapping;
    @XmlElement(name = "secondary-table")
    protected List<SecondaryTable> secondaryTable;
    protected Consistency consistency;

    /**
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjbName(String value) {
        this.ejbName = value;
    }

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
     * Gets the value of the cmpFieldMapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmpFieldMapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmpFieldMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmpFieldMapping }
     * 
     * 
     */
    public List<CmpFieldMapping> getCmpFieldMapping() {
        if (cmpFieldMapping == null) {
            cmpFieldMapping = new ArrayList<CmpFieldMapping>();
        }
        return this.cmpFieldMapping;
    }

    /**
     * Gets the value of the cmrFieldMapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmrFieldMapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmrFieldMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmrFieldMapping }
     * 
     * 
     */
    public List<CmrFieldMapping> getCmrFieldMapping() {
        if (cmrFieldMapping == null) {
            cmrFieldMapping = new ArrayList<CmrFieldMapping>();
        }
        return this.cmrFieldMapping;
    }

    /**
     * Gets the value of the secondaryTable property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the secondaryTable property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecondaryTable().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SecondaryTable }
     * 
     * 
     */
    public List<SecondaryTable> getSecondaryTable() {
        if (secondaryTable == null) {
            secondaryTable = new ArrayList<SecondaryTable>();
        }
        return this.secondaryTable;
    }

    /**
     * Gets the value of the consistency property.
     * 
     * @return
     *     possible object is
     *     {@link Consistency }
     *     
     */
    public Consistency getConsistency() {
        return consistency;
    }

    /**
     * Sets the value of the consistency property.
     * 
     * @param value
     *     allowed object is
     *     {@link Consistency }
     *     
     */
    public void setConsistency(Consistency value) {
        this.consistency = value;
    }

}
