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
import jakarta.xml.bind.annotation.XmlType;


/**
 * Target({METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface Basic {
 * FetchType fetch() default EAGER;
 * boolean optional() default true;
 * }
 *
 *
 *
 * <p>Java class for basic complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="basic"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="column" type="{http://java.sun.com/xml/ns/persistence/orm}column" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="lob" type="{http://java.sun.com/xml/ns/persistence/orm}lob" minOccurs="0"/&gt;
 *           &lt;element name="temporal" type="{http://java.sun.com/xml/ns/persistence/orm}temporal" minOccurs="0"/&gt;
 *           &lt;element name="enumerated" type="{http://java.sun.com/xml/ns/persistence/orm}enumerated" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fetch" type="{http://java.sun.com/xml/ns/persistence/orm}fetch-type" /&gt;
 *       &lt;attribute name="optional" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "basic", propOrder = {
    "column",
    "lob",
    "temporal",
    "enumerated"
})
public class Basic implements Field {

    protected Column column;
    protected Lob lob;
    protected TemporalType temporal;
    protected EnumType enumerated;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected FetchType fetch;
    @XmlAttribute
    protected Boolean optional;
    @XmlAttribute
    protected AccessType access;

    public Basic() {
    }

    public Basic(final String name) {
        this.name = name;
    }

    public Basic(final String name, final String columnName) {
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
     * Gets the value of the lob property.
     *
     * @return possible object is
     * {@link Lob }
     */
    public Lob getLob() {
        return lob;
    }

    /**
     * Sets the value of the lob property.
     *
     * @param value allowed object is
     *              {@link Lob }
     */
    public void setLob(final Lob value) {
        this.lob = value;
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
     * Gets the value of the enumerated property.
     *
     * @return possible object is
     * {@link EnumType }
     */
    public EnumType getEnumerated() {
        return enumerated;
    }

    /**
     * Sets the value of the enumerated property.
     *
     * @param value allowed object is
     *              {@link EnumType }
     */
    public void setEnumerated(final EnumType value) {
        this.enumerated = value;
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
     * Gets the value of the fetch property.
     *
     * @return possible object is
     * {@link FetchType }
     */
    public FetchType getFetch() {
        return fetch;
    }

    /**
     * Sets the value of the fetch property.
     *
     * @param value allowed object is
     *              {@link FetchType }
     */
    public void setFetch(final FetchType value) {
        this.fetch = value;
    }

    /**
     * Gets the value of the optional property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isOptional() {
        return optional;
    }

    /**
     * Sets the value of the optional property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setOptional(final Boolean value) {
        this.optional = value;
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

    public Object getKey() {
        return name;
    }
}
