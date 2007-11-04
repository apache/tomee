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
import javax.xml.namespace.QName;

/**
 * The exception-mapping element defines the mapping between the
 * service specific exception types and wsdl faults and
 * SOAP headerfaults.
 * <p/>
 * This element should be interpreted with respect to the
 * mapping between a method and an operation which provides the
 * mapping context.
 * <p/>
 * Used in: service-endpoint-method-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exception-mappingType", propOrder = {
    "exceptionType",
    "wsdlMessage",
    "wsdlMessagePartName",
    "constructorParameterOrder"
})
public class ExceptionMapping {
    @XmlElement(name = "exception-type", required = true)
    protected String exceptionType;
    @XmlElement(name = "wsdl-message", required = true)
    protected QName wsdlMessage;
    @XmlElement(name = "wsdl-message-part-name")
    protected String wsdlMessagePartName;
    @XmlElement(name = "constructor-parameter-order")
    protected ConstructorParameterOrder constructorParameterOrder;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String value) {
        this.exceptionType = value;
    }

    public QName getWsdlMessage() {
        return wsdlMessage;
    }

    public void setWsdlMessage(QName value) {
        this.wsdlMessage = value;
    }

    public String getWsdlMessagePartName() {
        return wsdlMessagePartName;
    }

    public void setWsdlMessagePartName(String value) {
        this.wsdlMessagePartName = value;
    }

    public ConstructorParameterOrder getConstructorParameterOrder() {
        return constructorParameterOrder;
    }

    public void setConstructorParameterOrder(ConstructorParameterOrder value) {
        this.constructorParameterOrder = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
