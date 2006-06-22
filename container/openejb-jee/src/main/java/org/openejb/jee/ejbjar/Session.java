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

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Session extends EnterpriseBean {
    private List<String> businessLocal = new ArrayList<String>();
    private String serviceEndpoint;
    private SessionType sessionType;
    private NamedMethod timeoutMethod;
    private List<InitMethod> initMethods = new ArrayList<InitMethod>();
    private List<RemoveMethod> removeMethods = new ArrayList<RemoveMethod>();
    private TransactionType transactionType;
    private List<AroundInvoke> aroundInvokes = new ArrayList<AroundInvoke>();
    private List<PostActivate> postActivates = new ArrayList<PostActivate>();
    private List<PrePassivate> prePassivates = new ArrayList<PrePassivate>();

    public Session() {
    }

    public Session(String ejbName, String ejbClass) {
        super(ejbName, ejbClass);
    }


    public Session(String ejbName, String ejbClass, SessionType sessionType, TransactionType transactionType) {
        super(ejbName, ejbClass);
        this.sessionType = sessionType;
        this.transactionType = transactionType;
    }

    public List<String> getBusinessLocal() {
        return businessLocal;
    }

    public void setBusinessLocal(List<String> businessLocal) {
        this.businessLocal = businessLocal;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public NamedMethod getTimeoutMethod() {
        return timeoutMethod;
    }

    public void setTimeoutMethod(NamedMethod timeoutMethod) {
        this.timeoutMethod = timeoutMethod;
    }

    public List<InitMethod> getInitMethods() {
        return initMethods;
    }

    public void setInitMethods(List<InitMethod> initMethods) {
        this.initMethods = initMethods;
    }

    public List<RemoveMethod> getRemoveMethods() {
        return removeMethods;
    }

    public void setRemoveMethods(List<RemoveMethod> removeMethods) {
        this.removeMethods = removeMethods;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public List<AroundInvoke> getAroundInvokes() {
        return aroundInvokes;
    }

    public void setAroundInvokes(List<AroundInvoke> aroundInvokes) {
        this.aroundInvokes = aroundInvokes;
    }

    public List<PostActivate> getPostActivates() {
        return postActivates;
    }

    public void setPostActivates(List<PostActivate> postActivates) {
        this.postActivates = postActivates;
    }

    public List<PrePassivate> getPrePassivates() {
        return prePassivates;
    }

    public void setPrePassivates(List<PrePassivate> prePassivates) {
        this.prePassivates = prePassivates;
    }
}
