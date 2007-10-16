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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Defines information about how to provide the value for a
 * tag handler attribute that accepts a javax.el.MethodExpression.
 * <p/>
 * The deferred-method element has one optional subelement:
 * <p/>
 * method-signature  Provides the signature, as in the Java
 * Language Specifies, that is expected for
 * the method being identified by the
 * expression.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tld-deferred-methodType", propOrder = {"methodSignature"})
public class TldDeferredMethod {
    @XmlElement(name = "method-signature")
    protected String methodSignature;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String value) {
        this.methodSignature = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
