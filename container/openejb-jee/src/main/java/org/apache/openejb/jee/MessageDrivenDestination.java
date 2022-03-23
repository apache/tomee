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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * no longer represented in schema?
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "destinationType",
    "subscriptionDurability"
})
@XmlRootElement(name = "message-driven-destination")
public class MessageDrivenDestination {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlElement(name = "destination-type", required = true)
    protected DestinationType destinationType;
    @XmlElement(name = "subscription-durability")
    protected SubscriptionDurability subscriptionDurability;

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

    /**
     * Gets the value of the destinationType property.
     *
     * @return possible object is
     * {@link DestinationType }
     */
    public DestinationType getDestinationType() {
        return destinationType;
    }

    /**
     * Sets the value of the destinationType property.
     *
     * @param value allowed object is
     *              {@link DestinationType }
     */
    public void setDestinationType(final DestinationType value) {
        this.destinationType = value;
    }

    /**
     * Gets the value of the subscriptionDurability property.
     *
     * @return possible object is
     * {@link SubscriptionDurability }
     */
    public SubscriptionDurability getSubscriptionDurability() {
        return subscriptionDurability;
    }

    /**
     * Sets the value of the subscriptionDurability property.
     *
     * @param value allowed object is
     *              {@link SubscriptionDurability }
     */
    public void setSubscriptionDurability(final SubscriptionDurability value) {
        this.subscriptionDurability = value;
    }
}