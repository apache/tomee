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
    "minimumSize",
    "maximumSize",
    "strictMaximumSize",
    "strictTimeout"
})
@XmlRootElement(name = "container-pool-conf")
public class ContainerPoolConf {

    @XmlElement(name = "MinimumSize")
    protected String minimumSize;
    @XmlElement(name = "MaximumSize")
    protected String maximumSize;
    protected String strictMaximumSize;
    protected String strictTimeout;

    /**
     * Gets the value of the minimumSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinimumSize() {
        return minimumSize;
    }

    /**
     * Sets the value of the minimumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinimumSize(String value) {
        this.minimumSize = value;
    }

    /**
     * Gets the value of the maximumSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the value of the maximumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaximumSize(String value) {
        this.maximumSize = value;
    }

    /**
     * Gets the value of the strictMaximumSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrictMaximumSize() {
        return strictMaximumSize;
    }

    /**
     * Sets the value of the strictMaximumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrictMaximumSize(String value) {
        this.strictMaximumSize = value;
    }

    /**
     * Gets the value of the strictTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrictTimeout() {
        return strictTimeout;
    }

    /**
     * Sets the value of the strictTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrictTimeout(String value) {
        this.strictTimeout = value;
    }

}
