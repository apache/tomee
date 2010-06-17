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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="named-methodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="method-name" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="method-params" type="{http://java.sun.com/xml/ns/javaee}method-paramsType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "named-methodType", propOrder = {
        "methodName",
        "methodParams"
        })
public class NamedMethod {

    @XmlElement(name = "method-name", required = true)
    protected String methodName;

    @XmlElement(name = "method-params")
    protected MethodParams methodParams;

    @XmlTransient
    protected String className;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public NamedMethod() {
    }

    public NamedMethod(java.lang.reflect.Method method) {
        this.className = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        MethodParams methodParams = new MethodParams();
        for (Class<?> type : method.getParameterTypes()) {
            methodParams.getMethodParam().add(type.getCanonicalName());
        }
        this.methodParams = methodParams;
    }

    public NamedMethod(String methodName, String... parameters) {
        this.methodName = methodName;

        if (parameters.length > 0){
            MethodParams params = new MethodParams();
            for (String paramName : parameters) {
                params.getMethodParam().add(paramName);
            }
            this.methodParams = params;
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String value) {
        this.methodName = value;
    }

    public MethodParams getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(MethodParams value) {
        this.methodParams = value;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final NamedMethod that = (NamedMethod) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;

        if (nullOrEmpty(this.methodParams) && nullOrEmpty(that.methodParams)) return true;

        if (methodParams != null ? !methodParams.equals(that.methodParams) : that.methodParams != null) return false;

        return true;
    }

    private boolean nullOrEmpty(MethodParams methodParams) {
        return methodParams == null || methodParams.getMethodParam().size() == 0;
    }

    public int hashCode() {
        int result;
        result = (methodName != null ? methodName.hashCode() : 0);
        result = 29 * result + (methodParams != null ? methodParams.hashCode() : 0);
        return result;
    }
}
