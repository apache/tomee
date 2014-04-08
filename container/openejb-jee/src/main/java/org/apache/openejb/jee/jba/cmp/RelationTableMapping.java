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
 *         &lt;element ref="{http://jboss.org}table-name" minOccurs="0"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element ref="{http://jboss.org}datasource"/>
 *           &lt;element ref="{http://jboss.org}datasource-mapping"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://jboss.org}create-table" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}remove-table" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}post-table-create" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}row-locking" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}pk-constraint" minOccurs="0"/>
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
    "tableName",
    "datasource",
    "datasourceMapping",
    "createTable",
    "removeTable",
    "postTableCreate",
    "rowLocking",
    "pkConstraint"
})
@XmlRootElement(name = "relation-table-mapping")
public class RelationTableMapping {

    @XmlElement(name = "table-name")
    protected TableName tableName;
    protected Datasource datasource;
    @XmlElement(name = "datasource-mapping")
    protected DatasourceMapping datasourceMapping;
    @XmlElement(name = "create-table")
    protected CreateTable createTable;
    @XmlElement(name = "remove-table")
    protected RemoveTable removeTable;
    @XmlElement(name = "post-table-create")
    protected PostTableCreate postTableCreate;
    @XmlElement(name = "row-locking")
    protected RowLocking rowLocking;
    @XmlElement(name = "pk-constraint")
    protected PkConstraint pkConstraint;

    /**
     * Gets the value of the tableName property.
     * 
     * @return
     *     possible object is
     *     {@link TableName }
     *     
     */
    public TableName getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableName }
     *     
     */
    public void setTableName(TableName value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the datasource property.
     * 
     * @return
     *     possible object is
     *     {@link Datasource }
     *     
     */
    public Datasource getDatasource() {
        return datasource;
    }

    /**
     * Sets the value of the datasource property.
     * 
     * @param value
     *     allowed object is
     *     {@link Datasource }
     *     
     */
    public void setDatasource(Datasource value) {
        this.datasource = value;
    }

    /**
     * Gets the value of the datasourceMapping property.
     * 
     * @return
     *     possible object is
     *     {@link DatasourceMapping }
     *     
     */
    public DatasourceMapping getDatasourceMapping() {
        return datasourceMapping;
    }

    /**
     * Sets the value of the datasourceMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link DatasourceMapping }
     *     
     */
    public void setDatasourceMapping(DatasourceMapping value) {
        this.datasourceMapping = value;
    }

    /**
     * Gets the value of the createTable property.
     * 
     * @return
     *     possible object is
     *     {@link CreateTable }
     *     
     */
    public CreateTable getCreateTable() {
        return createTable;
    }

    /**
     * Sets the value of the createTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateTable }
     *     
     */
    public void setCreateTable(CreateTable value) {
        this.createTable = value;
    }

    /**
     * Gets the value of the removeTable property.
     * 
     * @return
     *     possible object is
     *     {@link RemoveTable }
     *     
     */
    public RemoveTable getRemoveTable() {
        return removeTable;
    }

    /**
     * Sets the value of the removeTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoveTable }
     *     
     */
    public void setRemoveTable(RemoveTable value) {
        this.removeTable = value;
    }

    /**
     * Gets the value of the postTableCreate property.
     * 
     * @return
     *     possible object is
     *     {@link PostTableCreate }
     *     
     */
    public PostTableCreate getPostTableCreate() {
        return postTableCreate;
    }

    /**
     * Sets the value of the postTableCreate property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostTableCreate }
     *     
     */
    public void setPostTableCreate(PostTableCreate value) {
        this.postTableCreate = value;
    }

    /**
     * Gets the value of the rowLocking property.
     * 
     * @return
     *     possible object is
     *     {@link RowLocking }
     *     
     */
    public RowLocking getRowLocking() {
        return rowLocking;
    }

    /**
     * Sets the value of the rowLocking property.
     * 
     * @param value
     *     allowed object is
     *     {@link RowLocking }
     *     
     */
    public void setRowLocking(RowLocking value) {
        this.rowLocking = value;
    }

    /**
     * Gets the value of the pkConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link PkConstraint }
     *     
     */
    public PkConstraint getPkConstraint() {
        return pkConstraint;
    }

    /**
     * Sets the value of the pkConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link PkConstraint }
     *     
     */
    public void setPkConstraint(PkConstraint value) {
        this.pkConstraint = value;
    }

}
