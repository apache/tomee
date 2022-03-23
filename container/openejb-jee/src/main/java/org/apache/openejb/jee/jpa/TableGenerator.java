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

package org.apache.openejb.jee.jpa;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Target({TYPE, METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface TableGenerator {
 * String name();
 * String table() default "";
 * String catalog() default "";
 * String schema() default "";
 * String pkColumnName() default "";
 * String valueColumnName() default "";
 * String pkColumnValue() default "";
 * int initialValue() default 0;
 * int allocationSize() default 50;
 * UniqueConstraint[] uniqueConstraints() default {};
 * }
 *
 *
 *
 * <p>Java class for table-generator complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="table-generator"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="unique-constraint" type="{http://java.sun.com/xml/ns/persistence/orm}unique-constraint" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="table" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="catalog" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="schema" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="pk-column-name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="value-column-name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="pk-column-value" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="initial-value" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="allocation-size" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "table-generator", propOrder = {
    "description",
    "uniqueConstraint"
})
public class TableGenerator {

    protected String description;
    @XmlElement(name = "unique-constraint")
    protected List<UniqueConstraint> uniqueConstraint;
    @XmlAttribute(name = "allocation-size")
    protected Integer allocationSize;
    @XmlAttribute
    protected String catalog;
    @XmlAttribute(name = "initial-value")
    protected Integer initialValue;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "pk-column-name")
    protected String pkColumnName;
    @XmlAttribute(name = "pk-column-value")
    protected String pkColumnValue;
    @XmlAttribute
    protected String schema;
    @XmlAttribute
    protected String table;
    @XmlAttribute(name = "value-column-name")
    protected String valueColumnName;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(final String value) {
        this.description = value;
    }

    /**
     * Gets the value of the uniqueConstraint property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the uniqueConstraint property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUniqueConstraint().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link UniqueConstraint }
     */
    public List<UniqueConstraint> getUniqueConstraint() {
        if (uniqueConstraint == null) {
            uniqueConstraint = new ArrayList<UniqueConstraint>();
        }
        return this.uniqueConstraint;
    }

    /**
     * Gets the value of the allocationSize property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getAllocationSize() {
        return allocationSize;
    }

    /**
     * Sets the value of the allocationSize property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setAllocationSize(final Integer value) {
        this.allocationSize = value;
    }

    /**
     * Gets the value of the catalog property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Sets the value of the catalog property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCatalog(final String value) {
        this.catalog = value;
    }

    /**
     * Gets the value of the initialValue property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getInitialValue() {
        return initialValue;
    }

    /**
     * Sets the value of the initialValue property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setInitialValue(final Integer value) {
        this.initialValue = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the pkColumnName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPkColumnName() {
        return pkColumnName;
    }

    /**
     * Sets the value of the pkColumnName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPkColumnName(final String value) {
        this.pkColumnName = value;
    }

    /**
     * Gets the value of the pkColumnValue property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPkColumnValue() {
        return pkColumnValue;
    }

    /**
     * Sets the value of the pkColumnValue property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPkColumnValue(final String value) {
        this.pkColumnValue = value;
    }

    /**
     * Gets the value of the schema property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSchema(final String value) {
        this.schema = value;
    }

    /**
     * Gets the value of the table property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTable() {
        return table;
    }

    /**
     * Sets the value of the table property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTable(final String value) {
        this.table = value;
    }

    /**
     * Gets the value of the valueColumnName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValueColumnName() {
        return valueColumnName;
    }

    /**
     * Sets the value of the valueColumnName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValueColumnName(final String value) {
        this.valueColumnName = value;
    }

}
