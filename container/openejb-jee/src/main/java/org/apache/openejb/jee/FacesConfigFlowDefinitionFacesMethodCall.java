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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p class="changed_added_2_2">Invoke a method, passing parameters if necessary.
 * The return from the method is used as the outcome for where to go next in the
 * flow.  If the method is a void method, the default outcome is used.<p>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p>Java class for faces-config-flow-definition-faces-method-callType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="faces-config-flow-definition-faces-method-callType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="method" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-faces-method-call-methodType"/>
 *         &lt;element name="default-outcome" type="{http://xmlns.jcp.org/xml/ns/javaee}string"/>
 *         &lt;element name="parameter" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-flow-call-parameterType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definition-faces-method-callType", propOrder = {
    "method",
    "defaultOutcome",
    "parameter"
})
public class FacesConfigFlowDefinitionFacesMethodCall {

    @XmlElement(required = true)
    protected FacesConfigFlowDefinitionFacesMethodCallMethod method;
    @XmlElement(name = "default-outcome", required = true)
    protected String defaultOutcome;
    protected List<FacesConfigFlowDefinitionFlowCallParameter> parameter;

    /**
     * Gets the value of the method property.
     *
     * @return possible object is
     * {@link FacesConfigFlowDefinitionFacesMethodCallMethod }
     */
    public FacesConfigFlowDefinitionFacesMethodCallMethod getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     *
     * @param value allowed object is
     *              {@link FacesConfigFlowDefinitionFacesMethodCallMethod }
     */
    public void setMethod(final FacesConfigFlowDefinitionFacesMethodCallMethod value) {
        this.method = value;
    }

    /**
     * Gets the value of the defaultOutcome property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDefaultOutcome() {
        return defaultOutcome;
    }

    /**
     * Sets the value of the defaultOutcome property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultOutcome(final String value) {
        this.defaultOutcome = value;
    }

    /**
     * Gets the value of the parameter property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameter property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameter().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionFlowCallParameter }
     */
    public List<FacesConfigFlowDefinitionFlowCallParameter> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<FacesConfigFlowDefinitionFlowCallParameter>();
        }
        return this.parameter;
    }

}
