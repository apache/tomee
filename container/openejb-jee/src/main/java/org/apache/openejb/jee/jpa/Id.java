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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Target({METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface Id {}
 *
 *
 *
 * <p>Java class for id complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="id"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="column" type="{http://java.sun.com/xml/ns/persistence/orm}column" minOccurs="0"/&gt;
 *         &lt;element name="generated-value" type="{http://java.sun.com/xml/ns/persistence/orm}generated-value" minOccurs="0"/&gt;
 *         &lt;element name="temporal" type="{http://java.sun.com/xml/ns/persistence/orm}temporal" minOccurs="0"/&gt;
 *         &lt;element name="table-generator" type="{http://java.sun.com/xml/ns/persistence/orm}table-generator" minOccurs="0"/&gt;
 *         &lt;element name="sequence-generator" type="{http://java.sun.com/xml/ns/persistence/orm}sequence-generator" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "id", propOrder = {
    "column",
    "generatedValue",
    "temporal",
    "tableGenerator",
    "sequenceGenerator"
})
public class Id implements Field {

    protected Column column;
    @XmlElement(name = "generated-value")
    protected GeneratedValue generatedValue;
    protected TemporalType temporal;
    @XmlElement(name = "table-generator")
    protected TableGenerator tableGenerator;
    @XmlElement(name = "sequence-generator")
    protected SequenceGenerator sequenceGenerator;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected AccessType access;

    public Id() {
    }

    public Id(final String name) {
        this.name = name;
    }

    public Id(final String name, final String columnName) {
        this.name = name;
        this.column = new Column(columnName);
    }

    /**
     * Gets the value of the column property.
     *
     * @return possible object is
     * {@link Column }
     */
    public Column getColumn() {
        return column;
    }

    /**
     * Sets the value of the column property.
     *
     * @param value allowed object is
     *              {@link Column }
     */
    public void setColumn(final Column value) {
        this.column = value;
    }

    /**
     * Gets the value of the generatedValue property.
     *
     * @return possible object is
     * {@link GeneratedValue }
     */
    public GeneratedValue getGeneratedValue() {
        return generatedValue;
    }

    /**
     * Sets the value of the generatedValue property.
     *
     * @param value allowed object is
     *              {@link GeneratedValue }
     */
    public void setGeneratedValue(final GeneratedValue value) {
        this.generatedValue = value;
    }

    /**
     * Gets the value of the temporal property.
     *
     * @return possible object is
     * {@link TemporalType }
     */
    public TemporalType getTemporal() {
        return temporal;
    }

    /**
     * Sets the value of the temporal property.
     *
     * @param value allowed object is
     *              {@link TemporalType }
     */
    public void setTemporal(final TemporalType value) {
        this.temporal = value;
    }

    /**
     * Gets the value of the tableGenerator property.
     *
     * @return possible object is
     * {@link TableGenerator }
     */
    public TableGenerator getTableGenerator() {
        return tableGenerator;
    }

    /**
     * Sets the value of the tableGenerator property.
     *
     * @param value allowed object is
     *              {@link TableGenerator }
     */
    public void setTableGenerator(final TableGenerator value) {
        this.tableGenerator = value;
    }

    /**
     * Gets the value of the sequenceGenerator property.
     *
     * @return possible object is
     * {@link SequenceGenerator }
     */
    public SequenceGenerator getSequenceGenerator() {
        return sequenceGenerator;
    }

    /**
     * Sets the value of the sequenceGenerator property.
     *
     * @param value allowed object is
     *              {@link SequenceGenerator }
     */
    public void setSequenceGenerator(final SequenceGenerator value) {
        this.sequenceGenerator = value;
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

    public Object getKey() {
        return name;
    }

    /**
     * Gets the value of the access property.
     *
     * @return possible object is
     * {@link AccessType }
     */
    public AccessType getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     *
     * @param value allowed object is
     *              {@link AccessType }
     */
    public void setAccess(final AccessType value) {
        this.access = value;
    }
}
