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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * javaee6.xsd
 *
 * <p>Java class for lifecycle-callbackType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="lifecycle-callbackType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lifecycle-callback-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="lifecycle-callback-method" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lifecycle-callbackType", propOrder = {
        "lifecycleCallbackClass",
        "lifecycleCallbackMethod"
        })
public class LifecycleCallback implements CallbackMethod {

    @XmlElement(name = "lifecycle-callback-class")
    protected String lifecycleCallbackClass;
    @XmlElement(name = "lifecycle-callback-method", required = true)
    protected String lifecycleCallbackMethod;

    public LifecycleCallback() {
    }

    public LifecycleCallback(java.lang.reflect.Method method) {
        this.lifecycleCallbackClass = method.getDeclaringClass().getName();
        this.lifecycleCallbackMethod = method.getName();
    }

    public LifecycleCallback(String lifecycleCallbackClass, String lifecycleCallbackMethod) {
        this.lifecycleCallbackClass = lifecycleCallbackClass;
        this.lifecycleCallbackMethod = lifecycleCallbackMethod;
    }

    public LifecycleCallback(NamedMethod method) {
        this.lifecycleCallbackClass = method.getClassName();
        this.lifecycleCallbackMethod = method.getMethodName();
    }

    public String getLifecycleCallbackClass() {
        return lifecycleCallbackClass;
    }

    public void setLifecycleCallbackClass(String value) {
        this.lifecycleCallbackClass = value;
    }

    public String getLifecycleCallbackMethod() {
        return lifecycleCallbackMethod;
    }

    public void setLifecycleCallbackMethod(String value) {
        this.lifecycleCallbackMethod = value;
    }

    public String getClassName() {
        return getLifecycleCallbackClass();
    }

    public String getMethodName() {
        return getLifecycleCallbackMethod();
    }
}
