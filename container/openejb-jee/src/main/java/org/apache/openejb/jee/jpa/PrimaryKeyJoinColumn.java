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
 * public @interface PrimaryKeyJoinColumn {
 * String name() default "";
 * String referencedColumnName() default "";
 * String columnDefinition() default "";
 * }
 *
 *
 *
 * <p>Java class for primary-key-join-column complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="primary-key-join-column"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="column-definition" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="referenced-column-name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "primary-key-join-column")
public class PrimaryKeyJoinColumn {

    @XmlAttribute(name = "column-definition")
    protected String columnDefinition;
    @XmlAttribute
    protected String name;
    @XmlAttribute(name = "referenced-column-name")
    protected String referencedColumnName;

    /**
     * Gets the value of the columnDefinition property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getColumnDefinition() {
        return columnDefinition;
    }

    /**
     * Sets the value of the columnDefinition property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setColumnDefinition(final String value) {
        this.columnDefinition = value;
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
     * Gets the value of the referencedColumnName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReferencedColumnName() {
        return referencedColumnName;
    }

    /**
     * Sets the value of the referencedColumnName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReferencedColumnName(final String value) {
        this.referencedColumnName = value;
    }

}
