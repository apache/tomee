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
 * <p>Java class for resource-env-description complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-env-description"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="resource-env-ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;group ref="{http://www.bea.com/ns/weblogic/90}resource-lookup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-env-description", propOrder = {
    "resourceEnvRefName",
    "jndiName",
    "resourceLink"
})
public class ResourceEnvDescription {

    @XmlElement(name = "resource-env-ref-name", required = true)
    protected String resourceEnvRefName;
    @XmlElement(name = "jndi-name")
    protected String jndiName;
    @XmlElement(name = "resource-link")
    protected String resourceLink;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the resourceEnvRefName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getResourceEnvRefName() {
        return resourceEnvRefName;
    }

    /**
     * Sets the value of the resourceEnvRefName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResourceEnvRefName(final String value) {
        this.resourceEnvRefName = value;
    }

    /**
     * Gets the value of the jndiName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * Sets the value of the jndiName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJndiName(final String value) {
        this.jndiName = value;
    }

    /**
     * Gets the value of the resourceLink property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getResourceLink() {
        return resourceLink;
    }

    /**
     * Sets the value of the resourceLink property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResourceLink(final String value) {
        this.resourceLink = value;
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
