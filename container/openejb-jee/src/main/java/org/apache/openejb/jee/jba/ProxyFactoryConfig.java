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
    "clientInterceptors",
    "endpointInterceptors",
    "webClassLoader",
    "activationConfig",
    "jmsProviderAdapterJNDI",
    "serverSessionPoolFactoryJNDI",
    "createJBossMQDestination",
    "minimumSize",
    "maximumSize",
    "keepAliveMillis",
    "maxMessages",
    "mdbConfig",
    "poa",
    "registerEjbsInJnpContext",
    "jnpContext",
    "interfaceRepositorySupported"
})
@XmlRootElement(name = "proxy-factory-config")
public class ProxyFactoryConfig {

    @XmlElement(name = "client-interceptors")
    protected ClientInterceptors clientInterceptors;
    @XmlElement(name = "endpoint-interceptors")
    protected EndpointInterceptors endpointInterceptors;
    @XmlElement(name = "web-class-loader")
    protected String webClassLoader;
    @XmlElement(name = "activation-config")
    protected ActivationConfig activationConfig;
    @XmlElement(name = "JMSProviderAdapterJNDI")
    protected String jmsProviderAdapterJNDI;
    @XmlElement(name = "ServerSessionPoolFactoryJNDI")
    protected String serverSessionPoolFactoryJNDI;
    @XmlElement(name = "CreateJBossMQDestination")
    protected String createJBossMQDestination;
    @XmlElement(name = "MinimumSize")
    protected String minimumSize;
    @XmlElement(name = "MaximumSize")
    protected String maximumSize;
    @XmlElement(name = "KeepAliveMillis")
    protected String keepAliveMillis;
    @XmlElement(name = "MaxMessages")
    protected String maxMessages;
    @XmlElement(name = "MDBConfig")
    protected MDBConfig mdbConfig;
    protected String poa;
    @XmlElement(name = "register-ejbs-in-jnp-context")
    protected String registerEjbsInJnpContext;
    @XmlElement(name = "jnp-context")
    protected String jnpContext;
    @XmlElement(name = "interface-repository-supported")
    protected String interfaceRepositorySupported;

    /**
     * Gets the value of the clientInterceptors property.
     * 
     * @return
     *     possible object is
     *     {@link ClientInterceptors }
     *     
     */
    public ClientInterceptors getClientInterceptors() {
        return clientInterceptors;
    }

    /**
     * Sets the value of the clientInterceptors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClientInterceptors }
     *     
     */
    public void setClientInterceptors(ClientInterceptors value) {
        this.clientInterceptors = value;
    }

    /**
     * Gets the value of the endpointInterceptors property.
     * 
     * @return
     *     possible object is
     *     {@link EndpointInterceptors }
     *     
     */
    public EndpointInterceptors getEndpointInterceptors() {
        return endpointInterceptors;
    }

    /**
     * Sets the value of the endpointInterceptors property.
     * 
     * @param value
     *     allowed object is
     *     {@link EndpointInterceptors }
     *     
     */
    public void setEndpointInterceptors(EndpointInterceptors value) {
        this.endpointInterceptors = value;
    }

    /**
     * Gets the value of the webClassLoader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebClassLoader() {
        return webClassLoader;
    }

    /**
     * Sets the value of the webClassLoader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebClassLoader(String value) {
        this.webClassLoader = value;
    }

    /**
     * Gets the value of the activationConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationConfig }
     *     
     */
    public ActivationConfig getActivationConfig() {
        return activationConfig;
    }

    /**
     * Sets the value of the activationConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationConfig }
     *     
     */
    public void setActivationConfig(ActivationConfig value) {
        this.activationConfig = value;
    }

    /**
     * Gets the value of the jmsProviderAdapterJNDI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJMSProviderAdapterJNDI() {
        return jmsProviderAdapterJNDI;
    }

    /**
     * Sets the value of the jmsProviderAdapterJNDI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJMSProviderAdapterJNDI(String value) {
        this.jmsProviderAdapterJNDI = value;
    }

    /**
     * Gets the value of the serverSessionPoolFactoryJNDI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServerSessionPoolFactoryJNDI() {
        return serverSessionPoolFactoryJNDI;
    }

    /**
     * Sets the value of the serverSessionPoolFactoryJNDI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServerSessionPoolFactoryJNDI(String value) {
        this.serverSessionPoolFactoryJNDI = value;
    }

    /**
     * Gets the value of the createJBossMQDestination property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreateJBossMQDestination() {
        return createJBossMQDestination;
    }

    /**
     * Sets the value of the createJBossMQDestination property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateJBossMQDestination(String value) {
        this.createJBossMQDestination = value;
    }

    /**
     * Gets the value of the minimumSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinimumSize() {
        return minimumSize;
    }

    /**
     * Sets the value of the minimumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinimumSize(String value) {
        this.minimumSize = value;
    }

    /**
     * Gets the value of the maximumSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the value of the maximumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaximumSize(String value) {
        this.maximumSize = value;
    }

    /**
     * Gets the value of the keepAliveMillis property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeepAliveMillis() {
        return keepAliveMillis;
    }

    /**
     * Sets the value of the keepAliveMillis property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeepAliveMillis(String value) {
        this.keepAliveMillis = value;
    }

    /**
     * Gets the value of the maxMessages property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxMessages() {
        return maxMessages;
    }

    /**
     * Sets the value of the maxMessages property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxMessages(String value) {
        this.maxMessages = value;
    }

    /**
     * Gets the value of the mdbConfig property.
     * 
     * @return
     *     possible object is
     *     {@link MDBConfig }
     *     
     */
    public MDBConfig getMDBConfig() {
        return mdbConfig;
    }

    /**
     * Sets the value of the mdbConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link MDBConfig }
     *     
     */
    public void setMDBConfig(MDBConfig value) {
        this.mdbConfig = value;
    }

    /**
     * Gets the value of the poa property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPoa() {
        return poa;
    }

    /**
     * Sets the value of the poa property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPoa(String value) {
        this.poa = value;
    }

    /**
     * Gets the value of the registerEjbsInJnpContext property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegisterEjbsInJnpContext() {
        return registerEjbsInJnpContext;
    }

    /**
     * Sets the value of the registerEjbsInJnpContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegisterEjbsInJnpContext(String value) {
        this.registerEjbsInJnpContext = value;
    }

    /**
     * Gets the value of the jnpContext property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJnpContext() {
        return jnpContext;
    }

    /**
     * Sets the value of the jnpContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJnpContext(String value) {
        this.jnpContext = value;
    }

    /**
     * Gets the value of the interfaceRepositorySupported property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInterfaceRepositorySupported() {
        return interfaceRepositorySupported;
    }

    /**
     * Sets the value of the interfaceRepositorySupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInterfaceRepositorySupported(String value) {
        this.interfaceRepositorySupported = value;
    }

}
