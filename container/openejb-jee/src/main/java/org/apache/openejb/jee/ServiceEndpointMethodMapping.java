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
import java.util.ArrayList;
import java.util.List;

/**
 * The service-endpoint-method-mapping element defines the mapping of
 * Java methods to operations (which are not uniquely qualified by
 * qnames).
 * <p/>
 * The wsdl-operation should be interpreted with respect to the
 * portType and binding in which this definition is embedded within.
 * See the definitions for service-endpoint-interface-mapping and
 * service-interface-mapping to acquire the proper context.  The
 * wrapped-element indicator should only be specified when a WSDL
 * message wraps an element type.  The wsdl-return-value-mapping is
 * not specified for one-way operations.
 * <p/>
 * Used in: service-endpoint-interface-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-endpoint-method-mappingType", propOrder = {
    "javaMethodName",
    "wsdlOperation",
    "wrappedElement",
    "methodParamPartsMapping",
    "wsdlReturnValueMapping"
})
public class ServiceEndpointMethodMapping {
    @XmlElement(name = "java-method-name", required = true)
    protected String javaMethodName;
    @XmlElement(name = "wsdl-operation", required = true)
    protected String wsdlOperation;
    @XmlElement(name = "wrapped-element")
    protected Object wrappedElement;
    @XmlElement(name = "method-param-parts-mapping")
    protected List<MethodParamPartsMapping> methodParamPartsMapping;
    @XmlElement(name = "wsdl-return-value-mapping")
    protected WsdlReturnValueMapping wsdlReturnValueMapping;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getJavaMethodName() {
        return javaMethodName;
    }

    public void setJavaMethodName(String value) {
        this.javaMethodName = value;
    }

    public String getWsdlOperation() {
        return wsdlOperation;
    }

    public void setWsdlOperation(String value) {
        this.wsdlOperation = value;
    }

    public Object getWrappedElement() {
        return wrappedElement;
    }

    public void setWrappedElement(Object value) {
        this.wrappedElement = value;
    }

    public List<MethodParamPartsMapping> getMethodParamPartsMapping() {
        if (methodParamPartsMapping == null) {
            methodParamPartsMapping = new ArrayList<MethodParamPartsMapping>();
        }
        return this.methodParamPartsMapping;
    }

    public WsdlReturnValueMapping getWsdlReturnValueMapping() {
        return wsdlReturnValueMapping;
    }

    public void setWsdlReturnValueMapping(WsdlReturnValueMapping value) {
        this.wsdlReturnValueMapping = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
