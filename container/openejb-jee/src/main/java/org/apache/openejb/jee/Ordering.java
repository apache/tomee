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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * web-common_3_0.xsd
 * 
 * <p>Java class for orderingType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="orderingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="after" type="{http://java.sun.com/xml/ns/javaee}ordering-orderingType" minOccurs="0"/>
 *         &lt;element name="before" type="{http://java.sun.com/xml/ns/javaee}ordering-orderingType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderingType", propOrder = {
    "after",
    "before"
})
public class Ordering {

    protected OrderingOrdering after;
    protected OrderingOrdering before;

    public OrderingOrdering getAfter() {
        return after;
    }

    public void setAfter(OrderingOrdering value) {
        this.after = value;
    }

    public OrderingOrdering getBefore() {
        return before;
    }

    public void setBefore(OrderingOrdering value) {
        this.before = value;
    }

}
