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
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://jboss.org}name"/&gt;
 *         &lt;element ref="{http://jboss.org}row-locking-template"/&gt;
 *         &lt;element ref="{http://jboss.org}pk-constraint-template"/&gt;
 *         &lt;element ref="{http://jboss.org}fk-constraint-template"/&gt;
 *         &lt;element ref="{http://jboss.org}auto-increment-template" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}add-column-template" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}alter-column-template" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}drop-column-template" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}alias-header-prefix"/&gt;
 *         &lt;element ref="{http://jboss.org}alias-header-suffix"/&gt;
 *         &lt;element ref="{http://jboss.org}alias-max-length"/&gt;
 *         &lt;element ref="{http://jboss.org}subquery-supported"/&gt;
 *         &lt;element ref="{http://jboss.org}true-mapping"/&gt;
 *         &lt;element ref="{http://jboss.org}false-mapping"/&gt;
 *         &lt;element ref="{http://jboss.org}function-mapping" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}mapping" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "rowLockingTemplate",
    "pkConstraintTemplate",
    "fkConstraintTemplate",
    "autoIncrementTemplate",
    "addColumnTemplate",
    "alterColumnTemplate",
    "dropColumnTemplate",
    "aliasHeaderPrefix",
    "aliasHeaderSuffix",
    "aliasMaxLength",
    "subquerySupported",
    "trueMapping",
    "falseMapping",
    "functionMapping",
    "mapping"
})
@XmlRootElement(name = "type-mapping")
public class TypeMapping {

