/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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

package org.openejb.jee2;

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
    @XmlElement(name = "interceptor-binding", required = true)
    protected List<InterceptorBinding> interceptorBinding;
    @XmlElement(name = "message-destination", required = true)
    protected List<MessageDestination> messageDestination;
    @XmlElement(name = "exclude-list")
    protected ExcludeList excludeList;
    @XmlElement(name = "application-exception", required = true)
    protected List<ApplicationException> applicationException;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the securityRole property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the securityRole property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getSecurityRole().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityRole }
     */
    public List<SecurityRole> getSecurityRole() {
        if (securityRole == null) {
            securityRole = new ArrayList<SecurityRole>();
        }
        return this.securityRole;
    }

    /**
     * Gets the value of the methodPermission property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the methodPermission property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getMethodPermission().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link MethodPermission }
     */
    public List<MethodPermission> getMethodPermission() {
        if (methodPermission == null) {
            methodPermission = new ArrayList<MethodPermission>();
        }
        return this.methodPermission;
    }

    /**
     * Gets the value of the containerTransaction property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the containerTransaction property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getContainerTransaction().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ContainerTransaction }
     */
    public List<ContainerTransaction> getContainerTransaction() {
        if (containerTransaction == null) {
            containerTransaction = new ArrayList<ContainerTransaction>();
        }
        return this.containerTransaction;
    }

    /**
     * Gets the value of the interceptorBinding property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the interceptorBinding property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getInterceptorBinding().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link InterceptorBinding }
     */
    public List<InterceptorBinding> getInterceptorBinding() {
        if (interceptorBinding == null) {
            interceptorBinding = new ArrayList<InterceptorBinding>();
        }
        return this.interceptorBinding;
    }

    /**
     * Gets the value of the messageDestination property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDestination property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getMessageDestination().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestination }
     */
    public List<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestination>();
        }
        return this.messageDestination;
    }

    public ExcludeList getExcludeList() {
        return excludeList;
    }

    public void setExcludeList(ExcludeList value) {
        this.excludeList = value;
    }

    /**
     * Gets the value of the applicationException property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applicationException property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getApplicationException().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationException }
     */
    public List<ApplicationException> getApplicationException() {
        if (applicationException == null) {
            applicationException = new ArrayList<ApplicationException>();
        }
        return this.applicationException;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
