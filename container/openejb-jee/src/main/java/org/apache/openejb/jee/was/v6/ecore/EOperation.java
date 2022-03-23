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
package org.apache.openejb.jee.was.v6.ecore;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 *
 * Java class for EOperation complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EOperation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}ETypedElement"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="eParameters" type="{http://www.eclipse.org/emf/2002/Ecore}EParameter"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="eExceptions" type="{http://www.eclipse.org/emf/2002/Ecore}EClassifier"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="eExceptions" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EOperation", propOrder = {"eParameters",
    "eClassifierEExceptions"})
public class EOperation extends ETypedElement {

    protected List<EParameter> eParameters;
    @XmlElement(name = "eExceptions")
    protected List<EClassifier> eClassifierEExceptions;
    @XmlAttribute
    protected String eExceptions;

    /**
     * Gets the value of the eParameters property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the eParameters property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEParameters().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EParameter }
     */
    public List<EParameter> getEParameters() {
        if (eParameters == null) {
            eParameters = new ArrayList<EParameter>();
        }
        return this.eParameters;
    }

    /**
     * Gets the value of the eClassifierEExceptions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the eClassifierEExceptions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEClassifierEExceptions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EClassifier }
     */
    public List<EClassifier> getEClassifierEExceptions() {
        if (eClassifierEExceptions == null) {
            eClassifierEExceptions = new ArrayList<EClassifier>();
        }
        return this.eClassifierEExceptions;
    }

    /**
     * Gets the value of the eExceptions property.
     *
     * @return possible object is {@link String }
     */
    public String getEExceptions() {
        return eExceptions;
    }

    /**
     * Sets the value of the eExceptions property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEExceptions(final String value) {
        this.eExceptions = value;
    }

}
