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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ejb-ql-query complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ejb-ql-query">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="weblogic-ql" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="caching-name" type="{http://www.bea.com/ns/weblogic/90}caching-name" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-ql-query", propOrder = {
    "weblogicQl",
    "groupName",
    "cachingName"
})
public class EjbQlQuery {

    @XmlElement(name = "weblogic-ql")
    protected String weblogicQl;
    @XmlElement(name = "group-name")
    protected String groupName;
    @XmlElement(name = "caching-name")
    protected CachingName cachingName;

    /**
     * Gets the value of the weblogicQl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWeblogicQl() {
        return weblogicQl;
    }

    /**
     * Sets the value of the weblogicQl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWeblogicQl(String value) {
        this.weblogicQl = value;
    }

    /**
     * Gets the value of the groupName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupName(String value) {
        this.groupName = value;
    }

    /**
     * Gets the value of the cachingName property.
     * 
     * @return
     *     possible object is
     *     {@link CachingName }
     *     
     */
    public CachingName getCachingName() {
        return cachingName;
    }

    /**
     * Sets the value of the cachingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link CachingName }
     *     
     */
    public void setCachingName(CachingName value) {
        this.cachingName = value;
    }

}
