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
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p class="changed_added_2_2">Define a switch node in a flow graph.</p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>This element must contain one or more
 * <code>&lt;case&gt;</code&gt; elements.  When control passes to the
 * <code>&lt;switch&gt;</code&gt; node, each of the cases must be considered
 * in order and control must past to the <code>&lt;from-outcome&gt;</code&gt;
 * of the first one whose <code>&lt;if&gt;</code&gt; expression evaluates to
 * <code>true</code>.</p>
 *
 * </div>
 *
 *
 *
 *
 *
 * <p>Java class for faces-config-flow-definition-switchType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-flow-definition-switchType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="case" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-switch-caseType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="default-outcome" type="{http://xmlns.jcp.org/xml/ns/javaee}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definition-switchType", propOrder = {
    "_case",
    "defaultOutcome"
})
public class FacesConfigFlowDefinitionSwitch {

    @XmlElement(name = "case")
    protected List<FacesConfigFlowDefinitionSwitchCase> _case;
    @XmlElement(name = "default-outcome")
    protected XmlString defaultOutcome;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    /**
     * Gets the value of the case property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the case property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCase().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionSwitchCase }
     */
    public List<FacesConfigFlowDefinitionSwitchCase> getCase() {
        if (_case == null) {
            _case = new ArrayList<FacesConfigFlowDefinitionSwitchCase>();
        }
        return this._case;
    }

    /**
     * Gets the value of the defaultOutcome property.
     *
     * @return possible object is
     * {@link XmlString }
     */
    public XmlString getDefaultOutcome() {
        return defaultOutcome;
    }

    /**
     * Sets the value of the defaultOutcome property.
     *
     * @param value allowed object is
     *              {@link XmlString }
     */
    public void setDefaultOutcome(final XmlString value) {
        this.defaultOutcome = value;
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
