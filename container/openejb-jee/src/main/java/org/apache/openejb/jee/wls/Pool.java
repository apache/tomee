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

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for pool complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="pool"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="max-beans-in-free-pool" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="initial-beans-in-free-pool" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="idle-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pool", propOrder = {
    "maxBeansInFreePool",
    "initialBeansInFreePool",
    "idleTimeoutSeconds"
})
public class Pool {

    @XmlElement(name = "max-beans-in-free-pool")
    protected BigInteger maxBeansInFreePool;
    @XmlElement(name = "initial-beans-in-free-pool")
    protected BigInteger initialBeansInFreePool;
    @XmlElement(name = "idle-timeout-seconds")
    protected BigInteger idleTimeoutSeconds;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the maxBeansInFreePool property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getMaxBeansInFreePool() {
        return maxBeansInFreePool;
    }

    /**
     * Sets the value of the maxBeansInFreePool property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setMaxBeansInFreePool(final BigInteger value) {
        this.maxBeansInFreePool = value;
    }

    /**
     * Gets the value of the initialBeansInFreePool property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getInitialBeansInFreePool() {
        return initialBeansInFreePool;
    }

    /**
     * Sets the value of the initialBeansInFreePool property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setInitialBeansInFreePool(final BigInteger value) {
        this.initialBeansInFreePool = value;
    }

    /**
     * Gets the value of the idleTimeoutSeconds property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getIdleTimeoutSeconds() {
        return idleTimeoutSeconds;
    }

    /**
     * Sets the value of the idleTimeoutSeconds property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setIdleTimeoutSeconds(final BigInteger value) {
        this.idleTimeoutSeconds = value;
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
