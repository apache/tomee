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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * javaee6.xsd
 *
 * <p>Java class for injection-targetType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="injection-targetType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="injection-target-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="injection-target-name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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

    public InjectionTarget(final String injectionTargetClass, final String injectionTargetName) {
        this.injectionTargetClass = injectionTargetClass;
        this.injectionTargetName = injectionTargetName;
    }

    public String getInjectionTargetClass() {
        return injectionTargetClass;
    }

    public void setInjectionTargetClass(final String value) {
        this.injectionTargetClass = value;
    }

    public void setInjectionTargetClass(final Class clazz) {
        setInjectionTargetClass(clazz.getName());
    }

    public String getInjectionTargetName() {
        return injectionTargetName;
    }

    public void setInjectionTargetName(final String value) {
        this.injectionTargetName = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InjectionTarget that = (InjectionTarget) o;

        if (!Objects.equals(injectionTargetClass, that.injectionTargetClass))
            return false;
        if (!Objects.equals(injectionTargetName, that.injectionTargetName))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = injectionTargetClass != null ? injectionTargetClass.hashCode() : 0;
        result = 31 * result + (injectionTargetName != null ? injectionTargetName.hashCode() : 0);
        return result;
    }
}
