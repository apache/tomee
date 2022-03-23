/**
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
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for async-methodType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="async-methodType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="method-name" type="{http://java.sun.com/xml/ns/javaee}string"/&gt;
 *         &lt;element name="method-params" type="{http://java.sun.com/xml/ns/javaee}method-paramsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "async-methodType", propOrder = {
    "methodName",
    "methodParams"
})
public class AsyncMethod {

    @XmlElement(name = "method-name", required = true)
    protected String methodName;
    @XmlElement(name = "method-params")
    protected MethodParams methodParams;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public AsyncMethod() {
    }

    public AsyncMethod(final java.lang.reflect.Method method) {
        this.methodName = method.getName();
        final MethodParams methodParams = new MethodParams();
        for (final Class<?> type : method.getParameterTypes()) {
            methodParams.getMethodParam().add(type.getCanonicalName());
        }
        this.methodParams = methodParams;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(final String value) {
        this.methodName = value;
    }

    public MethodParams getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(final MethodParams value) {
        this.methodParams = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(final java.lang.String value) {
        this.id = value;
    }

}
