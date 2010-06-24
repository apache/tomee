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
 * @version $Rev$ $Date$
 */
/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for around-timeoutType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="around-timeoutType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="method-name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "around-timeoutType", propOrder = { "clazz", "methodName" })
public class AroundTimeout implements CallbackMethod {

    @XmlElement(name = "class")
    protected String clazz;

    @XmlElement(name = "method-name", required = true)
    protected String methodName;

    public AroundTimeout() {
    }

    public AroundTimeout(java.lang.reflect.Method method) {
        this(method.getDeclaringClass().getName(), method.getName());
    }

    public AroundTimeout(String clazz, String methodName) {
        this.clazz = clazz;
        this.methodName = methodName;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String value) {
        this.clazz = value;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String value) {
        this.methodName = value;
    }

    public String getClassName() {
        return getClazz();
    }
}
