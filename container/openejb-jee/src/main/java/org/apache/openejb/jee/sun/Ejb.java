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
package org.apache.openejb.jee.sun;

import org.apache.openejb.jee.KeyedCollection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ejbName",
    "jndiName",
    "ejbRef",
    "resourceRef",
    "resourceEnvRef",
    "serviceRef",
    "messageDestinationRef",
    "passByReference",
    "cmp",
    "principal",
    "mdbConnectionFactory",
    "jmsDurableSubscriptionName",
    "jmsMaxMessagesLoad",
    "iorSecurityConfig",
    "isReadOnlyBean",
    "refreshPeriodInSeconds",
    "commitOption",
    "cmtTimeoutInSeconds",
    "useThreadPoolId",
    "genClasses",
    "beanPool",
    "beanCache",
    "mdbResourceAdapter",
    "webserviceEndpoint",
    "flushAtEndOfMethod",
    "checkpointedMethods",
    "checkpointAtEndOfMethod"
})
public class Ejb {
    @XmlAttribute(name = "availability-enabled")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String availabilityEnabled;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "jndi-name")
    protected String jndiName;
    @XmlElement(name = "ejb-ref")
    protected List<EjbRef> ejbRef;
    @XmlElement(name = "resource-ref")
    protected List<ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref")
    protected List<ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "service-ref")
    protected List<ServiceRef> serviceRef;
    @XmlElement(name = "message-destination-ref")
    protected List<MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "pass-by-reference")
    protected String passByReference;
    protected Cmp cmp;
    protected Principal principal;
    @XmlElement(name = "mdb-connection-factory")
    protected MdbConnectionFactory mdbConnectionFactory;
    @XmlElement(name = "jms-durable-subscription-name")
    protected String jmsDurableSubscriptionName;
    @XmlElement(name = "jms-max-messages-load")
    protected String jmsMaxMessagesLoad;
    @XmlElement(name = "ior-security-config")
    protected IorSecurityConfig iorSecurityConfig;
    @XmlElement(name = "is-read-only-bean")
    protected String isReadOnlyBean;
    @XmlElement(name = "refresh-period-in-seconds")
    protected String refreshPeriodInSeconds;
    @XmlElement(name = "commit-option")
    protected String commitOption;
    @XmlElement(name = "cmt-timeout-in-seconds")
    protected String cmtTimeoutInSeconds;
    @XmlElement(name = "use-thread-pool-id")
    protected String useThreadPoolId;
    @XmlElement(name = "gen-classes")
    protected GenClasses genClasses;
    @XmlElement(name = "bean-pool")
    protected BeanPool beanPool;
    @XmlElement(name = "bean-cache")
    protected BeanCache beanCache;
    @XmlElement(name = "mdb-resource-adapter")
    protected MdbResourceAdapter mdbResourceAdapter;
    @XmlElement(name = "webservice-endpoint")
    protected KeyedCollection<String,WebserviceEndpoint> webserviceEndpoint;
    @XmlElement(name = "flush-at-end-of-method")
    protected FlushAtEndOfMethod flushAtEndOfMethod;
    @XmlElement(name = "checkpointed-methods")
    protected String checkpointedMethods;
    @XmlElement(name = "checkpoint-at-end-of-method")
    protected CheckpointAtEndOfMethod checkpointAtEndOfMethod;

    public String getAvailabilityEnabled() {
        return availabilityEnabled;
    }

    public void setAvailabilityEnabled(String value) {
        this.availabilityEnabled = value;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String value) {
        this.ejbName = value;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String value) {
        this.jndiName = value;
    }

    public List<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRef>();
        }
        return this.ejbRef;
    }

    public List<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRef>();
        }
        return this.resourceRef;
    }

    public List<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public List<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRef>();
        }
        return this.serviceRef;
    }

    public List<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public String getPassByReference() {
        return passByReference;
    }

    public void setPassByReference(String value) {
        this.passByReference = value;
    }

    public Cmp getCmp() {
        return cmp;
    }

    public void setCmp(Cmp value) {
        this.cmp = value;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal value) {
        this.principal = value;
    }

    public MdbConnectionFactory getMdbConnectionFactory() {
        return mdbConnectionFactory;
    }

    public void setMdbConnectionFactory(MdbConnectionFactory value) {
        this.mdbConnectionFactory = value;
    }

    public String getJmsDurableSubscriptionName() {
        return jmsDurableSubscriptionName;
    }

    public void setJmsDurableSubscriptionName(String value) {
        this.jmsDurableSubscriptionName = value;
    }

    public String getJmsMaxMessagesLoad() {
        return jmsMaxMessagesLoad;
    }

    public void setJmsMaxMessagesLoad(String value) {
        this.jmsMaxMessagesLoad = value;
    }

    public IorSecurityConfig getIorSecurityConfig() {
        return iorSecurityConfig;
    }

    public void setIorSecurityConfig(IorSecurityConfig value) {
        this.iorSecurityConfig = value;
    }

    public String getIsReadOnlyBean() {
        return isReadOnlyBean;
    }

    public void setIsReadOnlyBean(String value) {
        this.isReadOnlyBean = value;
    }

    public String getRefreshPeriodInSeconds() {
        return refreshPeriodInSeconds;
    }

    public void setRefreshPeriodInSeconds(String value) {
        this.refreshPeriodInSeconds = value;
    }

    public String getCommitOption() {
        return commitOption;
    }

    public void setCommitOption(String value) {
        this.commitOption = value;
    }

    public String getCmtTimeoutInSeconds() {
        return cmtTimeoutInSeconds;
    }

    public void setCmtTimeoutInSeconds(String value) {
        this.cmtTimeoutInSeconds = value;
    }

    public String getUseThreadPoolId() {
        return useThreadPoolId;
    }

    public void setUseThreadPoolId(String value) {
        this.useThreadPoolId = value;
    }

    public GenClasses getGenClasses() {
        return genClasses;
    }

    public void setGenClasses(GenClasses value) {
        this.genClasses = value;
    }

    public BeanPool getBeanPool() {
        return beanPool;
    }

    public void setBeanPool(BeanPool value) {
        this.beanPool = value;
    }

    public BeanCache getBeanCache() {
        return beanCache;
    }

    public void setBeanCache(BeanCache value) {
        this.beanCache = value;
    }

    public MdbResourceAdapter getMdbResourceAdapter() {
        return mdbResourceAdapter;
    }

    public void setMdbResourceAdapter(MdbResourceAdapter value) {
        this.mdbResourceAdapter = value;
    }

    public Collection<WebserviceEndpoint> getWebserviceEndpoint() {
        if (webserviceEndpoint == null) {
            webserviceEndpoint = new KeyedCollection<String,WebserviceEndpoint>();
        }
        return this.webserviceEndpoint;
    }

    public Map<String,WebserviceEndpoint> getWebserviceEndpointMap() {
        if (webserviceEndpoint == null) {
            webserviceEndpoint = new KeyedCollection<String,WebserviceEndpoint>();
        }
        return this.webserviceEndpoint.toMap();
    }

    public FlushAtEndOfMethod getFlushAtEndOfMethod() {
        return flushAtEndOfMethod;
    }

    public void setFlushAtEndOfMethod(FlushAtEndOfMethod value) {
        this.flushAtEndOfMethod = value;
    }

    public String getCheckpointedMethods() {
        return checkpointedMethods;
    }

    public void setCheckpointedMethods(String value) {
        this.checkpointedMethods = value;
    }

    public CheckpointAtEndOfMethod getCheckpointAtEndOfMethod() {
        return checkpointAtEndOfMethod;
    }

    public void setCheckpointAtEndOfMethod(CheckpointAtEndOfMethod value) {
        this.checkpointAtEndOfMethod = value;
    }
}
