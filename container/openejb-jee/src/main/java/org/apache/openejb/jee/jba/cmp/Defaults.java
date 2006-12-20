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
 *         &lt;element ref="{http://jboss.org}datasource" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}datasource-mapping" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}create-table" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}alter-table" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}remove-table" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}post-table-create" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-only" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-time-out" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}row-locking" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}pk-constraint" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}fk-constraint" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}preferred-relation-mapping" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-ahead" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}list-cache-max" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}clean-read-ahead-on-load" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}fetch-size" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}unknown-pk" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}entity-command" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}ql-compiler" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}throw-runtime-exceptions" minOccurs="0"/>
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
    "datasource",
    "datasourceMapping",
    "createTable",
    "alterTable",
    "removeTable",
    "postTableCreate",
    "readOnly",
    "readTimeOut",
    "rowLocking",
    "pkConstraint",
    "fkConstraint",
    "preferredRelationMapping",
    "readAhead",
    "listCacheMax",
    "cleanReadAheadOnLoad",
    "fetchSize",
    "unknownPk",
    "entityCommand",
    "qlCompiler",
    "throwRuntimeExceptions"
})
@XmlRootElement(name = "defaults")
public class Defaults {

    protected Datasource datasource;
    @XmlElement(name = "datasource-mapping")
    protected DatasourceMapping datasourceMapping;
    @XmlElement(name = "create-table")
    protected CreateTable createTable;
    @XmlElement(name = "alter-table")
    protected AlterTable alterTable;
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
    @XmlElement(name = "fk-constraint")
    protected FkConstraint fkConstraint;
    @XmlElement(name = "preferred-relation-mapping")
    protected PreferredRelationMapping preferredRelationMapping;
    @XmlElement(name = "read-ahead")
    protected ReadAhead readAhead;
    @XmlElement(name = "list-cache-max")
    protected ListCacheMax listCacheMax;
    @XmlElement(name = "clean-read-ahead-on-load")
    protected CleanReadAheadOnLoad cleanReadAheadOnLoad;
    @XmlElement(name = "fetch-size")
    protected FetchSize fetchSize;
    @XmlElement(name = "unknown-pk")
    protected UnknownPk unknownPk;
    @XmlElement(name = "entity-command")
    protected EntityCommand entityCommand;
    @XmlElement(name = "ql-compiler")
    protected QlCompiler qlCompiler;
    @XmlElement(name = "throw-runtime-exceptions")
    protected ThrowRuntimeExceptions throwRuntimeExceptions;

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
     * Gets the value of the alterTable property.
     * 
     * @return
     *     possible object is
     *     {@link AlterTable }
     *     
     */
    public AlterTable getAlterTable() {
        return alterTable;
    }

    /**
     * Sets the value of the alterTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlterTable }
     *     
     */
    public void setAlterTable(AlterTable value) {
        this.alterTable = value;
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
     * Gets the value of the fkConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link FkConstraint }
     *     
     */
    public FkConstraint getFkConstraint() {
        return fkConstraint;
    }

    /**
     * Sets the value of the fkConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link FkConstraint }
     *     
     */
    public void setFkConstraint(FkConstraint value) {
        this.fkConstraint = value;
    }

    /**
     * Gets the value of the preferredRelationMapping property.
     * 
     * @return
     *     possible object is
     *     {@link PreferredRelationMapping }
     *     
     */
    public PreferredRelationMapping getPreferredRelationMapping() {
        return preferredRelationMapping;
    }

    /**
     * Sets the value of the preferredRelationMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferredRelationMapping }
     *     
     */
    public void setPreferredRelationMapping(PreferredRelationMapping value) {
        this.preferredRelationMapping = value;
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
     * Gets the value of the qlCompiler property.
     * 
     * @return
     *     possible object is
     *     {@link QlCompiler }
     *     
     */
    public QlCompiler getQlCompiler() {
        return qlCompiler;
    }

    /**
     * Sets the value of the qlCompiler property.
     * 
     * @param value
     *     allowed object is
     *     {@link QlCompiler }
     *     
     */
    public void setQlCompiler(QlCompiler value) {
        this.qlCompiler = value;
    }

    /**
     * Gets the value of the throwRuntimeExceptions property.
     * 
     * @return
     *     possible object is
     *     {@link ThrowRuntimeExceptions }
     *     
     */
    public ThrowRuntimeExceptions getThrowRuntimeExceptions() {
        return throwRuntimeExceptions;
    }

    /**
     * Sets the value of the throwRuntimeExceptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThrowRuntimeExceptions }
     *     
     */
    public void setThrowRuntimeExceptions(ThrowRuntimeExceptions value) {
        this.throwRuntimeExceptions = value;
    }

}
