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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collection;


/**
 * ejb-jar_3_1.xsd
 *
 *
 * <p>Java class for assembly-descriptorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="assembly-descriptorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="security-role" type="{http://java.sun.com/xml/ns/javaee}security-roleType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="method-permission" type="{http://java.sun.com/xml/ns/javaee}method-permissionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="container-transaction" type="{http://java.sun.com/xml/ns/javaee}container-transactionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="interceptor-binding" type="{http://java.sun.com/xml/ns/javaee}interceptor-bindingType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="message-destination" type="{http://java.sun.com/xml/ns/javaee}message-destinationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="exclude-list" type="{http://java.sun.com/xml/ns/javaee}exclude-listType" minOccurs="0"/&gt;
 *         &lt;element name="application-exception" type="{http://java.sun.com/xml/ns/javaee}application-exceptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assembly-descriptorType", propOrder = {
    "securityRole",
    "methodPermission",
    "containerTransaction",
    //TODO moved out of assembly descriptor in schema
    "containerConcurrency",
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
    //TODO moved out of assembly descriptor in schema
    @XmlElement(name = "container-concurrency", required = true)
    protected List<ContainerConcurrency> containerConcurrency;
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

    public Map<String, List<MethodAttribute>> getMethodTransactionMap(final String ejbName) {
        return getMethodAttributes(ejbName, getContainerTransaction());
    }

    //TODO moved out of assembly descriptor in schema

    public List<ContainerConcurrency> getContainerConcurrency() {
        if (containerConcurrency == null) {
            containerConcurrency = new ArrayList<ContainerConcurrency>();
        }
        return this.containerConcurrency;
    }

    //TODO moved out of assembly descriptor in schema
    public Map<String, List<MethodAttribute>> getMethodConcurrencyMap(final String ejbName) {
        return getMethodAttributes(ejbName, getContainerConcurrency());
    }

    private Map<String, List<MethodAttribute>> getMethodAttributes(final String ejbName, final List<? extends AttributeBinding> bindings) {

        final Map<String, List<MethodAttribute>> methods = new LinkedHashMap<String, List<MethodAttribute>>();

        for (final AttributeBinding<?> binding : bindings) {

            for (final Method method : binding.getMethod()) {
                if (method.getEjbName().equals(ejbName)) {
                    final String methodName = method.getMethodName();
                    List<MethodAttribute> list = methods.computeIfAbsent(methodName, k -> new ArrayList<MethodAttribute>());
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

    public InterceptorBinding addInterceptorBinding(final InterceptorBinding binding) {
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
        if (excludeList == null) {
            excludeList = new ExcludeList();
        }
        return excludeList;
    }

    public void setExcludeList(final ExcludeList value) {
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

    public ApplicationException getApplicationException(final String className) {
        return this.getApplicationExceptionMap().get(className);
    }

    public ApplicationException getApplicationException(final Class clazz) {
        return getApplicationException(clazz.getName());
    }

    public void addApplicationException(final Class clazz, final boolean rollback, final boolean inherited) {
        getApplicationException().add(new ApplicationException(clazz, rollback));
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
