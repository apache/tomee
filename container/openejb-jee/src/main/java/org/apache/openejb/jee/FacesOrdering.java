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
 * web-facesconfig_2_0.xsd
 *
 * <p>Java class for faces-config-orderingType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-orderingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="after" type="{http://java.sun.com/xml/ns/javaee}faces-config-ordering-orderingType" minOccurs="0"/&gt;
 *         &lt;element name="before" type="{http://java.sun.com/xml/ns/javaee}faces-config-ordering-orderingType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-orderingType", propOrder = {
    "after",
    "before"
})
public class FacesOrdering {

    protected FacesOrderingOrdering after;
    protected FacesOrderingOrdering before;

    /**
     * Gets the value of the after property.
     *
     * @return possible object is
     * {@link FacesOrderingOrdering }
     */
    public FacesOrderingOrdering getAfter() {
        return after;
    }

    /**
     * Sets the value of the after property.
     *
     * @param value allowed object is
     *              {@link FacesOrderingOrdering }
     */
    public void setAfter(final FacesOrderingOrdering value) {
        this.after = value;
    }

    /**
     * Gets the value of the before property.
     *
     * @return possible object is
     * {@link FacesOrderingOrdering }
     */
    public FacesOrderingOrdering getBefore() {
        return before;
    }

    /**
     * Sets the value of the before property.
     *
     * @param value allowed object is
     *              {@link FacesOrderingOrdering }
     */
    public void setBefore(final FacesOrderingOrdering value) {
        this.before = value;
    }

}
