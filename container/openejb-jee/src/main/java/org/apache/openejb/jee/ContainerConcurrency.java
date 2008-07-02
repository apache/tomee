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

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.util.List;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "container-concurrencyType", propOrder = {
        "descriptions",
        "method",
        "concurrencyAttribute"
        })
public class ContainerConcurrency implements AttributeBinding<ConcurrencyAttribute>{

    @XmlElement(required = true)
    protected List<Method> method;
    @XmlElement(name = "concurrency-attribute", required = true)
    protected ConcurrencyAttribute concurrencyAttribute;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    protected TextMap description = new TextMap();

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public ContainerConcurrency() {
    }

    public ContainerConcurrency(ConcurrencyAttribute concurrencyAttribute, String className, String ejbName, String methodName) {
        this(concurrencyAttribute, new Method(ejbName, className, methodName));
    }

    public ContainerConcurrency(ConcurrencyAttribute concurrencyAttribute, String ejbName, java.lang.reflect.Method method) {
        this(concurrencyAttribute, new Method(ejbName, method));
    }

    public ContainerConcurrency(ConcurrencyAttribute concurrencyAttribute, Method method) {
        this.concurrencyAttribute = concurrencyAttribute;
        getMethod().add(method);
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public List<Method> getMethod() {
        if (method == null) {
            method = new ArrayList<Method>();
        }
        return this.method;
    }

    public ConcurrencyAttribute getAttribute() {
        return concurrencyAttribute;
    }

    public ConcurrencyAttribute getConcurrencyAttribute() {
        return concurrencyAttribute;
    }

    public void setConcurrencyAttribute(ConcurrencyAttribute value) {
        this.concurrencyAttribute = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
