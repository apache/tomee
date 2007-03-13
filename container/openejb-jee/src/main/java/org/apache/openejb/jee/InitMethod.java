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
@XmlType(name = "init-methodType", propOrder = {
        "createMethod",
        "beanMethod"
        })
public class InitMethod {

    @XmlElement(name = "create-method", required = true)
    protected NamedMethod createMethod;
    @XmlElement(name = "bean-method", required = true)
    protected NamedMethod beanMethod;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public InitMethod() {
    }

    public InitMethod(java.lang.reflect.Method beanMethod) {
        this.beanMethod = new NamedMethod(beanMethod);
    }

    public NamedMethod getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(NamedMethod value) {
        this.createMethod = value;
    }

    public void setCreateMethod(String methodName) {
        this.createMethod = new NamedMethod(methodName);
    }

    public NamedMethod getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(NamedMethod value) {
        this.beanMethod = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
