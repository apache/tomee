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
 * Target({TYPE, METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface SequenceGenerator {
 * String name();
 * String sequenceName() default "";
 * String catalog() default "";
 * String schema() default "";
 * int initialValue() default 1;
 * int allocationSize() default 50;
 * }
 *
 *
 *
 * <p>Java class for sequence-generator complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="sequence-generator"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sequence-name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="catalog" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="schema" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="initial-value" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="allocation-size" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sequence-generator", propOrder = {
    "description"
})
public class SequenceGenerator {

    protected String description;
    @XmlAttribute(name = "allocation-size")
    protected Integer allocationSize;
    @XmlAttribute(name = "initial-value")
    protected Integer initialValue;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "sequence-name")
    protected String sequenceName;
    @XmlAttribute
    protected String catalog;
    @XmlAttribute
    protected String schema;

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
     * Gets the value of the sequenceName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSequenceName() {
        return sequenceName;
    }

    /**
     * Sets the value of the sequenceName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSequenceName(final String value) {
        this.sequenceName = value;
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

}
