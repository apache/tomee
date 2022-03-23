/*
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

package org.apache.openejb.config.sys;

import org.apache.openejb.util.SuperProperties;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string"&gt;
 *       &lt;attribute name="class-name" type="{http://www.openejb.org/Service/Configuration}ClassName" /&gt;
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="display-name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="provider-type" use="required" type="{http://www.openejb.org/Service/Configuration}ProviderTypes" /&gt;
 *       &lt;attribute name="constructor" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="factory-name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
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
    @XmlAttribute(name = "service", required = true)
    protected String service;
    @XmlAttribute(name = "types", required = false)
//    @XmlJavaTypeAdapter(ListAdapter.class)
    // for some reason when this field is type List JaxB gives us a List<List<String>>
    protected List<String> types;

    @XmlAttribute(name = "parent")
    protected String parent;

    public ServiceProvider() {
    }

    public ServiceProvider(final Class clazz, final String id, final String service) {
        this(clazz.getName(), id, service);
    }

    public ServiceProvider(final String className, final String id, final String service) {
        this.className = className;
        this.id = id;
        this.service = service;
    }

    /**
     * Gets the value of the properties property.
     *
     *
     * This accessor method returns a reference to the live Properties Object,
     * not a snapshot. Therefore any modification you make to the
     * returned Properties will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the properties property.
     *
     *
     * For example, to add a new value, do as follows:
     * <pre>
     *    getProperties().setProperty(key, value);
     * </pre>
     *
     *
     *
     */
    public Properties getProperties() {
        if (properties == null) {
            final SuperProperties sp = new SuperProperties();
            sp.setCaseInsensitive(true);
            properties = sp;
        }
        return properties;
    }

    /**
     * Gets the value of the className property.
     *
     * @return possible object is
     * {@link String }
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
    public void setClassName(final String value) {
        this.className = value;
    }

    /**
     * Gets the value of the constructor property.
     *
     * @return possible object is
     * {@link String }
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
    public void setConstructor(final String value) {
        this.constructor = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
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
    public void setDescription(final String value) {
        this.description = value;
    }

    /**
     * Gets the value of the displayName property.
     *
     * @return possible object is
     * {@link String }
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
    public void setDisplayName(final String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the factoryName property.
     *
     * @return possible object is
     * {@link String }
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
    public void setFactoryName(final String value) {
        this.factoryName = value;
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

    /**
     * Gets the value of the providerType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getService() {
        return service;
    }

    /**
     * Sets the value of the providerType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setService(final String value) {
        this.service = value;
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getTypes() {
        if (types == null) {
            types = new ArrayList<>();
        }
        return (List<String>) types;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "ServiceProvider{" +
            "id='" + id + '\'' +
            ", service='" + service + '\'' +
            '}';
    }
}
