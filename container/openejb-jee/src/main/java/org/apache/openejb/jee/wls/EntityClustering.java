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
 * <p>Java class for entity-clustering complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="entity-clustering"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="home-is-clusterable" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="home-load-algorithm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="home-call-router-class-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="use-serverside-stubs" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity-clustering", propOrder = {
    "homeIsClusterable",
    "homeLoadAlgorithm",
    "homeCallRouterClassName",
    "useServersideStubs"
})
public class EntityClustering {

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
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the homeIsClusterable property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getHomeIsClusterable() {
        return homeIsClusterable;
    }

    /**
     * Sets the value of the homeIsClusterable property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setHomeIsClusterable(final Boolean value) {
        this.homeIsClusterable = value;
    }

    /**
     * Gets the value of the homeLoadAlgorithm property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getHomeLoadAlgorithm() {
        return homeLoadAlgorithm;
    }

    /**
     * Sets the value of the homeLoadAlgorithm property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHomeLoadAlgorithm(final String value) {
        this.homeLoadAlgorithm = value;
    }

    /**
     * Gets the value of the homeCallRouterClassName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getHomeCallRouterClassName() {
        return homeCallRouterClassName;
    }

    /**
     * Sets the value of the homeCallRouterClassName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHomeCallRouterClassName(final String value) {
        this.homeCallRouterClassName = value;
    }

    /**
     * Gets the value of the useServersideStubs property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getUseServersideStubs() {
        return useServersideStubs;
    }

    /**
     * Sets the value of the useServersideStubs property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setUseServersideStubs(final Boolean value) {
        this.useServersideStubs = value;
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
