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

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{http://jboss.org}field-name"/>
 *         &lt;element ref="{http://jboss.org}read-only" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-time-out" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}column-name" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}not-null" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;sequence>
 *             &lt;element ref="{http://jboss.org}jdbc-type"/>
 *             &lt;element ref="{http://jboss.org}sql-type"/>
 *           &lt;/sequence>
 *           &lt;sequence>
 *             &lt;element ref="{http://jboss.org}property" maxOccurs="unbounded"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *         &lt;element ref="{http://jboss.org}auto-increment" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}dbindex" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}check-dirty-after-get" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}state-factory" minOccurs="0"/>
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
    "fieldName",
    "readOnly",
    "readTimeOut",
    "columnName",
    "notNull",
    "jdbcType",
    "sqlType",
    "property",
    "autoIncrement",
    "dbindex",
    "checkDirtyAfterGet",
    "stateFactory"
})
@XmlRootElement(name = "cmp-field")
public class CmpField {

    @XmlElement(name = "field-name", required = true)
    protected FieldName fieldName;
    @XmlElement(name = "read-only")
    protected ReadOnly readOnly;
    @XmlElement(name = "read-time-out")
    protected ReadTimeOut readTimeOut;
    @XmlElement(name = "column-name")
    protected ColumnName columnName;
    @XmlElement(name = "not-null")
    protected NotNull notNull;
    @XmlElement(name = "jdbc-type")
    protected JdbcType jdbcType;
    @XmlElement(name = "sql-type")
    protected SqlType sqlType;
    protected List<Property> property;
    @XmlElement(name = "auto-increment")
    protected AutoIncrement autoIncrement;
    protected Dbindex dbindex;
    @XmlElement(name = "check-dirty-after-get")
    protected CheckDirtyAfterGet checkDirtyAfterGet;
    @XmlElement(name = "state-factory")
    protected StateFactory stateFactory;

    /**
     * Gets the value of the fieldName property.
     * 
     * @return
     *     possible object is
     *     {@link FieldName }
     *     
     */
    public FieldName getFieldName() {
        return fieldName;
    }

    /**
     * Sets the value of the fieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldName }
     *     
     */
    public void setFieldName(FieldName value) {
        this.fieldName = value;
    }

    /**
     * Gets the value of the readOnly property.
     * 
     * @return
     *     possible object is
     *     {@link ReadOnly }
     *     
     */
    public ReadOnly getReadOnly() {
        return readOnly;
    }

    /**
     * Sets the value of the readOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReadOnly }
     *     
     */
    public void setReadOnly(ReadOnly value) {
        this.readOnly = value;
    }

    /**
     * Gets the value of the readTimeOut property.
     * 
     * @return
     *     possible object is
     *     {@link ReadTimeOut }
     *     
     */
    public ReadTimeOut getReadTimeOut() {
        return readTimeOut;
    }

    /**
     * Sets the value of the readTimeOut property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReadTimeOut }
     *     
     */
    public void setReadTimeOut(ReadTimeOut value) {
        this.readTimeOut = value;
    }

    /**
     * Gets the value of the columnName property.
     * 
     * @return
     *     possible object is
     *     {@link ColumnName }
     *     
     */
    public ColumnName getColumnName() {
        return columnName;
    }

    /**
     * Sets the value of the columnName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColumnName }
     *     
     */
    public void setColumnName(ColumnName value) {
        this.columnName = value;
    }

    /**
     * Gets the value of the notNull property.
     * 
     * @return
     *     possible object is
     *     {@link NotNull }
     *     
     */
    public NotNull getNotNull() {
        return notNull;
    }

    /**
     * Sets the value of the notNull property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotNull }
     *     
     */
    public void setNotNull(NotNull value) {
        this.notNull = value;
    }

    /**
     * Gets the value of the jdbcType property.
     * 
     * @return
     *     possible object is
     *     {@link JdbcType }
     *     
     */
    public JdbcType getJdbcType() {
        return jdbcType;
    }

    /**
     * Sets the value of the jdbcType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JdbcType }
     *     
     */
    public void setJdbcType(JdbcType value) {
        this.jdbcType = value;
    }

    /**
     * Gets the value of the sqlType property.
     * 
     * @return
     *     possible object is
     *     {@link SqlType }
     *     
     */
    public SqlType getSqlType() {
        return sqlType;
    }

    /**
     * Sets the value of the sqlType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SqlType }
     *     
     */
    public void setSqlType(SqlType value) {
        this.sqlType = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * 
     * 
     */
    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

    /**
     * Gets the value of the autoIncrement property.
     * 
     * @return
     *     possible object is
     *     {@link AutoIncrement }
     *     
     */
    public AutoIncrement getAutoIncrement() {
        return autoIncrement;
    }

    /**
     * Sets the value of the autoIncrement property.
     * 
     * @param value
     *     allowed object is
     *     {@link AutoIncrement }
     *     
     */
    public void setAutoIncrement(AutoIncrement value) {
        this.autoIncrement = value;
    }

    /**
     * Gets the value of the dbindex property.
     * 
     * @return
     *     possible object is
     *     {@link Dbindex }
     *     
     */
    public Dbindex getDbindex() {
        return dbindex;
    }

    /**
     * Sets the value of the dbindex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Dbindex }
     *     
     */
    public void setDbindex(Dbindex value) {
        this.dbindex = value;
    }

    /**
     * Gets the value of the checkDirtyAfterGet property.
     * 
     * @return
     *     possible object is
     *     {@link CheckDirtyAfterGet }
     *     
     */
    public CheckDirtyAfterGet getCheckDirtyAfterGet() {
        return checkDirtyAfterGet;
    }

    /**
     * Sets the value of the checkDirtyAfterGet property.
     * 
     * @param value
     *     allowed object is
     *     {@link CheckDirtyAfterGet }
     *     
     */
    public void setCheckDirtyAfterGet(CheckDirtyAfterGet value) {
        this.checkDirtyAfterGet = value;
    }

    /**
     * Gets the value of the stateFactory property.
     * 
     * @return
     *     possible object is
     *     {@link StateFactory }
     *     
     */
    public StateFactory getStateFactory() {
        return stateFactory;
    }

    /**
     * Sets the value of the stateFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link StateFactory }
     *     
     */
    public void setStateFactory(StateFactory value) {
        this.stateFactory = value;
    }

}
