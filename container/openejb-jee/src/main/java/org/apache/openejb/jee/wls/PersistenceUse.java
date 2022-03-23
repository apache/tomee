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
 * <p>Java class for persistence-use complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-use"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="type-identifier" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="type-version" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="type-storage" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence-use", propOrder = {
    "typeIdentifier",
    "typeVersion",
    "typeStorage"
})
public class PersistenceUse {

    @XmlElement(name = "type-identifier", required = true)
    protected String typeIdentifier;
    @XmlElement(name = "type-version", required = true)
    protected String typeVersion;
    @XmlElement(name = "type-storage", required = true)
    protected String typeStorage;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the typeIdentifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTypeIdentifier() {
        return typeIdentifier;
    }

    /**
     * Sets the value of the typeIdentifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTypeIdentifier(final String value) {
        this.typeIdentifier = value;
    }

    /**
     * Gets the value of the typeVersion property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTypeVersion() {
        return typeVersion;
    }

    /**
     * Sets the value of the typeVersion property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTypeVersion(final String value) {
        this.typeVersion = value;
    }

    /**
     * Gets the value of the typeStorage property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTypeStorage() {
        return typeStorage;
    }

    /**
     * Sets the value of the typeStorage property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTypeStorage(final String value) {
        this.typeStorage = value;
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
