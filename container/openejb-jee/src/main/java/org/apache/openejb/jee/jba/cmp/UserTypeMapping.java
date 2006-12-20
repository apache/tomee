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

package org.apache.openejb.jee.jba.cmp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://jboss.org}java-type"/>
 *         &lt;element ref="{http://jboss.org}mapped-type"/>
 *         &lt;element ref="{http://jboss.org}mapper"/>
 *         &lt;element ref="{http://jboss.org}check-dirty-after-get" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}state-factory" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "javaType",
    "mappedType",
    "mapper",
    "checkDirtyAfterGet",
    "stateFactory"
})
@XmlRootElement(name = "user-type-mapping")
public class UserTypeMapping {

    @XmlElement(name = "java-type", required = true)
    protected JavaType javaType;
    @XmlElement(name = "mapped-type", required = true)
    protected MappedType mappedType;
    @XmlElement(required = true)
    protected Mapper mapper;
    @XmlElement(name = "check-dirty-after-get")
    protected CheckDirtyAfterGet checkDirtyAfterGet;
    @XmlElement(name = "state-factory")
    protected StateFactory stateFactory;

    /**
     * Gets the value of the javaType property.
     * 
     * @return
     *     possible object is
     *     {@link JavaType }
     *     
     */
    public JavaType getJavaType() {
        return javaType;
    }

    /**
     * Sets the value of the javaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaType }
     *     
     */
    public void setJavaType(JavaType value) {
        this.javaType = value;
    }

    /**
     * Gets the value of the mappedType property.
     * 
     * @return
     *     possible object is
     *     {@link MappedType }
     *     
     */
    public MappedType getMappedType() {
        return mappedType;
    }

    /**
     * Sets the value of the mappedType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MappedType }
     *     
     */
    public void setMappedType(MappedType value) {
        this.mappedType = value;
    }

    /**
     * Gets the value of the mapper property.
     * 
     * @return
     *     possible object is
     *     {@link Mapper }
     *     
     */
    public Mapper getMapper() {
        return mapper;
    }

    /**
     * Sets the value of the mapper property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mapper }
     *     
     */
    public void setMapper(Mapper value) {
        this.mapper = value;
    }

    /**
     * Gets the value of the checkDirtyAfterGet property.
     * 
     * @return
     *     possible object is
     *     {@link CheckDirtyAfterGet }
     *     
     */
    public CheckDirtyAfterGet getCheckDirtyAfterGet() {
        return checkDirtyAfterGet;
    }

    /**
     * Sets the value of the checkDirtyAfterGet property.
     * 
     * @param value
     *     allowed object is
     *     {@link CheckDirtyAfterGet }
     *     
     */
    public void setCheckDirtyAfterGet(CheckDirtyAfterGet value) {
        this.checkDirtyAfterGet = value;
    }

    /**
     * Gets the value of the stateFactory property.
     * 
     * @return
     *     possible object is
     *     {@link StateFactory }
     *     
     */
    public StateFactory getStateFactory() {
        return stateFactory;
    }

    /**
     * Sets the value of the stateFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link StateFactory }
     *     
     */
    public void setStateFactory(StateFactory value) {
        this.stateFactory = value;
    }

}
