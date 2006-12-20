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

package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "remoteImpl",
    "localImpl",
    "remoteHomeImpl",
    "localHomeImpl"
})
@XmlRootElement(name = "gen-classes")
public class GenClasses {

    @XmlElement(name = "remote-impl")
    protected String remoteImpl;
    @XmlElement(name = "local-impl")
    protected String localImpl;
    @XmlElement(name = "remote-home-impl")
    protected String remoteHomeImpl;
    @XmlElement(name = "local-home-impl")
    protected String localHomeImpl;

    /**
     * Gets the value of the remoteImpl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoteImpl() {
        return remoteImpl;
    }

    /**
     * Sets the value of the remoteImpl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoteImpl(String value) {
        this.remoteImpl = value;
    }

    /**
     * Gets the value of the localImpl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalImpl() {
        return localImpl;
    }

    /**
     * Sets the value of the localImpl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalImpl(String value) {
        this.localImpl = value;
    }

    /**
     * Gets the value of the remoteHomeImpl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoteHomeImpl() {
        return remoteHomeImpl;
    }

    /**
     * Sets the value of the remoteHomeImpl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoteHomeImpl(String value) {
        this.remoteHomeImpl = value;
    }

    /**
     * Gets the value of the localHomeImpl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalHomeImpl() {
        return localHomeImpl;
    }

    /**
     * Sets the value of the localHomeImpl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalHomeImpl(String value) {
        this.localHomeImpl = value;
    }

}
