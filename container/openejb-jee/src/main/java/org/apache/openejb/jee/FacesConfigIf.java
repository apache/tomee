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
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>The "if" element defines a condition that must resolve
 * to true in order for the navigation case on which it is
 * defined to be matched, with the existing match criteria
 * (action method and outcome) as a prerequiste, if present.
 * The condition is defined declaratively using a value
 * expression in the body of this element. The expression is
 * evaluated at the time the navigation case is being matched.
 * If the "from-outcome" is omitted and this element is
 * present, the navigation handler will match a null outcome
 * and use the condition return value to determine if the
 * case should be considered a match.</p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>When used in a <code>&lt;switch&gt;</code&gt; within a flow, if the
 * expresion returns <code>true</code>, the
 * <code>&lt;from-outcome&gt;</code&gt; sibling element's outcome is used as
 * the id of the node in the flow graph to which control must be
 * passed.</p>
 *
 * </div>
 *
 *
 *
 *
 * <p>Java class for faces-config-ifType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-ifType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://xmlns.jcp.org/xml/ns/javaee&gt;faces-config-el-expressionType"&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-ifType", propOrder = {
    "value"
})
public class FacesConfigIf {

    @XmlValue
    protected java.lang.String value;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    /**
     * <p> EL expressions present within a faces config file
     * must start with the character sequence of '#{' and
     * end with '}'.</p>
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setValue(final java.lang.String value) {
        this.value = value;
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
