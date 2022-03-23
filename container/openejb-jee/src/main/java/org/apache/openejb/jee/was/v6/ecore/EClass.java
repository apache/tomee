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
 * Java class for EClass complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EClass"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EClassifier"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="eSuperTypes" type="{http://www.eclipse.org/emf/2002/Ecore}EClass"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="eOperations" type="{http://www.eclipse.org/emf/2002/Ecore}EOperation"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="eStructuralFeatures" type="{http://www.eclipse.org/emf/2002/Ecore}EStructuralFeature"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="abstract" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="eSuperTypes" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="interface" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EClass", propOrder = {"eClassESuperTypes", "eOperations",
    "eStructuralFeatures"})
public class EClass extends EClassifier {

    @XmlElement(name = "eSuperTypes")
    protected List<EClass> eClassESuperTypes;
    protected List<EOperation> eOperations;
    protected List<EStructuralFeature> eStructuralFeatures;
    @XmlAttribute(name = "abstract")
    protected Boolean isAbstract;
    @XmlAttribute
    protected String eSuperTypes;
    @XmlAttribute(name = "interface")
    protected Boolean isInterface;

    /**
     * Gets the value of the eClassESuperTypes property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the eClassESuperTypes property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEClassESuperTypes().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link EClass }
     */
    public List<EClass> getEClassESuperTypes() {
        if (eClassESuperTypes == null) {
            eClassESuperTypes = new ArrayList<EClass>();
        }
        return this.eClassESuperTypes;
    }

    /**
     * Gets the value of the eOperations property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the eOperations property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEOperations().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EOperation }
     */
    public List<EOperation> getEOperations() {
        if (eOperations == null) {
            eOperations = new ArrayList<EOperation>();
        }
        return this.eOperations;
    }

    /**
     * Gets the value of the eStructuralFeatures property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the eStructuralFeatures property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEStructuralFeatures().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EStructuralFeature }
     */
    public List<EStructuralFeature> getEStructuralFeatures() {
        if (eStructuralFeatures == null) {
            eStructuralFeatures = new ArrayList<EStructuralFeature>();
        }
        return this.eStructuralFeatures;
    }

    /**
     * Gets the value of the isAbstract property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsAbstract() {
        return isAbstract;
    }

    /**
     * Sets the value of the isAbstract property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsAbstract(final Boolean value) {
        this.isAbstract = value;
    }

    /**
     * Gets the value of the eSuperTypes property.
     *
     * @return possible object is {@link String }
     */
    public String getESuperTypes() {
        return eSuperTypes;
    }

    /**
     * Sets the value of the eSuperTypes property.
     *
     * @param value allowed object is {@link String }
     */
    public void setESuperTypes(final String value) {
        this.eSuperTypes = value;
    }

    /**
     * Gets the value of the isInterface property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsInterface() {
        return isInterface;
    }

    /**
     * Sets the value of the isInterface property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsInterface(final Boolean value) {
        this.isInterface = value;
    }

}
