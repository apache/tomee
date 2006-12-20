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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "containerName",
    "callLogging",
    "invokerProxyBindingName",
    "syncOnCommitOnly",
    "insertAfterEjbPostCreate",
    "callEjbStoreOnClean",
    "storeNotFlushed",
    "containerInterceptors",
    "instancePool",
    "instanceCache",
    "persistenceManager",
    "webClassLoader",
    "lockingPolicy",
    "containerCacheConf",
    "containerPoolConf",
    "commitOption",
    "optiondRefreshRate",
    "securityDomain",
    "clusterConfig",
    "depends"
})
@XmlRootElement(name = "container-configuration")
public class ContainerConfiguration {

    @XmlAttribute(name = "extends")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String _extends;
    @XmlElement(name = "container-name", required = true)
    protected String containerName;
    @XmlElement(name = "call-logging")
    protected String callLogging;
    @XmlElement(name = "invoker-proxy-binding-name")
    protected String invokerProxyBindingName;
    @XmlElement(name = "sync-on-commit-only")
    protected String syncOnCommitOnly;
    @XmlElement(name = "insert-after-ejb-post-create")
    protected String insertAfterEjbPostCreate;
    @XmlElement(name = "call-ejb-store-on-clean")
    protected String callEjbStoreOnClean;
    @XmlElement(name = "store-not-flushed")
    protected String storeNotFlushed;
    @XmlElement(name = "container-interceptors")
    protected ContainerInterceptors containerInterceptors;
    @XmlElement(name = "instance-pool")
    protected String instancePool;
    @XmlElement(name = "instance-cache")
    protected String instanceCache;
    @XmlElement(name = "persistence-manager")
    protected String persistenceManager;
    @XmlElement(name = "web-class-loader")
    protected String webClassLoader;
    @XmlElement(name = "locking-policy")
    protected String lockingPolicy;
    @XmlElement(name = "container-cache-conf")
    protected ContainerCacheConf containerCacheConf;
    @XmlElement(name = "container-pool-conf")
    protected ContainerPoolConf containerPoolConf;
    @XmlElement(name = "commit-option")
    protected String commitOption;
    @XmlElement(name = "optiond-refresh-rate")
    protected String optiondRefreshRate;
    @XmlElement(name = "security-domain")
    protected String securityDomain;
    @XmlElement(name = "cluster-config")
    protected ClusterConfig clusterConfig;
    protected List<Depends> depends;

    /**
     * Gets the value of the extends property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtends() {
        return _extends;
    }

    /**
     * Sets the value of the extends property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtends(String value) {
        this._extends = value;
    }

    /**
     * Gets the value of the containerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Sets the value of the containerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContainerName(String value) {
        this.containerName = value;
    }

    /**
     * Gets the value of the callLogging property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallLogging() {
        return callLogging;
    }

    /**
     * Sets the value of the callLogging property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallLogging(String value) {
        this.callLogging = value;
    }

    /**
     * Gets the value of the invokerProxyBindingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvokerProxyBindingName() {
        return invokerProxyBindingName;
    }

    /**
     * Sets the value of the invokerProxyBindingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvokerProxyBindingName(String value) {
        this.invokerProxyBindingName = value;
    }

    /**
     * Gets the value of the syncOnCommitOnly property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSyncOnCommitOnly() {
        return syncOnCommitOnly;
    }

    /**
     * Sets the value of the syncOnCommitOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSyncOnCommitOnly(String value) {
        this.syncOnCommitOnly = value;
    }

    /**
     * Gets the value of the insertAfterEjbPostCreate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsertAfterEjbPostCreate() {
        return insertAfterEjbPostCreate;
    }

    /**
     * Sets the value of the insertAfterEjbPostCreate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsertAfterEjbPostCreate(String value) {
        this.insertAfterEjbPostCreate = value;
    }

    /**
     * Gets the value of the callEjbStoreOnClean property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallEjbStoreOnClean() {
        return callEjbStoreOnClean;
    }

    /**
     * Sets the value of the callEjbStoreOnClean property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallEjbStoreOnClean(String value) {
        this.callEjbStoreOnClean = value;
    }

    /**
     * Gets the value of the storeNotFlushed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStoreNotFlushed() {
        return storeNotFlushed;
    }

    /**
     * Sets the value of the storeNotFlushed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStoreNotFlushed(String value) {
        this.storeNotFlushed = value;
    }

    /**
     * Gets the value of the containerInterceptors property.
     * 
     * @return
     *     possible object is
     *     {@link ContainerInterceptors }
     *     
     */
    public ContainerInterceptors getContainerInterceptors() {
        return containerInterceptors;
    }

