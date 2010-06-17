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
 * ejb-jar_3_1.xsd
 *
 * <p/>
 * <p>Java class for assembly-descriptorType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="assembly-descriptorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="security-role" type="{http://java.sun.com/xml/ns/javaee}security-roleType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="method-permission" type="{http://java.sun.com/xml/ns/javaee}method-permissionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="container-transaction" type="{http://java.sun.com/xml/ns/javaee}container-transactionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="interceptor-binding" type="{http://java.sun.com/xml/ns/javaee}interceptor-bindingType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="message-destination" type="{http://java.sun.com/xml/ns/javaee}message-destinationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="exclude-list" type="{http://java.sun.com/xml/ns/javaee}exclude-listType" minOccurs="0"/>
 *         &lt;element name="application-exception" type="{http://java.sun.com/xml/ns/javaee}application-exceptionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assembly-descriptorType", propOrder = {
        "securityRole",
        "methodPermission",
        "containerTransaction",
        //TODO moved out of assembly descriptor in schema
        "containerConcurrency",
        //TODO moved out of assembly descriptor in schema
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
    //TODO moved out of assembly descriptor in schema
    @XmlElement(name = "container-concurrency", required = true)
    protected List<ContainerConcurrency> containerConcurrency;
    //TODO moved out of assembly descriptor in schema
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

    public Map<String, List<MethodAttribute>> getMethodTransactionMap(String ejbName) {
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

    public List<MethodSchedule> getMethodSchedule() {
        if (methodSchedule == null) {
            methodSchedule = new ArrayList<MethodSchedule>();
        }
        return this.methodSchedule;
    }

    //TODO moved out of assembly descriptor in schema

    public Map<String, List<MethodAttribute>> getMethodConcurrencyMap(String ejbName) {
        return getMethodAttributes(ejbName, getContainerConcurrency());
    }

    //TODO moved out of assembly descriptor in schema

    public Map<String, List<MethodAttribute>> getMethodScheduleMap(String ejbName) {
        Map<String, List<MethodAttribute>> methods = new LinkedHashMap<String, List<MethodAttribute>>();

        for (MethodSchedule methodSchedule : getMethodSchedule()) {
            if (!methodSchedule.getEjbName().equals(ejbName)) continue;

            NamedMethod method = methodSchedule.getMethod();
            String methodName = method.getMethodName();
            List<MethodAttribute> list = methods.get(methodName);
            if (list == null) {
                list = new ArrayList<MethodAttribute>();
                methods.put(methodName, list);
            }
            list.add(new MethodAttribute(methodSchedule.getAttribute(), ejbName, method));
        }
        return methods;
    }

    private Map<String, List<MethodAttribute>> getMethodAttributes(String ejbName, List<? extends AttributeBinding> bindings) {

        Map<String, List<MethodAttribute>> methods = new LinkedHashMap<String, List<MethodAttribute>>();

        for (AttributeBinding<?> binding : bindings) {

            for (Method method : binding.getMethod()) {
                if (method.getEjbName().equals(ejbName)) {
                    String methodName = method.getMethodName();
                    List<MethodAttribute> list = methods.get(methodName);
                    if (list == null) {
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

    public InterceptorBinding addInterceptorBinding(InterceptorBinding binding) {
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
