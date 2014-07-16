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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p class="changed_added_2_2">Define a call node in a flow graph.</p>
 * <p/>
 * <div class="changed_added_2_2">
 * <p/>
 * <p>This element must contain exactly one <code>&lt;flow-reference&gt;</code> element,
 * which must contain exactly one <code>&lt;flow-id&gt;</code> element.</p>
 * </div>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p>Java class for faces-config-flow-definition-flow-callType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="faces-config-flow-definition-flow-callType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="flow-reference" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-flow-call-flow-referenceType"/>
 *         &lt;element name="outbound-parameter" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-flow-call-outbound-parameterType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definition-flow-callType", propOrder = {
    "flowReference",
    "outboundParameter"
})
public class FacesConfigFlowDefinitionFlowCall {

    @XmlElement(name = "flow-reference", required = true)
    protected FacesConfigFlowDefinitionFlowCallFlowReference flowReference;
    @XmlElement(name = "outbound-parameter")
    protected List<FacesConfigFlowDefinitionFlowCallOutboundParameter> outboundParameter;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    /**
     * Gets the value of the flowReference property.
     *
     * @return possible object is
     * {@link FacesConfigFlowDefinitionFlowCallFlowReference }
     */
    public FacesConfigFlowDefinitionFlowCallFlowReference getFlowReference() {
        return flowReference;
    }

    /**
     * Sets the value of the flowReference property.
     *
     * @param value allowed object is
     *              {@link FacesConfigFlowDefinitionFlowCallFlowReference }
     */
    public void setFlowReference(final FacesConfigFlowDefinitionFlowCallFlowReference value) {
        this.flowReference = value;
    }

    /**
     * Gets the value of the outboundParameter property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outboundParameter property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutboundParameter().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionFlowCallOutboundParameter }
     */
    public List<FacesConfigFlowDefinitionFlowCallOutboundParameter> getOutboundParameter() {
        if (outboundParameter == null) {
            outboundParameter = new ArrayList<FacesConfigFlowDefinitionFlowCallOutboundParameter>();
        }
        return this.outboundParameter;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setId(final java.lang.String value) {
        this.id = value;
    }

}
