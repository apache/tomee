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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collection;


/**
 * The assembly-descriptorType defines
 * application-assembly information.
 * <p/>
 * The application-assembly information consists of the
 * following parts: the definition of security roles, the
 * definition of method permissions, the definition of
 * transaction attributes for enterprise beans with
 * container-managed transaction demarcation, the definition
 * of interceptor bindings, a list of
 * methods to be excluded from being invoked, and a list of
 * exception types that should be treated as application exceptions.
 * <p/>
 * All the parts are optional in the sense that they are
 * omitted if the lists represented by them are empty.
 * <p/>
 * Providing an assembly-descriptor in the deployment
 * descriptor is optional for the ejb-jar file producer.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assembly-descriptorType", propOrder = {
        "securityRole",
        "methodPermission",
        "containerTransaction",
        "containerConcurrency",
        "methodSchedule",
        "interceptorBinding",
        "messageDestination",
        "excludeList",
        "applicationException"
        })
public class AssemblyDescriptor {

    @XmlElement(name = "security-role", required = true)
    protected List<SecurityRole> securityRole;
    @XmlElement(name = "method-permission", required = true)
    protected List<MethodPermission> methodPermission;
    @XmlElement(name = "container-transaction", required = true)
    protected List<ContainerTransaction> containerTransaction;
    @XmlElement(name = "container-concurrency", required = true)
    protected List<ContainerConcurrency> containerConcurrency;
    @XmlElement(name = "method-schedule", required = true)
    protected List<MethodSchedule> methodSchedule;
    @XmlElement(name = "interceptor-binding", required = true)
    protected List<InterceptorBinding> interceptorBinding;
    @XmlElement(name = "message-destination", required = true)
    protected List<MessageDestination> messageDestination;
    @XmlElement(name = "exclude-list")
    protected ExcludeList excludeList;
    @XmlElement(name = "application-exception", required = true)
    protected KeyedCollection<String, ApplicationException> applicationException;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<SecurityRole> getSecurityRole() {
        if (securityRole == null) {
            securityRole = new ArrayList<SecurityRole>();
        }
        return this.securityRole;
    }

    public List<MethodPermission> getMethodPermission() {
        if (methodPermission == null) {
            methodPermission = new ArrayList<MethodPermission>();
        }
        return this.methodPermission;
    }

    public List<ContainerTransaction> getContainerTransaction() {
        if (containerTransaction == null) {
            containerTransaction = new ArrayList<ContainerTransaction>();
        }
        return this.containerTransaction;
    }

    public List<ContainerConcurrency> getContainerConcurrency() {
        if (containerConcurrency == null) {
            containerConcurrency = new ArrayList<ContainerConcurrency>();
        }
        return this.containerConcurrency;
    }

    public List<MethodSchedule> getMethodSchedule() {
        if (methodSchedule == null) {
            methodSchedule = new ArrayList<MethodSchedule>();
        }
        return this.methodSchedule;
    }

    public Map<String,List<MethodAttribute>> getMethodTransactionMap(String ejbName) {
        return getMethodAttributes(ejbName, getContainerTransaction());
    }

    public Map<String,List<MethodAttribute>> getMethodConcurrencyMap(String ejbName) {
        return getMethodAttributes(ejbName, getContainerConcurrency());
    }

    public Map<String,List<MethodAttribute>> getMethodScheduleMap(String ejbName) {
        return getMethodAttributes(ejbName, getMethodSchedule());
    }

    private Map<String, List<MethodAttribute>> getMethodAttributes(String ejbName, List<? extends AttributeBinding> bindings) {

        Map<String,List<MethodAttribute>> methods = new LinkedHashMap<String,List<MethodAttribute>>();

        for (AttributeBinding<?> binding : bindings) {

            for (Method method : binding.getMethod()) {
                if (method.getEjbName().equals(ejbName)){
                    String methodName = method.getMethodName();
                    List<MethodAttribute> list = methods.get(methodName);
                    if (list == null){
                        list = new ArrayList<MethodAttribute>();
                        methods.put(methodName, list);
                    }
                    list.add(new MethodAttribute(binding.getAttribute(), method));
                }
            }
        }
        return methods;
    }

    public List<InterceptorBinding> getInterceptorBinding() {
        if (interceptorBinding == null) {
            interceptorBinding = new ArrayList<InterceptorBinding>();
        }
        return this.interceptorBinding;
    }

    public InterceptorBinding addInterceptorBinding(InterceptorBinding binding){
        getInterceptorBinding().add(binding);
        return binding;
    }

    public List<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestination>();
        }
        return this.messageDestination;
    }

    public ExcludeList getExcludeList() {
        if (excludeList == null){
            excludeList = new ExcludeList();
        }
        return excludeList;
    }

    public void setExcludeList(ExcludeList value) {
        this.excludeList = value;
    }

    public Collection<ApplicationException> getApplicationException() {
        if (applicationException == null) {
            applicationException = new KeyedCollection<String, ApplicationException>();
        }
        return this.applicationException;
    }

    public Map<String, ApplicationException> getApplicationExceptionMap() {
        return ((KeyedCollection<String, ApplicationException>) getApplicationException()).toMap();
    }

    public ApplicationException getApplicationException(String className) {
        return this.getApplicationExceptionMap().get(className);
    }

    public ApplicationException getApplicationException(Class clazz) {
        return getApplicationException(clazz.getName());
    }

    public void addApplicationException(Class clazz, boolean rollback) {
        getApplicationException().add(new ApplicationException(clazz, rollback));
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
