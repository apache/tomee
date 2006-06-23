/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import org.openejb.jee.common.MessageDestination;
import org.openejb.jee.common.SecurityRole;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class AssemblyDescriptor {
    private String id;
    private List<SecurityRole> securityRoles = new ArrayList<SecurityRole>();
    private List<MethodPermission> methodPermissions = new ArrayList<MethodPermission>();
    private List<ContainerTransaction> containerTransactions = new ArrayList<ContainerTransaction>();
    private List<InterceptorBinding> interceptorBindings = new ArrayList<InterceptorBinding>();
    private List<MessageDestination> messageDestinations = new ArrayList<MessageDestination>();
    private ExcludeList excludeList;
    private List<ApplicationException> applicationExceptions = new ArrayList<ApplicationException>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SecurityRole> getSecurityRoles() {
        return securityRoles;
    }

    public void setSecurityRoles(List<SecurityRole> securityRoles) {
        this.securityRoles = securityRoles;
    }

    public List<MethodPermission> getMethodPermissions() {
        return methodPermissions;
    }

    public void setMethodPermissions(List<MethodPermission> methodPermissions) {
        this.methodPermissions = methodPermissions;
    }

    public List<ContainerTransaction> getContainerTransactions() {
        return containerTransactions;
    }

    public void setContainerTransactions(List<ContainerTransaction> containerTransactions) {
        this.containerTransactions = containerTransactions;
    }

    public List<InterceptorBinding> getInterceptorBindings() {
        return interceptorBindings;
    }

    public void setInterceptorBindings(List<InterceptorBinding> interceptorBindings) {
        this.interceptorBindings = interceptorBindings;
    }

    public List<MessageDestination> getMessageDestinations() {
        return messageDestinations;
    }

    public void setMessageDestinations(List<MessageDestination> messageDestinations) {
        this.messageDestinations = messageDestinations;
    }

    public ExcludeList getExcludeList() {
        return excludeList;
    }

    public void setExcludeList(ExcludeList excludeList) {
        this.excludeList = excludeList;
    }

    public List<ApplicationException> getApplicationExceptions() {
        return applicationExceptions;
    }

    public void setApplicationExceptions(List<ApplicationException> applicationExceptions) {
        this.applicationExceptions = applicationExceptions;
    }
}
