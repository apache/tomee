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
    "mappingProperties",
    "isOneOneCmp",
    "oneOneFinders",
    "prefetchDisabled"
})
@XmlRootElement(name = "cmp")
public class Cmp {

    @XmlElement(name = "mapping-properties")
    protected String mappingProperties;
    @XmlElement(name = "is-one-one-cmp")
    protected String isOneOneCmp;
    @XmlElement(name = "one-one-finders")
    protected OneOneFinders oneOneFinders;
    @XmlElement(name = "prefetch-disabled")
    protected PrefetchDisabled prefetchDisabled;

    /**
     * Gets the value of the mappingProperties property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMappingProperties() {
        return mappingProperties;
    }

    /**
     * Sets the value of the mappingProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMappingProperties(String value) {
        this.mappingProperties = value;
    }

    /**
     * Gets the value of the isOneOneCmp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsOneOneCmp() {
        return isOneOneCmp;
    }

    /**
     * Sets the value of the isOneOneCmp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsOneOneCmp(String value) {
        this.isOneOneCmp = value;
    }

    /**
     * Gets the value of the oneOneFinders property.
     * 
     * @return
     *     possible object is
     *     {@link OneOneFinders }
     *     
     */
    public OneOneFinders getOneOneFinders() {
        return oneOneFinders;
    }

    /**
     * Sets the value of the oneOneFinders property.
     * 
     * @param value
     *     allowed object is
     *     {@link OneOneFinders }
     *     
     */
    public void setOneOneFinders(OneOneFinders value) {
        this.oneOneFinders = value;
    }

    /**
     * Gets the value of the prefetchDisabled property.
     * 
     * @return
     *     possible object is
     *     {@link PrefetchDisabled }
     *     
     */
    public PrefetchDisabled getPrefetchDisabled() {
        return prefetchDisabled;
    }

    /**
     * Sets the value of the prefetchDisabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrefetchDisabled }
     *     
     */
    public void setPrefetchDisabled(PrefetchDisabled value) {
        this.prefetchDisabled = value;
    }

}
