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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p class="changed_added_2_2">Identifiy the called flow.</p>
 *
 * <div class="changed_added_2_2">
 *
 * </div>
 *
 *
 *
 *
 * <p>Java class for faces-config-flow-definition-flow-call-flow-referenceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-flow-definition-flow-call-flow-referenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="flow-document-id" type="{http://xmlns.jcp.org/xml/ns/javaee}java-identifierType" minOccurs="0"/&gt;
 *         &lt;element name="flow-id" type="{http://xmlns.jcp.org/xml/ns/javaee}java-identifierType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definition-flow-call-flow-referenceType", propOrder = {
    "flowDocumentId",
    "flowId"
})
public class FacesConfigFlowDefinitionFlowCallFlowReference {

    @XmlElement(name = "flow-document-id")
    protected JavaIdentifier flowDocumentId;
    @XmlElement(name = "flow-id", required = true)
    protected JavaIdentifier flowId;

    /**
     * Gets the value of the flowDocumentId property.
     *
     * @return possible object is
     * {@link JavaIdentifier }
     */
    public JavaIdentifier getFlowDocumentId() {
        return flowDocumentId;
    }

    /**
     * Sets the value of the flowDocumentId property.
     *
     * @param value allowed object is
     *              {@link JavaIdentifier }
     */
    public void setFlowDocumentId(final JavaIdentifier value) {
        this.flowDocumentId = value;
    }

    /**
     * Gets the value of the flowId property.
     *
     * @return possible object is
     * {@link JavaIdentifier }
     */
    public JavaIdentifier getFlowId() {
        return flowId;
    }

    /**
     * Sets the value of the flowId property.
     *
     * @param value allowed object is
     *              {@link JavaIdentifier }
     */
    public void setFlowId(final JavaIdentifier value) {
        this.flowId = value;
    }

}
