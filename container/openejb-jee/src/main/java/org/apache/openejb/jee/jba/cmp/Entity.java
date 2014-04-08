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
 *         &lt;element ref="{http://jboss.org}ejb-name"/>
 *         &lt;element ref="{http://jboss.org}datasource" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}datasource-mapping" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}create-table" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}remove-table" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}post-table-create" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-only" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-time-out" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}row-locking" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}pk-constraint" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-ahead" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}list-cache-max" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}clean-read-ahead-on-load" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}fetch-size" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}table-name" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}cmp-field" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}load-groups" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}eager-load-group" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}lazy-load-groups" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}query" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}unknown-pk" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}entity-command" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}optimistic-locking" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}audit" minOccurs="0"/>
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
    "ejbName",
    "datasource",
    "datasourceMapping",
    "createTable",
    "removeTable",
    "postTableCreate",
    "readOnly",
    "readTimeOut",
    "rowLocking",
    "pkConstraint",
    "readAhead",
    "listCacheMax",
    "cleanReadAheadOnLoad",
    "fetchSize",
    "tableName",
    "cmpField",
    "loadGroups",
    "eagerLoadGroup",
    "lazyLoadGroups",
    "query",
    "unknownPk",
    "entityCommand",
    "optimisticLocking",
    "audit"
})
@XmlRootElement(name = "entity")
public class Entity {

    @XmlElement(name = "ejb-name", required = true)
    protected EjbName ejbName;
    protected Datasource datasource;
    @XmlElement(name = "datasource-mapping")
    protected DatasourceMapping datasourceMapping;
    @XmlElement(name = "create-table")
    protected CreateTable createTable;
    @XmlElement(name = "remove-table")
    protected RemoveTable removeTable;
    @XmlElement(name = "post-table-create")
    protected PostTableCreate postTableCreate;
    @XmlElement(name = "read-only")
    protected ReadOnly readOnly;
    @XmlElement(name = "read-time-out")
    protected ReadTimeOut readTimeOut;
    @XmlElement(name = "row-locking")
    protected RowLocking rowLocking;
    @XmlElement(name = "pk-constraint")
    protected PkConstraint pkConstraint;
    @XmlElement(name = "read-ahead")
    protected ReadAhead readAhead;
    @XmlElement(name = "list-cache-max")
    protected ListCacheMax listCacheMax;
    @XmlElement(name = "clean-read-ahead-on-load")
    protected CleanReadAheadOnLoad cleanReadAheadOnLoad;
    @XmlElement(name = "fetch-size")
    protected FetchSize fetchSize;
    @XmlElement(name = "table-name")
    protected TableName tableName;
    @XmlElement(name = "cmp-field")
    protected List<CmpField> cmpField;
    @XmlElement(name = "load-groups")
    protected LoadGroups loadGroups;
    @XmlElement(name = "eager-load-group")
    protected EagerLoadGroup eagerLoadGroup;
    @XmlElement(name = "lazy-load-groups")
    protected LazyLoadGroups lazyLoadGroups;
    protected List<Query> query;
    @XmlElement(name = "unknown-pk")
    protected UnknownPk unknownPk;
    @XmlElement(name = "entity-command")
    protected EntityCommand entityCommand;
    @XmlElement(name = "optimistic-locking")
    protected OptimisticLocking optimisticLocking;
    protected Audit audit;

