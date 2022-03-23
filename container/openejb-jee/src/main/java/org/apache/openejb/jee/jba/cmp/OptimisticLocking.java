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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{http://jboss.org}group-name"/&gt;
 *         &lt;element ref="{http://jboss.org}modified-strategy"/&gt;
 *         &lt;element ref="{http://jboss.org}read-strategy"/&gt;
 *         &lt;sequence&gt;
 *           &lt;choice&gt;
 *             &lt;element ref="{http://jboss.org}version-column"/&gt;
 *             &lt;element ref="{http://jboss.org}timestamp-column"/&gt;
 *             &lt;sequence&gt;
 *               &lt;element ref="{http://jboss.org}key-generator-factory"/&gt;
 *               &lt;element ref="{http://jboss.org}field-type"/&gt;
 *             &lt;/sequence&gt;
 *           &lt;/choice&gt;
 *           &lt;element ref="{http://jboss.org}field-name" minOccurs="0"/&gt;
 *           &lt;element ref="{http://jboss.org}column-name" minOccurs="0"/&gt;
 *           &lt;sequence minOccurs="0"&gt;
 *             &lt;element ref="{http://jboss.org}jdbc-type"/&gt;
 *             &lt;element ref="{http://jboss.org}sql-type"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/sequence&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "groupName",
    "modifiedStrategy",
    "readStrategy",
    "versionColumn",
    "timestampColumn",
    "keyGeneratorFactory",
    "fieldType",
    "fieldName",
    "columnName",
    "jdbcType",
    "sqlType"
})
@XmlRootElement(name = "optimistic-locking")
public class OptimisticLocking {

    @XmlElement(name = "group-name")
    protected GroupName groupName;
    @XmlElement(name = "modified-strategy")
    protected ModifiedStrategy modifiedStrategy;
    @XmlElement(name = "read-strategy")
    protected ReadStrategy readStrategy;
    @XmlElement(name = "version-column")
    protected VersionColumn versionColumn;
    @XmlElement(name = "timestamp-column")
    protected TimestampColumn timestampColumn;
    @XmlElement(name = "key-generator-factory")
    protected KeyGeneratorFactory keyGeneratorFactory;
    @XmlElement(name = "field-type")
    protected FieldType fieldType;
    @XmlElement(name = "field-name")
    protected FieldName fieldName;
    @XmlElement(name = "column-name")
    protected ColumnName columnName;
    @XmlElement(name = "jdbc-type")
    protected JdbcType jdbcType;
    @XmlElement(name = "sql-type")
    protected SqlType sqlType;

    /**
     * Gets the value of the groupName property.
     *
     * @return possible object is
     * {@link GroupName }
     */
    public GroupName getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     *
     * @param value allowed object is
     *              {@link GroupName }
     */
    public void setGroupName(final GroupName value) {
        this.groupName = value;
    }

    /**
     * Gets the value of the modifiedStrategy property.
     *
     * @return possible object is
     * {@link ModifiedStrategy }
     */
    public ModifiedStrategy getModifiedStrategy() {
        return modifiedStrategy;
    }

    /**
     * Sets the value of the modifiedStrategy property.
     *
     * @param value allowed object is
     *              {@link ModifiedStrategy }
     */
    public void setModifiedStrategy(final ModifiedStrategy value) {
        this.modifiedStrategy = value;
    }

    /**
     * Gets the value of the readStrategy property.
     *
     * @return possible object is
     * {@link ReadStrategy }
     */
    public ReadStrategy getReadStrategy() {
        return readStrategy;
    }

    /**
     * Sets the value of the readStrategy property.
     *
     * @param value allowed object is
     *              {@link ReadStrategy }
     */
    public void setReadStrategy(final ReadStrategy value) {
        this.readStrategy = value;
    }

    /**
     * Gets the value of the versionColumn property.
     *
     * @return possible object is
     * {@link VersionColumn }
     */
    public VersionColumn getVersionColumn() {
        return versionColumn;
    }

    /**
     * Sets the value of the versionColumn property.
     *
     * @param value allowed object is
     *              {@link VersionColumn }
     */
    public void setVersionColumn(final VersionColumn value) {
        this.versionColumn = value;
    }

    /**
     * Gets the value of the timestampColumn property.
     *
     * @return possible object is
     * {@link TimestampColumn }
     */
    public TimestampColumn getTimestampColumn() {
        return timestampColumn;
    }

    /**
     * Sets the value of the timestampColumn property.
     *
     * @param value allowed object is
     *              {@link TimestampColumn }
     */
    public void setTimestampColumn(final TimestampColumn value) {
        this.timestampColumn = value;
    }

    /**
     * Gets the value of the keyGeneratorFactory property.
     *
     * @return possible object is
     * {@link KeyGeneratorFactory }
     */
    public KeyGeneratorFactory getKeyGeneratorFactory() {
        return keyGeneratorFactory;
    }

    /**
     * Sets the value of the keyGeneratorFactory property.
     *
     * @param value allowed object is
     *              {@link KeyGeneratorFactory }
     */
    public void setKeyGeneratorFactory(final KeyGeneratorFactory value) {
        this.keyGeneratorFactory = value;
    }

    /**
     * Gets the value of the fieldType property.
     *
     * @return possible object is
     * {@link FieldType }
     */
    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * Sets the value of the fieldType property.
     *
     * @param value allowed object is
     *              {@link FieldType }
     */
    public void setFieldType(final FieldType value) {
        this.fieldType = value;
    }

    /**
     * Gets the value of the fieldName property.
     *
     * @return possible object is
     * {@link FieldName }
     */
    public FieldName getFieldName() {
        return fieldName;
    }

    /**
     * Sets the value of the fieldName property.
     *
     * @param value allowed object is
     *              {@link FieldName }
     */
    public void setFieldName(final FieldName value) {
        this.fieldName = value;
    }

    /**
     * Gets the value of the columnName property.
     *
     * @return possible object is
     * {@link ColumnName }
     */
    public ColumnName getColumnName() {
        return columnName;
    }

    /**
     * Sets the value of the columnName property.
     *
     * @param value allowed object is
     *              {@link ColumnName }
     */
    public void setColumnName(final ColumnName value) {
        this.columnName = value;
    }

    /**
     * Gets the value of the jdbcType property.
     *
     * @return possible object is
     * {@link JdbcType }
     */
    public JdbcType getJdbcType() {
        return jdbcType;
    }

    /**
     * Sets the value of the jdbcType property.
     *
     * @param value allowed object is
     *              {@link JdbcType }
     */
    public void setJdbcType(final JdbcType value) {
        this.jdbcType = value;
    }

    /**
     * Gets the value of the sqlType property.
     *
     * @return possible object is
     * {@link SqlType }
     */
    public SqlType getSqlType() {
        return sqlType;
    }

    /**
     * Sets the value of the sqlType property.
     *
     * @param value allowed object is
     *              {@link SqlType }
     */
    public void setSqlType(final SqlType value) {
        this.sqlType = value;
    }

}
