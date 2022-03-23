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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigInteger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "method-param-parts-mappingType", propOrder = {
    "paramPosition",
    "paramType",
    "wsdlMessageMapping"
})
public class MethodParamPartsMapping {
    @XmlElement(name = "param-position", required = true)
    protected BigInteger paramPosition;
    @XmlElement(name = "param-type", required = true)
    protected String paramType;
    @XmlElement(name = "wsdl-message-mapping", required = true)
    protected WsdlMessageMapping wsdlMessageMapping;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public BigInteger getParamPosition() {
        return paramPosition;
    }

    public void setParamPosition(final BigInteger value) {
        this.paramPosition = value;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(final String value) {
        this.paramType = value;
    }

    public WsdlMessageMapping getWsdlMessageMapping() {
        return wsdlMessageMapping;
    }

    public void setWsdlMessageMapping(final WsdlMessageMapping value) {
        this.wsdlMessageMapping = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }
}
