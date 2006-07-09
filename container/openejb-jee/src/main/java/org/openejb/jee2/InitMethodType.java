/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

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
public class InitMethodType {

    @XmlElement(name = "create-method", required = true)
    protected NamedMethodType createMethod;
    @XmlElement(name = "bean-method", required = true)
    protected NamedMethodType beanMethod;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public NamedMethodType getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(NamedMethodType value) {
        this.createMethod = value;
    }

    public NamedMethodType getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(NamedMethodType value) {
        this.beanMethod = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
