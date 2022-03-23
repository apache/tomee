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

import org.apache.openejb.config.Service;
import org.apache.openejb.util.SuperProperties;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Objects;
import java.util.Properties;

/**
 * <p>Java class for Service complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Service"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string"&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="jar" type="{http://www.openejb.org/System/Configuration}JarFileLocation" /&gt;
 *       &lt;attribute name="provider" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Service")
public abstract class AbstractService implements Service {
    @XmlValue
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;

    @XmlAttribute(required = true)
    protected String id;

    @XmlAttribute
    protected String jar;

    @XmlAttribute
    protected String provider;

    @XmlAttribute
    protected String type;

    @XmlAttribute
    protected String classpath;

    @XmlAttribute(name = "classpath-api")
    protected String classpathAPI;

    /**
     * Mutually exclusive with 'provider'
     */
    @XmlAttribute(name = "class-name")
    protected String className;

    /**
     * Mutually exclusive with 'provider'
     */
    @XmlAttribute(name = "constructor")
    protected String constructor;

    /**
     * Mutually exclusive with 'provider'
     */
    @XmlAttribute(name = "factory-name")
    protected String factoryName;

    @XmlAttribute(name = "properties-provider")
    private String propertiesProvider;

    @XmlAttribute(name = "template")
    private String template;

    protected AbstractService(final String id) {
        this(id, null, null);
    }

    protected AbstractService(final String id, final String type) {
        this.id = id;
        this.type = type;
    }

    protected AbstractService(final String id, final String type, final String provider) {
        this.id = id;
        this.provider = provider;
        this.type = type;
    }

    protected AbstractService() {
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
            properties = new SuperProperties();
        }
        return properties;
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
     * Gets the value of the jar property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getJar() {
        return jar;
    }

    /**
     * Sets the value of the jar property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJar(final String value) {
        this.jar = value;
    }

    /**
     * Gets the value of the provider property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the value of the provider property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProvider(final String value) {
        this.provider = value;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public String getClasspathAPI() {
        return classpathAPI;
    }

    public void setClasspathAPI(final String classpathAPI) {
        this.classpathAPI = classpathAPI;
    }

    public String getConstructor() {
        return constructor;
    }

    public void setConstructor(final String constructor) {
        this.constructor = constructor;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(final String factoryName) {
        this.factoryName = factoryName;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(final String classpath) {
        this.classpath = classpath;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractService)) {
            return false;
        }

        final AbstractService that = (AbstractService) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }
        if (!Objects.equals(jar, that.jar)) {
            return false;
        }
        if (!Objects.equals(type, that.type)) {
            return false;
        }
        if (!Objects.equals(provider, that.provider)) {
            return false;
        }
        if (!Objects.equals(properties, that.properties)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = properties != null ? properties.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (jar != null ? jar.hashCode() : 0);
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public void setPropertiesProvider(final String propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    @Override
    public String getPropertiesProvider() {
        return propertiesProvider;
    }
}
