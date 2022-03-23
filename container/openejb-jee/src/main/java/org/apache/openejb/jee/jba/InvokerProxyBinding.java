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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "invokerMbean",
    "proxyFactory",
    "proxyFactoryConfig"
})
@XmlRootElement(name = "invoker-proxy-binding")
public class InvokerProxyBinding {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(name = "invoker-mbean", required = true)
    protected String invokerMbean;
    @XmlElement(name = "proxy-factory", required = true)
    protected String proxyFactory;
    @XmlElement(name = "proxy-factory-config", required = true)
    protected ProxyFactoryConfig proxyFactoryConfig;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the invokerMbean property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getInvokerMbean() {
        return invokerMbean;
    }

    /**
     * Sets the value of the invokerMbean property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInvokerMbean(final String value) {
        this.invokerMbean = value;
    }

    /**
     * Gets the value of the proxyFactory property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProxyFactory() {
        return proxyFactory;
    }

    /**
     * Sets the value of the proxyFactory property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProxyFactory(final String value) {
        this.proxyFactory = value;
    }

    /**
     * Gets the value of the proxyFactoryConfig property.
     *
     * @return possible object is
     * {@link ProxyFactoryConfig }
     */
    public ProxyFactoryConfig getProxyFactoryConfig() {
        return proxyFactoryConfig;
    }

    /**
     * Sets the value of the proxyFactoryConfig property.
     *
     * @param value allowed object is
     *              {@link ProxyFactoryConfig }
     */
    public void setProxyFactoryConfig(final ProxyFactoryConfig value) {
        this.proxyFactoryConfig = value;
    }

}
