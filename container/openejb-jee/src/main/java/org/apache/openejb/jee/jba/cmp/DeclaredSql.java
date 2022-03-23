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
 *         &lt;element ref="{http://jboss.org}select" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}from" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}where" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}order" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}other" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "select",
    "from",
    "where",
    "order",
    "other"
})
@XmlRootElement(name = "declared-sql")
public class DeclaredSql {

    protected Select select;
    protected From from;
    protected Where where;
    protected Order order;
    protected Other other;

    /**
     * Gets the value of the select property.
     *
     * @return possible object is
     * {@link Select }
     */
    public Select getSelect() {
        return select;
    }

    /**
     * Sets the value of the select property.
     *
     * @param value allowed object is
     *              {@link Select }
     */
    public void setSelect(final Select value) {
        this.select = value;
    }

    /**
     * Gets the value of the from property.
     *
     * @return possible object is
     * {@link From }
     */
    public From getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     *
     * @param value allowed object is
     *              {@link From }
     */
    public void setFrom(final From value) {
        this.from = value;
    }

    /**
     * Gets the value of the where property.
     *
     * @return possible object is
     * {@link Where }
     */
    public Where getWhere() {
        return where;
    }

    /**
     * Sets the value of the where property.
     *
     * @param value allowed object is
     *              {@link Where }
     */
    public void setWhere(final Where value) {
        this.where = value;
    }

    /**
     * Gets the value of the order property.
     *
     * @return possible object is
     * {@link Order }
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     *
     * @param value allowed object is
     *              {@link Order }
     */
    public void setOrder(final Order value) {
        this.order = value;
    }

    /**
     * Gets the value of the other property.
     *
     * @return possible object is
     * {@link Other }
     */
    public Other getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     *
     * @param value allowed object is
     *              {@link Other }
     */
    public void setOther(final Other value) {
        this.other = value;
    }

}
