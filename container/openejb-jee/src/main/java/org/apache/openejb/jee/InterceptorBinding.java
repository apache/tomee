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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for interceptor-bindingType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="interceptor-bindingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}string"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="interceptor-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="interceptor-order" type="{http://java.sun.com/xml/ns/javaee}interceptor-orderType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="exclude-default-interceptors" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="exclude-class-interceptors" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interceptor-bindingType", propOrder = {
    "descriptions",
    "ejbName",
    "interceptorClass",
    "interceptorOrder",
    "excludeDefaultInterceptors",
    "excludeClassInterceptors",
    "method"
})
public class InterceptorBinding {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "interceptor-class", required = true)
    protected List<String> interceptorClass;
    @XmlElement(name = "interceptor-order")
    protected InterceptorOrder interceptorOrder;
    @XmlElement(name = "exclude-default-interceptors")
    protected boolean excludeDefaultInterceptors;
    @XmlElement(name = "exclude-class-interceptors")
    protected boolean excludeClassInterceptors;
    protected NamedMethod method;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    protected String className;

    public InterceptorBinding() {
    }

    public InterceptorBinding(final EnterpriseBean bean, final Interceptor... interceptors) {
        this(bean.getEjbName(), interceptors);
        this.className = bean.getEjbClass();
    }

    public InterceptorBinding(final String ejbName, final Interceptor... interceptors) {
        this.ejbName = ejbName;
        final List<String> interceptorClasses = this.getInterceptorClass();
        for (final Interceptor interceptor : interceptors) {
            interceptorClasses.add(interceptor.getInterceptorClass());
        }
    }

    public InterceptorBinding(final String ejbName, final String... interceptorClasses) {
        this.ejbName = ejbName;
        this.getInterceptorClass().addAll(Arrays.asList(interceptorClasses));
    }

    public InterceptorBinding(final String ejbName) {
        this.ejbName = ejbName;
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(final String value) {
        this.ejbName = value;
    }

    public List<String> getInterceptorClass() {
        if (interceptorClass == null) {
            interceptorClass = new ArrayList<String>();
        }
        return this.interceptorClass;
    }

    public InterceptorOrder getInterceptorOrder() {
        return interceptorOrder;
    }

    public InterceptorOrder setInterceptorOrder(final InterceptorOrder value) {
        this.interceptorOrder = value;
        return value;
    }

    public boolean getExcludeDefaultInterceptors() {
        return excludeDefaultInterceptors;
    }

    public void setExcludeDefaultInterceptors(final boolean value) {
        this.excludeDefaultInterceptors = value;
    }

    public boolean getExcludeClassInterceptors() {
        return excludeClassInterceptors;
    }

    public void setExcludeClassInterceptors(final boolean value) {
        this.excludeClassInterceptors = value;
    }

    public NamedMethod getMethod() {
        return method;
    }

    public void setMethod(final NamedMethod value) {
        this.method = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    public String getClassName() {
        return className;
    }
}
