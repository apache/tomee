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


/**
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
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public NamedMethod() {
    }

    public NamedMethod(java.lang.reflect.Method method) {
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

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