    @XmlElement(required = true)
    protected Name name;
    @XmlElement(name = "row-locking-template", required = true)
    protected RowLockingTemplate rowLockingTemplate;
    @XmlElement(name = "pk-constraint-template", required = true)
    protected PkConstraintTemplate pkConstraintTemplate;
    @XmlElement(name = "fk-constraint-template", required = true)
    protected FkConstraintTemplate fkConstraintTemplate;
    @XmlElement(name = "auto-increment-template")
    protected AutoIncrementTemplate autoIncrementTemplate;
    @XmlElement(name = "add-column-template")
    protected AddColumnTemplate addColumnTemplate;
    @XmlElement(name = "alter-column-template")
    protected AlterColumnTemplate alterColumnTemplate;
    @XmlElement(name = "drop-column-template")
    protected DropColumnTemplate dropColumnTemplate;
    @XmlElement(name = "alias-header-prefix", required = true)
    protected AliasHeaderPrefix aliasHeaderPrefix;
    @XmlElement(name = "alias-header-suffix", required = true)
    protected AliasHeaderSuffix aliasHeaderSuffix;
    @XmlElement(name = "alias-max-length", required = true)
    protected AliasMaxLength aliasMaxLength;
    @XmlElement(name = "subquery-supported", required = true)
    protected SubquerySupported subquerySupported;
    @XmlElement(name = "true-mapping", required = true)
    protected TrueMapping trueMapping;
    @XmlElement(name = "false-mapping", required = true)
    protected FalseMapping falseMapping;
    @XmlElement(name = "function-mapping")
    protected List<FunctionMapping> functionMapping;
    @XmlElement(required = true)
    protected List<Mapping> mapping;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link Name }
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link Name }
     */
    public void setName(final Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the rowLockingTemplate property.
     *
     * @return possible object is
     * {@link RowLockingTemplate }
     */
    public RowLockingTemplate getRowLockingTemplate() {
        return rowLockingTemplate;
    }

    /**
     * Sets the value of the rowLockingTemplate property.
     *
     * @param value allowed object is
     *              {@link RowLockingTemplate }
     */
    public void setRowLockingTemplate(final RowLockingTemplate value) {
        this.rowLockingTemplate = value;
    }

    /**
     * Gets the value of the pkConstraintTemplate property.
     *
     * @return possible object is
     * {@link PkConstraintTemplate }
     */
    public PkConstraintTemplate getPkConstraintTemplate() {
        return pkConstraintTemplate;
    }

    /**
     * Sets the value of the pkConstraintTemplate property.
     *
     * @param value allowed object is
     *              {@link PkConstraintTemplate }
     */
    public void setPkConstraintTemplate(final PkConstraintTemplate value) {
        this.pkConstraintTemplate = value;
    }

    /**
     * Gets the value of the fkConstraintTemplate property.
     *
     * @return possible object is
     * {@link FkConstraintTemplate }
     */
    public FkConstraintTemplate getFkConstraintTemplate() {
        return fkConstraintTemplate;
    }

    /**
     * Sets the value of the fkConstraintTemplate property.
     *
     * @param value allowed object is
     *              {@link FkConstraintTemplate }
     */
    public void setFkConstraintTemplate(final FkConstraintTemplate value) {
        this.fkConstraintTemplate = value;
    }

    /**
     * Gets the value of the autoIncrementTemplate property.
     *
     * @return possible object is
     * {@link AutoIncrementTemplate }
     */
    public AutoIncrementTemplate getAutoIncrementTemplate() {
        return autoIncrementTemplate;
    }

    /**
     * Sets the value of the autoIncrementTemplate property.
     *
     * @param value allowed object is
     *              {@link AutoIncrementTemplate }
     */
    public void setAutoIncrementTemplate(final AutoIncrementTemplate value) {
        this.autoIncrementTemplate = value;
    }

    /**
     * Gets the value of the addColumnTemplate property.
     *
     * @return possible object is
     * {@link AddColumnTemplate }
     */
    public AddColumnTemplate getAddColumnTemplate() {
        return addColumnTemplate;
    }

    /**
     * Sets the value of the addColumnTemplate property.
     *
     * @param value allowed object is
     *              {@link AddColumnTemplate }
     */
    public void setAddColumnTemplate(final AddColumnTemplate value) {
        this.addColumnTemplate = value;
    }

    /**
     * Gets the value of the alterColumnTemplate property.
     *
     * @return possible object is
     * {@link AlterColumnTemplate }
     */
    public AlterColumnTemplate getAlterColumnTemplate() {
        return alterColumnTemplate;
    }

    /**
     * Sets the value of the alterColumnTemplate property.
     *
     * @param value allowed object is
     *              {@link AlterColumnTemplate }
     */
    public void setAlterColumnTemplate(final AlterColumnTemplate value) {
        this.alterColumnTemplate = value;
    }

    /**
     * Gets the value of the dropColumnTemplate property.
     *
     * @return possible object is
     * {@link DropColumnTemplate }
     */
    public DropColumnTemplate getDropColumnTemplate() {
        return dropColumnTemplate;
    }

    /**
     * Sets the value of the dropColumnTemplate property.
     *
     * @param value allowed object is
     *              {@link DropColumnTemplate }
     */
    public void setDropColumnTemplate(final DropColumnTemplate value) {
        this.dropColumnTemplate = value;
    }

    /**
     * Gets the value of the aliasHeaderPrefix property.
     *
     * @return possible object is
     * {@link AliasHeaderPrefix }
     */
    public AliasHeaderPrefix getAliasHeaderPrefix() {
        return aliasHeaderPrefix;
    }

    /**
     * Sets the value of the aliasHeaderPrefix property.
     *
     * @param value allowed object is
     *              {@link AliasHeaderPrefix }
     */
    public void setAliasHeaderPrefix(final AliasHeaderPrefix value) {
        this.aliasHeaderPrefix = value;
    }

    /**
     * Gets the value of the aliasHeaderSuffix property.
     *
     * @return possible object is
     * {@link AliasHeaderSuffix }
     */
    public AliasHeaderSuffix getAliasHeaderSuffix() {
        return aliasHeaderSuffix;
    }

    /**
     * Sets the value of the aliasHeaderSuffix property.
     *
     * @param value allowed object is
     *              {@link AliasHeaderSuffix }
     */
    public void setAliasHeaderSuffix(final AliasHeaderSuffix value) {
        this.aliasHeaderSuffix = value;
    }

    /**
     * Gets the value of the aliasMaxLength property.
     *
     * @return possible object is
     * {@link AliasMaxLength }
     */
    public AliasMaxLength getAliasMaxLength() {
        return aliasMaxLength;
    }

    /**
     * Sets the value of the aliasMaxLength property.
     *
     * @param value allowed object is
     *              {@link AliasMaxLength }
     */
    public void setAliasMaxLength(final AliasMaxLength value) {
        this.aliasMaxLength = value;
    }

    /**
     * Gets the value of the subquerySupported property.
     *
     * @return possible object is
     * {@link SubquerySupported }
     */
    public SubquerySupported getSubquerySupported() {
        return subquerySupported;
    }

    /**
     * Sets the value of the subquerySupported property.
     *
     * @param value allowed object is
     *              {@link SubquerySupported }
     */
    public void setSubquerySupported(final SubquerySupported value) {
        this.subquerySupported = value;
    }

    /**
     * Gets the value of the trueMapping property.
     *
     * @return possible object is
     * {@link TrueMapping }
     */
    public TrueMapping getTrueMapping() {
        return trueMapping;
    }

    /**
     * Sets the value of the trueMapping property.
     *
     * @param value allowed object is
     *              {@link TrueMapping }
     */
    public void setTrueMapping(final TrueMapping value) {
        this.trueMapping = value;
    }

    /**
     * Gets the value of the falseMapping property.
     *
     * @return possible object is
     * {@link FalseMapping }
     */
    public FalseMapping getFalseMapping() {
        return falseMapping;
    }

    /**
     * Sets the value of the falseMapping property.
     *
     * @param value allowed object is
     *              {@link FalseMapping }
     */
    public void setFalseMapping(final FalseMapping value) {
        this.falseMapping = value;
    }

    /**
     * Gets the value of the functionMapping property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the functionMapping property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFunctionMapping().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FunctionMapping }
     */
    public List<FunctionMapping> getFunctionMapping() {
        if (functionMapping == null) {
            functionMapping = new ArrayList<FunctionMapping>();
        }
        return this.functionMapping;
    }

    /**
     * Gets the value of the mapping property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapping property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapping().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Mapping }
     */
    public List<Mapping> getMapping() {
        if (mapping == null) {
            mapping = new ArrayList<Mapping>();
        }
        return this.mapping;
    }

}
