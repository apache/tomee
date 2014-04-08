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
 * TODO what schema?
 * The wsdl-return-value-mapping  element defines the mapping for the
 * method's return value. It defines the mapping to a specific message
 * and its part.  Together they define uniquely the mapping for a
 * specific parameter. Parts within a message context are uniquely
 * identified with their names. The wsdl-message-part-name is not
 * specified if there is no return value or OUT parameters.
 * <p/>
 * Used in: service-endpoint-method-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wsdl-return-value-mappingType", propOrder = {
    "methodReturnValue",
    "wsdlMessage",
    "wsdlMessagePartName"
})
public class WsdlReturnValueMapping {
    @XmlElement(name = "method-return-value", required = true)
    protected String methodReturnValue;
    @XmlElement(name = "wsdl-message", required = true)
    protected QName wsdlMessage;
    @XmlElement(name = "wsdl-message-part-name")
    protected String wsdlMessagePartName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getMethodReturnValue() {
        return methodReturnValue;
    }

    public void setMethodReturnValue(String value) {
        this.methodReturnValue = value;
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

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
