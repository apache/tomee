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
 * <p>Java class for weblogic-rdbms-bean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="weblogic-rdbms-bean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="data-source-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="unknown-primary-key-field" type="{http://www.bea.com/ns/weblogic/90}unknown-primary-key-field" minOccurs="0"/>
 *         &lt;element name="table-map" type="{http://www.bea.com/ns/weblogic/90}table-map" maxOccurs="unbounded"/>
 *         &lt;element name="field-group" type="{http://www.bea.com/ns/weblogic/90}field-group" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="relationship-caching" type="{http://www.bea.com/ns/weblogic/90}relationship-caching" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sql-shape" type="{http://www.bea.com/ns/weblogic/90}sql-shape" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="weblogic-query" type="{http://www.bea.com/ns/weblogic/90}weblogic-query" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="delay-database-insert-until" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="use-select-for-update" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="lock-order" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="instance-lock-order" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="automatic-key-generation" type="{http://www.bea.com/ns/weblogic/90}automatic-key-generation" minOccurs="0"/>
 *         &lt;element name="check-exists-on-method" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="cluster-invalidation-disabled" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
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
@XmlType(name = "weblogic-rdbms-bean", propOrder = {
    "ejbName",
    "dataSourceJndiName",
    "unknownPrimaryKeyField",
    "tableMap",
    "fieldGroup",
    "relationshipCaching",
    "sqlShape",
    "weblogicQuery",
    "delayDatabaseInsertUntil",
    "useSelectForUpdate",
    "lockOrder",
    "instanceLockOrder",
    "automaticKeyGeneration",
    "checkExistsOnMethod",
    "clusterInvalidationDisabled"
})
public class WeblogicRdbmsBean {

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "data-source-jndi-name", required = true)
    protected String dataSourceJndiName;
    @XmlElement(name = "unknown-primary-key-field")
    protected UnknownPrimaryKeyField unknownPrimaryKeyField;
    @XmlElement(name = "table-map", required = true)
    protected List<TableMap> tableMap;
    @XmlElement(name = "field-group")
    protected List<FieldGroup> fieldGroup;
    @XmlElement(name = "relationship-caching")
    protected List<RelationshipCaching> relationshipCaching;
    @XmlElement(name = "sql-shape")
    protected List<SqlShape> sqlShape;
    @XmlElement(name = "weblogic-query")
    protected List<WeblogicQuery> weblogicQuery;
    @XmlElement(name = "delay-database-insert-until")
    protected String delayDatabaseInsertUntil;
    @XmlElement(name = "use-select-for-update")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean useSelectForUpdate;
    @XmlElement(name = "lock-order")
    protected BigInteger lockOrder;
    @XmlElement(name = "instance-lock-order")
    protected String instanceLockOrder;
    @XmlElement(name = "automatic-key-generation")
    protected AutomaticKeyGeneration automaticKeyGeneration;
    @XmlElement(name = "check-exists-on-method")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean checkExistsOnMethod;
    @XmlElement(name = "cluster-invalidation-disabled")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean clusterInvalidationDisabled;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

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
     * Gets the value of the dataSourceJndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSourceJndiName() {
        return dataSourceJndiName;
    }

    /**
     * Sets the value of the dataSourceJndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSourceJndiName(String value) {
        this.dataSourceJndiName = value;
    }

    /**
     * Gets the value of the unknownPrimaryKeyField property.
     * 
     * @return
     *     possible object is
     *     {@link UnknownPrimaryKeyField }
     *     
     */
    public UnknownPrimaryKeyField getUnknownPrimaryKeyField() {
        return unknownPrimaryKeyField;
    }

    /**
     * Sets the value of the unknownPrimaryKeyField property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnknownPrimaryKeyField }
     *     
     */
    public void setUnknownPrimaryKeyField(UnknownPrimaryKeyField value) {
        this.unknownPrimaryKeyField = value;
    }

    /**
     * Gets the value of the tableMap property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tableMap property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTableMap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TableMap }
     * 
     * 
     */
    public List<TableMap> getTableMap() {
        if (tableMap == null) {
            tableMap = new ArrayList<TableMap>();
        }
        return this.tableMap;
    }

    /**
     * Gets the value of the fieldGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldGroup }
     * 
     * 
     */
    public List<FieldGroup> getFieldGroup() {
        if (fieldGroup == null) {
            fieldGroup = new ArrayList<FieldGroup>();
        }
        return this.fieldGroup;
    }

    /**
     * Gets the value of the relationshipCaching property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relationshipCaching property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelationshipCaching().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RelationshipCaching }
     * 
     * 
     */
    public List<RelationshipCaching> getRelationshipCaching() {
        if (relationshipCaching == null) {
            relationshipCaching = new ArrayList<RelationshipCaching>();
        }
        return this.relationshipCaching;
    }

    /**
     * Gets the value of the sqlShape property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sqlShape property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSqlShape().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SqlShape }
     * 
     * 
     */
    public List<SqlShape> getSqlShape() {
        if (sqlShape == null) {
            sqlShape = new ArrayList<SqlShape>();
        }
        return this.sqlShape;
    }

    /**
     * Gets the value of the weblogicQuery property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the weblogicQuery property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeblogicQuery().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WeblogicQuery }
     * 
     * 
     */
    public List<WeblogicQuery> getWeblogicQuery() {
        if (weblogicQuery == null) {
            weblogicQuery = new ArrayList<WeblogicQuery>();
        }
        return this.weblogicQuery;
    }

    /**
     * Gets the value of the delayDatabaseInsertUntil property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelayDatabaseInsertUntil() {
        return delayDatabaseInsertUntil;
    }

    /**
     * Sets the value of the delayDatabaseInsertUntil property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelayDatabaseInsertUntil(String value) {
        this.delayDatabaseInsertUntil = value;
    }

    /**
     * Gets the value of the useSelectForUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getUseSelectForUpdate() {
        return useSelectForUpdate;
    }

    /**
     * Sets the value of the useSelectForUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseSelectForUpdate(Boolean value) {
        this.useSelectForUpdate = value;
    }

    /**
     * Gets the value of the lockOrder property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLockOrder() {
        return lockOrder;
    }

    /**
     * Sets the value of the lockOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLockOrder(BigInteger value) {
        this.lockOrder = value;
    }

    /**
     * Gets the value of the instanceLockOrder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceLockOrder() {
        return instanceLockOrder;
    }

    /**
     * Sets the value of the instanceLockOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceLockOrder(String value) {
        this.instanceLockOrder = value;
    }

    /**
     * Gets the value of the automaticKeyGeneration property.
     * 
     * @return
     *     possible object is
     *     {@link AutomaticKeyGeneration }
     *     
     */
    public AutomaticKeyGeneration getAutomaticKeyGeneration() {
        return automaticKeyGeneration;
    }

    /**
     * Sets the value of the automaticKeyGeneration property.
     * 
     * @param value
     *     allowed object is
     *     {@link AutomaticKeyGeneration }
     *     
     */
    public void setAutomaticKeyGeneration(AutomaticKeyGeneration value) {
        this.automaticKeyGeneration = value;
    }

    /**
     * Gets the value of the checkExistsOnMethod property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getCheckExistsOnMethod() {
        return checkExistsOnMethod;
    }

    /**
     * Sets the value of the checkExistsOnMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCheckExistsOnMethod(Boolean value) {
        this.checkExistsOnMethod = value;
    }

    /**
     * Gets the value of the clusterInvalidationDisabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getClusterInvalidationDisabled() {
        return clusterInvalidationDisabled;
    }

    /**
     * Sets the value of the clusterInvalidationDisabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClusterInvalidationDisabled(Boolean value) {
        this.clusterInvalidationDisabled = value;
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