    /**
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link EjbName }
     *     
     */
    public EjbName getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     * 
     * @param value
     *     allowed object is
     *     {@link EjbName }
     *     
     */
    public void setEjbName(EjbName value) {
        this.ejbName = value;
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

    /**
     * Gets the value of the readAhead property.
     * 
     * @return
     *     possible object is
     *     {@link ReadAhead }
     *     
     */
    public ReadAhead getReadAhead() {
        return readAhead;
    }

    /**
     * Sets the value of the readAhead property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReadAhead }
     *     
     */
    public void setReadAhead(ReadAhead value) {
        this.readAhead = value;
    }

    /**
     * Gets the value of the listCacheMax property.
     * 
     * @return
     *     possible object is
     *     {@link ListCacheMax }
     *     
     */
    public ListCacheMax getListCacheMax() {
        return listCacheMax;
    }

    /**
     * Sets the value of the listCacheMax property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListCacheMax }
     *     
     */
    public void setListCacheMax(ListCacheMax value) {
        this.listCacheMax = value;
    }

    /**
     * Gets the value of the cleanReadAheadOnLoad property.
     * 
     * @return
     *     possible object is
     *     {@link CleanReadAheadOnLoad }
     *     
     */
    public CleanReadAheadOnLoad getCleanReadAheadOnLoad() {
        return cleanReadAheadOnLoad;
    }

    /**
     * Sets the value of the cleanReadAheadOnLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link CleanReadAheadOnLoad }
     *     
     */
    public void setCleanReadAheadOnLoad(CleanReadAheadOnLoad value) {
        this.cleanReadAheadOnLoad = value;
    }

    /**
     * Gets the value of the fetchSize property.
     * 
     * @return
     *     possible object is
     *     {@link FetchSize }
     *     
     */
    public FetchSize getFetchSize() {
        return fetchSize;
    }

    /**
     * Sets the value of the fetchSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link FetchSize }
     *     
     */
    public void setFetchSize(FetchSize value) {
        this.fetchSize = value;
    }

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
     * Gets the value of the cmpField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmpField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmpField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmpField }
     * 
     * 
     */
    public List<CmpField> getCmpField() {
        if (cmpField == null) {
            cmpField = new ArrayList<CmpField>();
        }
        return this.cmpField;
    }

    /**
     * Gets the value of the loadGroups property.
     * 
     * @return
     *     possible object is
     *     {@link LoadGroups }
     *     
     */
    public LoadGroups getLoadGroups() {
        return loadGroups;
    }

    /**
     * Sets the value of the loadGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link LoadGroups }
     *     
     */
    public void setLoadGroups(LoadGroups value) {
        this.loadGroups = value;
    }

    /**
     * Gets the value of the eagerLoadGroup property.
     * 
     * @return
     *     possible object is
     *     {@link EagerLoadGroup }
     *     
     */
    public EagerLoadGroup getEagerLoadGroup() {
        return eagerLoadGroup;
    }

    /**
     * Sets the value of the eagerLoadGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link EagerLoadGroup }
     *     
     */
    public void setEagerLoadGroup(EagerLoadGroup value) {
        this.eagerLoadGroup = value;
    }

    /**
     * Gets the value of the lazyLoadGroups property.
     * 
     * @return
     *     possible object is
     *     {@link LazyLoadGroups }
     *     
     */
    public LazyLoadGroups getLazyLoadGroups() {
        return lazyLoadGroups;
    }

    /**
     * Sets the value of the lazyLoadGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link LazyLoadGroups }
     *     
     */
    public void setLazyLoadGroups(LazyLoadGroups value) {
        this.lazyLoadGroups = value;
    }

    /**
     * Gets the value of the query property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the query property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuery().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Query }
     * 
     * 
     */
    public List<Query> getQuery() {
        if (query == null) {
            query = new ArrayList<Query>();
        }
        return this.query;
    }

    /**
     * Gets the value of the unknownPk property.
     * 
     * @return
     *     possible object is
     *     {@link UnknownPk }
     *     
     */
    public UnknownPk getUnknownPk() {
        return unknownPk;
    }

    /**
     * Sets the value of the unknownPk property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnknownPk }
     *     
     */
    public void setUnknownPk(UnknownPk value) {
        this.unknownPk = value;
    }

    /**
     * Gets the value of the entityCommand property.
     * 
     * @return
     *     possible object is
     *     {@link EntityCommand }
     *     
     */
    public EntityCommand getEntityCommand() {
        return entityCommand;
    }

    /**
     * Sets the value of the entityCommand property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityCommand }
     *     
     */
    public void setEntityCommand(EntityCommand value) {
        this.entityCommand = value;
    }

    /**
     * Gets the value of the optimisticLocking property.
     * 
     * @return
     *     possible object is
     *     {@link OptimisticLocking }
     *     
     */
    public OptimisticLocking getOptimisticLocking() {
        return optimisticLocking;
    }

    /**
     * Sets the value of the optimisticLocking property.
     * 
     * @param value
     *     allowed object is
     *     {@link OptimisticLocking }
     *     
     */
    public void setOptimisticLocking(OptimisticLocking value) {
        this.optimisticLocking = value;
    }

    /**
     * Gets the value of the audit property.
     * 
     * @return
     *     possible object is
     *     {@link Audit }
     *     
     */
    public Audit getAudit() {
        return audit;
    }

    /**
     * Sets the value of the audit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Audit }
     *     
     */
    public void setAudit(Audit value) {
        this.audit = value;
    }

}
