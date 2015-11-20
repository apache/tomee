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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface OrderColumn {
 * String name() default "";
 * boolean nullable() default true;
 * boolean insertable() default true;
 * boolean updatable() default true;
 * String columnDefinition() default "";
 * }
 * <p/>
 * <p/>
 * <p/>
 * <p>Java class for order-column complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="order-column">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="nullable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="insertable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="updatable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="column-definition" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "order-column")
public class OrderColumn {

    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Boolean nullable;
    @XmlAttribute
    protected Boolean insertable;
    @XmlAttribute
    protected Boolean updatable;
    @XmlAttribute(name = "column-definition")
    protected String columnDefinition;

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
     * Gets the value of the nullable property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isNullable() {
        return nullable;
    }

    /**
     * Sets the value of the nullable property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setNullable(final Boolean value) {
        this.nullable = value;
    }

    /**
     * Gets the value of the insertable property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isInsertable() {
        return insertable;
    }

    /**
     * Sets the value of the insertable property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setInsertable(final Boolean value) {
        this.insertable = value;
    }

    /**
     * Gets the value of the updatable property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isUpdatable() {
        return updatable;
    }

    /**
     * Sets the value of the updatable property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setUpdatable(final Boolean value) {
        this.updatable = value;
    }

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

}
