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
package org.apache.openejb.jee.wls;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for persistence complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="is-modified-method-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="delay-updates-until-end-of-tx" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="finders-load-bean" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="persistence-use" type="{http://www.bea.com/ns/weblogic/90}persistence-use" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence", propOrder = {
    "isModifiedMethodName",
    "delayUpdatesUntilEndOfTx",
    "findersLoadBean",
    "persistenceUse"
})
public class Persistence {

    @XmlElement(name = "is-modified-method-name")
    protected String isModifiedMethodName;
    @XmlElement(name = "delay-updates-until-end-of-tx")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean delayUpdatesUntilEndOfTx;
    @XmlElement(name = "finders-load-bean")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean findersLoadBean;
    @XmlElement(name = "persistence-use")
    protected PersistenceUse persistenceUse;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the isModifiedMethodName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getIsModifiedMethodName() {
        return isModifiedMethodName;
    }

    /**
     * Sets the value of the isModifiedMethodName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIsModifiedMethodName(final String value) {
        this.isModifiedMethodName = value;
    }

    /**
     * Gets the value of the delayUpdatesUntilEndOfTx property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getDelayUpdatesUntilEndOfTx() {
        return delayUpdatesUntilEndOfTx;
    }

    /**
     * Sets the value of the delayUpdatesUntilEndOfTx property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setDelayUpdatesUntilEndOfTx(final Boolean value) {
        this.delayUpdatesUntilEndOfTx = value;
    }

    /**
     * Gets the value of the findersLoadBean property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getFindersLoadBean() {
        return findersLoadBean;
    }

    /**
     * Sets the value of the findersLoadBean property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setFindersLoadBean(final Boolean value) {
        this.findersLoadBean = value;
    }

    /**
     * Gets the value of the persistenceUse property.
     *
     * @return possible object is
     * {@link PersistenceUse }
     */
    public PersistenceUse getPersistenceUse() {
        return persistenceUse;
    }

    /**
     * Sets the value of the persistenceUse property.
     *
     * @param value allowed object is
     *              {@link PersistenceUse }
     */
    public void setPersistenceUse(final PersistenceUse value) {
        this.persistenceUse = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
