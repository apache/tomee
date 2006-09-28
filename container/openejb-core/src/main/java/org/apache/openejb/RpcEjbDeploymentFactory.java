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
package org.apache.openejb;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.security.auth.Subject;

/**
 * @version $Revision$ $Date$
 */
public abstract class RpcEjbDeploymentFactory {
    protected String containerId;
    protected String ejbName;
    protected String homeInterfaceName;
    protected String remoteInterfaceName;
    protected String localHomeInterfaceName;
    protected String localInterfaceName;
    protected String beanClassName;
    protected EjbContainer ejbContainer;
    protected String[] jndiNames;
    protected String[] localJndiNames;
    protected boolean securityEnabled;
    protected String policyContextId;
    protected Subject runAs;
    protected SortedMap transactionPolicies;
    protected Map componentContext;
    protected Set unshareableResources;
    protected Set applicationManagedSecurityResources;
    protected ClassLoader classLoader;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public String getHomeInterfaceName() {
        return homeInterfaceName;
    }

    public void setHomeInterfaceName(String homeInterfaceName) {
        this.homeInterfaceName = homeInterfaceName;
    }

    public String getRemoteInterfaceName() {
        return remoteInterfaceName;
    }

    public void setRemoteInterfaceName(String remoteInterfaceName) {
        this.remoteInterfaceName = remoteInterfaceName;
    }

    public String getLocalHomeInterfaceName() {
        return localHomeInterfaceName;
    }

    public void setLocalHomeInterfaceName(String localHomeInterfaceName) {
        this.localHomeInterfaceName = localHomeInterfaceName;
    }

    public String getLocalInterfaceName() {
        return localInterfaceName;
    }

    public void setLocalInterfaceName(String localInterfaceName) {
        this.localInterfaceName = localInterfaceName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public EjbContainer getEjbContainer() {
        return ejbContainer;
    }

    public void setEjbContainer(EjbContainer ejbContainer) {
        this.ejbContainer = ejbContainer;
    }

    public String[] getJndiNames() {
        return jndiNames;
    }

    public void setJndiNames(String[] jndiNames) {
        this.jndiNames = jndiNames;
    }

    public String[] getLocalJndiNames() {
        return localJndiNames;
    }

    public void setLocalJndiNames(String[] localJndiNames) {
        this.localJndiNames = localJndiNames;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public String getPolicyContextId() {
        return policyContextId;
    }

    public void setPolicyContextId(String policyContextId) {
        this.policyContextId = policyContextId;
    }

    public Subject getRunAs() {
        return runAs;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public SortedMap getTransactionPolicies() {
        return transactionPolicies;
    }

    public void setTransactionPolicies(SortedMap transactionPolicies) {
        this.transactionPolicies = transactionPolicies;
    }

    public Map getComponentContext() {
        return componentContext;
    }

    public void setComponentContext(Map componentContext) {
        this.componentContext = componentContext;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public void setUnshareableResources(Set unshareableResources) {
        this.unshareableResources = unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Class loadClass(String className, String description) throws ClassNotFoundException {
        if (className == null) {
            return null;
        }
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Unable to load " + description + " " + className);
        }
    }

}
