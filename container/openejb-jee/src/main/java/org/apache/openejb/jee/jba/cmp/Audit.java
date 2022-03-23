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
 *         &lt;element ref="{http://jboss.org}created-by" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}created-time" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}updated-by" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}updated-time" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "createdBy",
    "createdTime",
    "updatedBy",
    "updatedTime"
})
@XmlRootElement(name = "audit")
public class Audit {

    @XmlElement(name = "created-by")
    protected CreatedBy createdBy;
    @XmlElement(name = "created-time")
    protected CreatedTime createdTime;
    @XmlElement(name = "updated-by")
    protected UpdatedBy updatedBy;
    @XmlElement(name = "updated-time")
    protected UpdatedTime updatedTime;

    /**
     * Gets the value of the createdBy property.
     *
     * @return possible object is
     * {@link CreatedBy }
     */
    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the value of the createdBy property.
     *
     * @param value allowed object is
     *              {@link CreatedBy }
     */
    public void setCreatedBy(final CreatedBy value) {
        this.createdBy = value;
    }

    /**
     * Gets the value of the createdTime property.
     *
     * @return possible object is
     * {@link CreatedTime }
     */
    public CreatedTime getCreatedTime() {
        return createdTime;
    }

    /**
     * Sets the value of the createdTime property.
     *
     * @param value allowed object is
     *              {@link CreatedTime }
     */
    public void setCreatedTime(final CreatedTime value) {
        this.createdTime = value;
    }

    /**
     * Gets the value of the updatedBy property.
     *
     * @return possible object is
     * {@link UpdatedBy }
     */
    public UpdatedBy getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the value of the updatedBy property.
     *
     * @param value allowed object is
     *              {@link UpdatedBy }
     */
    public void setUpdatedBy(final UpdatedBy value) {
        this.updatedBy = value;
    }

    /**
     * Gets the value of the updatedTime property.
     *
     * @return possible object is
     * {@link UpdatedTime }
     */
    public UpdatedTime getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Sets the value of the updatedTime property.
     *
     * @param value allowed object is
     *              {@link UpdatedTime }
     */
    public void setUpdatedTime(final UpdatedTime value) {
        this.updatedTime = value;
    }

}
