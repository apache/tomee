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

package org.apache.openejb.jee.jba;

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
    "invalidationGroupName",
    "invalidationManagerName"
})
@XmlRootElement(name = "cache-invalidation-config")
public class CacheInvalidationConfig {

    @XmlElement(name = "invalidation-group-name")
    protected String invalidationGroupName;
    @XmlElement(name = "invalidation-manager-name")
    protected String invalidationManagerName;

    /**
     * Gets the value of the invalidationGroupName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvalidationGroupName() {
        return invalidationGroupName;
    }

    /**
     * Sets the value of the invalidationGroupName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvalidationGroupName(String value) {
        this.invalidationGroupName = value;
    }

    /**
     * Gets the value of the invalidationManagerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvalidationManagerName() {
        return invalidationManagerName;
    }

    /**
     * Sets the value of the invalidationManagerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvalidationManagerName(String value) {
        this.invalidationManagerName = value;
    }

}
