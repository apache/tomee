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
import java.util.ArrayList;
import java.util.List;


/**
 * The container-transactionType specifies how the container
 * must manage transaction scopes for the enterprise bean's
 * method invocations. It defines an optional description, a
 * list of method elements, and a transaction attribute. The
 * transaction attribute is to be applied to all the specified
 * methods.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "container-transactionType", propOrder = {
        "descriptions",
        "method",
        "transAttribute"
        })
public class ContainerTransaction {

    @XmlElement(required = true)
    protected List<Method> method;
    @XmlElement(name = "trans-attribute", required = true)
    protected TransAttribute transAttribute;
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

    public ContainerTransaction() {
    }

    public ContainerTransaction(TransAttribute transAttribute, String className, String ejbName, String methodName) {
        this(transAttribute, new Method(ejbName, className, methodName));
    }

    public ContainerTransaction(TransAttribute transAttribute, String ejbName, java.lang.reflect.Method method) {
        this(transAttribute, new Method(ejbName, method));
    }

    public ContainerTransaction(TransAttribute transAttribute, Method method) {
        this.transAttribute = transAttribute;
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

    public TransAttribute getTransAttribute() {
        return transAttribute;
    }

    public void setTransAttribute(TransAttribute value) {
        this.transAttribute = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }


}