    /**
     * Sets the value of the containerInterceptors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContainerInterceptors }
     *     
     */
    public void setContainerInterceptors(ContainerInterceptors value) {
        this.containerInterceptors = value;
    }

    /**
     * Gets the value of the instancePool property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstancePool() {
        return instancePool;
    }

    /**
     * Sets the value of the instancePool property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstancePool(String value) {
        this.instancePool = value;
    }

    /**
     * Gets the value of the instanceCache property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceCache() {
        return instanceCache;
    }

    /**
     * Sets the value of the instanceCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceCache(String value) {
        this.instanceCache = value;
    }

    /**
     * Gets the value of the persistenceManager property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPersistenceManager() {
        return persistenceManager;
    }

    /**
     * Sets the value of the persistenceManager property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPersistenceManager(String value) {
        this.persistenceManager = value;
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
     * Gets the value of the lockingPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockingPolicy() {
        return lockingPolicy;
    }

    /**
     * Sets the value of the lockingPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockingPolicy(String value) {
        this.lockingPolicy = value;
    }

    /**
     * Gets the value of the containerCacheConf property.
     * 
     * @return
     *     possible object is
     *     {@link ContainerCacheConf }
     *     
     */
    public ContainerCacheConf getContainerCacheConf() {
        return containerCacheConf;
    }

    /**
     * Sets the value of the containerCacheConf property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContainerCacheConf }
     *     
     */
    public void setContainerCacheConf(ContainerCacheConf value) {
        this.containerCacheConf = value;
    }

    /**
     * Gets the value of the containerPoolConf property.
     * 
     * @return
     *     possible object is
     *     {@link ContainerPoolConf }
     *     
     */
    public ContainerPoolConf getContainerPoolConf() {
        return containerPoolConf;
    }

    /**
     * Sets the value of the containerPoolConf property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContainerPoolConf }
     *     
     */
    public void setContainerPoolConf(ContainerPoolConf value) {
        this.containerPoolConf = value;
    }

    /**
     * Gets the value of the commitOption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommitOption() {
        return commitOption;
    }

    /**
     * Sets the value of the commitOption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommitOption(String value) {
        this.commitOption = value;
    }

    /**
     * Gets the value of the optiondRefreshRate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOptiondRefreshRate() {
        return optiondRefreshRate;
    }

    /**
     * Sets the value of the optiondRefreshRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOptiondRefreshRate(String value) {
        this.optiondRefreshRate = value;
    }

    /**
     * Gets the value of the securityDomain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurityDomain() {
        return securityDomain;
    }

    /**
     * Sets the value of the securityDomain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityDomain(String value) {
        this.securityDomain = value;
    }

    /**
     * Gets the value of the clusterConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterConfig }
     *     
     */
    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    /**
     * Sets the value of the clusterConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterConfig }
     *     
     */
    public void setClusterConfig(ClusterConfig value) {
        this.clusterConfig = value;
    }

    /**
     * Gets the value of the depends property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the depends property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDepends().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Depends }
     * 
     * 
     */
    public List<Depends> getDepends() {
        if (depends == null) {
            depends = new ArrayList<Depends>();
        }
        return this.depends;
    }

}
