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
 * <p>Java class for injection-targetType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="injection-targetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="injection-target-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="injection-target-name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "injection-targetType", propOrder = {
        "injectionTargetClass",
        "injectionTargetName"
        })
public class InjectionTarget {

    @XmlElement(name = "injection-target-class", required = true)
    protected String injectionTargetClass;
    @XmlElement(name = "injection-target-name", required = true)
    protected String injectionTargetName;

    public InjectionTarget() {
    }

    public InjectionTarget(String injectionTargetClass, String injectionTargetName) {
        this.injectionTargetClass = injectionTargetClass;
        this.injectionTargetName = injectionTargetName;
    }

    public String getInjectionTargetClass() {
        return injectionTargetClass;
    }

    public void setInjectionTargetClass(String value) {
        this.injectionTargetClass = value;
    }

    public void setInjectionTargetClass(Class clazz) {
        setInjectionTargetClass(clazz.getName());
    }

    public String getInjectionTargetName() {
        return injectionTargetName;
    }

    public void setInjectionTargetName(String value) {
        this.injectionTargetName = value;
    }

}
