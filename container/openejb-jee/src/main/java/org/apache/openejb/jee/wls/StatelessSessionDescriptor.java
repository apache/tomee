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
 * <p>Java class for stateless-session-descriptor complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="stateless-session-descriptor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pool" type="{http://www.bea.com/ns/weblogic/90}pool" minOccurs="0"/&gt;
 *         &lt;element name="timer-descriptor" type="{http://www.bea.com/ns/weblogic/90}timer-descriptor" minOccurs="0"/&gt;
 *         &lt;element name="stateless-clustering" type="{http://www.bea.com/ns/weblogic/90}stateless-clustering" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stateless-session-descriptor", propOrder = {
    "pool",
    "timerDescriptor",
    "statelessClustering"
})
public class StatelessSessionDescriptor {

    protected Pool pool;
    @XmlElement(name = "timer-descriptor")
    protected TimerDescriptor timerDescriptor;
    @XmlElement(name = "stateless-clustering")
    protected StatelessClustering statelessClustering;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the pool property.
     *
     * @return possible object is
     * {@link Pool }
     */
    public Pool getPool() {
        return pool;
    }

    /**
     * Sets the value of the pool property.
     *
     * @param value allowed object is
     *              {@link Pool }
     */
    public void setPool(final Pool value) {
        this.pool = value;
    }

    /**
     * Gets the value of the timerDescriptor property.
     *
     * @return possible object is
     * {@link TimerDescriptor }
     */
    public TimerDescriptor getTimerDescriptor() {
        return timerDescriptor;
    }

    /**
     * Sets the value of the timerDescriptor property.
     *
     * @param value allowed object is
     *              {@link TimerDescriptor }
     */
    public void setTimerDescriptor(final TimerDescriptor value) {
        this.timerDescriptor = value;
    }

    /**
     * Gets the value of the statelessClustering property.
     *
     * @return possible object is
     * {@link StatelessClustering }
     */
    public StatelessClustering getStatelessClustering() {
        return statelessClustering;
    }

    /**
     * Sets the value of the statelessClustering property.
     *
     * @param value allowed object is
     *              {@link StatelessClustering }
     */
    public void setStatelessClustering(final StatelessClustering value) {
        this.statelessClustering = value;
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
