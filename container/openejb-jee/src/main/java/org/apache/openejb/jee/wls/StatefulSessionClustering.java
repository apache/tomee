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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for stateful-session-clustering complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="stateful-session-clustering">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="home-is-clusterable" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="home-load-algorithm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="home-call-router-class-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="use-serverside-stubs" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="replication-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stateful-session-clustering", propOrder = {
    "homeIsClusterable",
    "homeLoadAlgorithm",
    "homeCallRouterClassName",
    "useServersideStubs",
    "replicationType"
})
public class StatefulSessionClustering {

    @XmlElement(name = "home-is-clusterable")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean homeIsClusterable;
    @XmlElement(name = "home-load-algorithm")
    protected String homeLoadAlgorithm;
    @XmlElement(name = "home-call-router-class-name")
    protected String homeCallRouterClassName;
    @XmlElement(name = "use-serverside-stubs")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean useServersideStubs;
    @XmlElement(name = "replication-type")
    protected String replicationType;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the homeIsClusterable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getHomeIsClusterable() {
        return homeIsClusterable;
    }

    /**
     * Sets the value of the homeIsClusterable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHomeIsClusterable(Boolean value) {
        this.homeIsClusterable = value;
    }

    /**
     * Gets the value of the homeLoadAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomeLoadAlgorithm() {
        return homeLoadAlgorithm;
    }

    /**
     * Sets the value of the homeLoadAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomeLoadAlgorithm(String value) {
        this.homeLoadAlgorithm = value;
    }

    /**
     * Gets the value of the homeCallRouterClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomeCallRouterClassName() {
        return homeCallRouterClassName;
    }

    /**
     * Sets the value of the homeCallRouterClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomeCallRouterClassName(String value) {
        this.homeCallRouterClassName = value;
    }

    /**
     * Gets the value of the useServersideStubs property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getUseServersideStubs() {
        return useServersideStubs;
    }

    /**
     * Sets the value of the useServersideStubs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseServersideStubs(Boolean value) {
        this.useServersideStubs = value;
    }

    /**
     * Gets the value of the replicationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplicationType() {
        return replicationType;
    }

    /**
     * Sets the value of the replicationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplicationType(String value) {
        this.replicationType = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
