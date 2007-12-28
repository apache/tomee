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

import org.apache.openejb.config.Service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Properties;

/**
 * <p>Java class for Service complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="Service">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jar" type="{http://www.openejb.org/System/Configuration}JarFileLocation" />
 *       &lt;attribute name="provider" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
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


    protected AbstractService(String id) {
        this(id, null, null);
    }

    protected AbstractService(String id, String type) {
        this.id = id;
        this.type = type;
    }

    protected AbstractService(String id, String type, String provider) {
        this.id = id;
        this.provider = provider;
        this.type = type;
    }

    protected AbstractService() {
    }

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
     * Gets the value of the jar property.
     *
     * @return possible object is
     *         {@link String }
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
    public void setJar(String value) {
        this.jar = value;
    }

    /**
     * Gets the value of the provider property.
     *
     * @return possible object is
     *         {@link String }
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
    public void setProvider(String value) {
        this.provider = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
