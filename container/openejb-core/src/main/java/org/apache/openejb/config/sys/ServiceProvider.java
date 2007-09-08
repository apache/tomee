/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.sys;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="class-name" type="{http://www.openejb.org/Service/Configuration}ClassName" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="display-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="provider-type" use="required" type="{http://www.openejb.org/Service/Configuration}ProviderTypes" />
 *       &lt;attribute name="constructor" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="factory-name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "ServiceProvider")
public class ServiceProvider {

    @XmlValue
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;
    @XmlAttribute(name = "class-name")
    protected String className;
    @XmlAttribute(name = "constructor")
    protected String constructor;
    @XmlAttribute
    protected String description;
    @XmlAttribute(name = "display-name")
    protected String displayName;
    @XmlAttribute(name = "factory-name")
    protected String factoryName;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute(name = "provider-type", required = true)
    protected String providerType;
    @XmlAttribute(name = "service-types", required = false)
    @XmlJavaTypeAdapter(ListAdapter.class)
    protected List<String> serviceTypes;

    /**
     * Gets the value of the properties property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live Properties Object,
     * not a snapshot. Therefore any modification you make to the
     * returned Properties will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the properties property.
     * <p/>
     * <p/>
     * For example, to add a new value, do as follows:
     * <pre>
     *    getProperties().setProperty(key, value);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     */
    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    /**
     * Gets the value of the className property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the constructor property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConstructor() {
        return constructor;
    }

    /**
     * Sets the value of the constructor property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConstructor(String value) {
        this.constructor = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the displayName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the factoryName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFactoryName() {
        return factoryName;
    }

    /**
     * Sets the value of the factoryName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFactoryName(String value) {
        this.factoryName = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     *         {@link String }
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
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the providerType property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * Sets the value of the providerType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProviderType(String value) {
        this.providerType = value;
    }

    public List<String> getServiceTypes() {
        if (serviceTypes == null){
            serviceTypes = new ArrayList<String>();
        }
        return serviceTypes;
    }
}
