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
 * The wsdl-message-mapping element defines the mapping to a
 * specific message and its part. Together they define uniquely
 * the mapping for a specific parameter. Parts within a message
 * context are uniquely identified with their names.
 * <p/>
 * The parameter-mode is defined by the mapping to indicate
 * whether the mapping will be IN, OUT, or INOUT..  The presence
 * of the soap-header element indicates that the parameter is
 * mapped to a soap header only.  When absent, it means that the
 * wsdl-message is mapped to a Java parameter. The soap headers
 * are interpreted in the order they are provided in the mapping.
 * <p/>
 * Used in: method-param-parts-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wsdl-message-mappingType", propOrder = {
    "wsdlMessage",
    "wsdlMessagePartName",
    "parameterMode",
    "soapHeader"
})
public class WsdlMessageMapping {
    @XmlElement(name = "wsdl-message", required = true)
    protected QName wsdlMessage;
    @XmlElement(name = "wsdl-message-part-name", required = true)
    protected String wsdlMessagePartName;
    @XmlElement(name = "parameter-mode", required = true)
    protected String parameterMode;
    @XmlElement(name = "soap-header")
    protected Object soapHeader;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

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

    public String getParameterMode() {
        return parameterMode;
    }

    public void setParameterMode(String value) {
        this.parameterMode = value;
    }

    public Object getSoapHeader() {
        return soapHeader;
    }

    public void setSoapHeader(Object value) {
        this.soapHeader = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
