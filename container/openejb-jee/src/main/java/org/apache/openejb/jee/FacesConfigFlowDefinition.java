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

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p class="changed_added_2_2">Top level element for a flow
 * definition.</p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>If there is no <code>&lt;start-node&gt;</code&gt; element declared, it
 * is assumed to be <code>&lt;flowName&gt;.xhtml</code&gt;.</p&gt;
 *
 * </div>
 *
 *
 *
 *
 * <p>Java class for faces-config-flow-definitionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-flow-definitionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://xmlns.jcp.org/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="start-node" type="{http://xmlns.jcp.org/xml/ns/javaee}java-identifierType" minOccurs="0"/&gt;
 *         &lt;element name="view" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-viewType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="switch" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-switchType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="flow-return" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-flow-returnType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="navigation-rule" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-navigation-ruleType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="flow-call" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-flow-callType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="method-call" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-faces-method-callType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="initializer" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-initializerType" minOccurs="0"/&gt;
 *         &lt;element name="finalizer" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-finalizerType" minOccurs="0"/&gt;
 *         &lt;element name="inbound-parameter" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-flow-definition-inbound-parameterType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definitionType", propOrder = {
    "description",
    "displayName",
    "icon",
    "startNode",
    "view",
    "_switch",
    "flowReturn",
    "navigationRule",
    "flowCall",
    "methodCall",
    "initializer",
    "finalizer",
    "inboundParameter"
})
public class FacesConfigFlowDefinition {

    protected List<Description> description;
    @XmlElement(name = "display-name")
    protected List<DisplayName> displayName;
    protected List<Icon> icon;
    @XmlElement(name = "start-node")
    protected JavaIdentifier startNode;
    protected List<FacesConfigFlowDefinitionView> view;
    @XmlElement(name = "switch")
    protected List<FacesConfigFlowDefinitionSwitch> _switch;
    @XmlElement(name = "flow-return")
    protected List<FacesConfigFlowDefinitionFlowReturn> flowReturn;
    @XmlElement(name = "navigation-rule")
    protected List<FacesNavigationRule> navigationRule;
    @XmlElement(name = "flow-call")
    protected List<FacesConfigFlowDefinitionFlowCall> flowCall;
    @XmlElement(name = "method-call")
    protected List<FacesConfigFlowDefinitionFacesMethodCall> methodCall;
    protected FacesConfigFlowDefinitionInitializer initializer;
    protected FacesConfigFlowDefinitionFinalizer finalizer;
    @XmlElement(name = "inbound-parameter")
    protected List<FacesConfigFlowDefinitionInboundParameter> inboundParameter;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    /**
     * Gets the value of the description property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Description }
     */
    public List<Description> getDescription() {
        if (description == null) {
            description = new ArrayList<Description>();
        }
        return this.description;
    }

    /**
     * Gets the value of the displayName property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDisplayName().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link DisplayName }
     */
    public List<DisplayName> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<DisplayName>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIcon().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Icon }
     */
    public List<Icon> getIcon() {
        if (icon == null) {
            icon = new ArrayList<Icon>();
        }
        return this.icon;
    }

    /**
     * Gets the value of the startNode property.
     *
     * @return possible object is
     * {@link JavaIdentifier }
     */
    public JavaIdentifier getStartNode() {
        return startNode;
    }

    /**
     * Sets the value of the startNode property.
     *
     * @param value allowed object is
     *              {@link JavaIdentifier }
     */
    public void setStartNode(final JavaIdentifier value) {
        this.startNode = value;
    }

    /**
     * Gets the value of the view property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the view property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getView().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionView }
     */
    public List<FacesConfigFlowDefinitionView> getView() {
        if (view == null) {
            view = new ArrayList<FacesConfigFlowDefinitionView>();
        }
        return this.view;
    }

    /**
     * Gets the value of the switch property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the switch property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSwitch().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionSwitch }
     */
    public List<FacesConfigFlowDefinitionSwitch> getSwitch() {
        if (_switch == null) {
            _switch = new ArrayList<FacesConfigFlowDefinitionSwitch>();
        }
        return this._switch;
    }

    /**
     * Gets the value of the flowReturn property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flowReturn property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlowReturn().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionFlowReturn }
     */
    public List<FacesConfigFlowDefinitionFlowReturn> getFlowReturn() {
        if (flowReturn == null) {
            flowReturn = new ArrayList<FacesConfigFlowDefinitionFlowReturn>();
        }
        return this.flowReturn;
    }

    /**
     * Gets the value of the navigationRule property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the navigationRule property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNavigationRule().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigNavigationRuleExtension }
     */
    public List<FacesNavigationRule> getNavigationRule() {
        if (navigationRule == null) {
            navigationRule = new ArrayList<FacesNavigationRule>();
        }
        return this.navigationRule;
    }

    /**
     * Gets the value of the flowCall property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flowCall property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlowCall().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionFlowCall }
     */
    public List<FacesConfigFlowDefinitionFlowCall> getFlowCall() {
        if (flowCall == null) {
            flowCall = new ArrayList<FacesConfigFlowDefinitionFlowCall>();
        }
        return this.flowCall;
    }

    /**
     * Gets the value of the methodCall property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the methodCall property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMethodCall().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionFacesMethodCall }
     */
    public List<FacesConfigFlowDefinitionFacesMethodCall> getMethodCall() {
        if (methodCall == null) {
            methodCall = new ArrayList<FacesConfigFlowDefinitionFacesMethodCall>();
        }
        return this.methodCall;
    }

    /**
     * Gets the value of the initializer property.
     *
     * @return possible object is
     * {@link FacesConfigFlowDefinitionInitializer }
     */
    public FacesConfigFlowDefinitionInitializer getInitializer() {
        return initializer;
    }

    /**
     * Sets the value of the initializer property.
     *
     * @param value allowed object is
     *              {@link FacesConfigFlowDefinitionInitializer }
     */
    public void setInitializer(final FacesConfigFlowDefinitionInitializer value) {
        this.initializer = value;
    }

    /**
     * Gets the value of the finalizer property.
     *
     * @return possible object is
     * {@link FacesConfigFlowDefinitionFinalizer }
     */
    public FacesConfigFlowDefinitionFinalizer getFinalizer() {
        return finalizer;
    }

    /**
     * Sets the value of the finalizer property.
     *
     * @param value allowed object is
     *              {@link FacesConfigFlowDefinitionFinalizer }
     */
    public void setFinalizer(final FacesConfigFlowDefinitionFinalizer value) {
        this.finalizer = value;
    }

    /**
     * Gets the value of the inboundParameter property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inboundParameter property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInboundParameter().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesConfigFlowDefinitionInboundParameter }
     */
    public List<FacesConfigFlowDefinitionInboundParameter> getInboundParameter() {
        if (inboundParameter == null) {
            inboundParameter = new ArrayList<FacesConfigFlowDefinitionInboundParameter>();
        }
        return this.inboundParameter;
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
