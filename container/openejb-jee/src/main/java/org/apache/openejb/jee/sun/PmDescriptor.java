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
    "pmIdentifier",
    "pmVersion",
    "pmConfig",
    "pmClassGenerator",
    "pmMappingFactory"
})
@XmlRootElement(name = "pm-descriptor")
public class PmDescriptor {

    @XmlElement(name = "pm-identifier", required = true)
    protected String pmIdentifier;
    @XmlElement(name = "pm-version", required = true)
    protected String pmVersion;
    @XmlElement(name = "pm-config")
    protected String pmConfig;
    @XmlElement(name = "pm-class-generator")
    protected String pmClassGenerator;
    @XmlElement(name = "pm-mapping-factory")
    protected String pmMappingFactory;

    /**
     * Gets the value of the pmIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmIdentifier() {
        return pmIdentifier;
    }

    /**
     * Sets the value of the pmIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmIdentifier(String value) {
        this.pmIdentifier = value;
    }

    /**
     * Gets the value of the pmVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmVersion() {
        return pmVersion;
    }

    /**
     * Sets the value of the pmVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmVersion(String value) {
        this.pmVersion = value;
    }

    /**
     * Gets the value of the pmConfig property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmConfig() {
        return pmConfig;
    }

    /**
     * Sets the value of the pmConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmConfig(String value) {
        this.pmConfig = value;
    }

    /**
     * Gets the value of the pmClassGenerator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmClassGenerator() {
        return pmClassGenerator;
    }

    /**
     * Sets the value of the pmClassGenerator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmClassGenerator(String value) {
        this.pmClassGenerator = value;
    }

    /**
     * Gets the value of the pmMappingFactory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmMappingFactory() {
        return pmMappingFactory;
    }

    /**
     * Sets the value of the pmMappingFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmMappingFactory(String value) {
        this.pmMappingFactory = value;
    }

}
