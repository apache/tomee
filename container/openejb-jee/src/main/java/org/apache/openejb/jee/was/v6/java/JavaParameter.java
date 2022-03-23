/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.java;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.openejb.jee.was.v6.ecore.EParameter;

/**
 *
 * Java class for JavaParameter complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="JavaParameter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EParameter"&gt;
 *       &lt;attribute name="final" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="parameterKind" type="{java.xmi}JavaParameterKind" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JavaParameter")
public class JavaParameter extends EParameter {

    @XmlAttribute(name = "final")
    protected Boolean isFinal;
    @XmlAttribute
    protected JavaParameterEnum parameterKind;

    /**
     * Gets the value of the isFinal property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsFinal() {
        return isFinal;
    }

    /**
     * Sets the value of the isFinal property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsFinal(final Boolean value) {
        this.isFinal = value;
    }

    /**
     * Gets the value of the parameterKind property.
     *
     * @return possible object is {@link JavaParameterEnum }
     */
    public JavaParameterEnum getParameterKind() {
        return parameterKind;
    }

    /**
     * Sets the value of the parameterKind property.
     *
     * @param value allowed object is {@link JavaParameterEnum }
     */
    public void setParameterKind(final JavaParameterEnum value) {
        this.parameterKind = value;
    }

}
