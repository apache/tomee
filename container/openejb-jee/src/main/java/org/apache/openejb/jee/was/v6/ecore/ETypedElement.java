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
 * Java class for ETypedElement complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="ETypedElement"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}ENamedElement"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="eType" type="{http://www.eclipse.org/emf/2002/Ecore}EClassifier"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="eType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="lowerBound" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ordered" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="unique" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="upperBound" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ETypedElement", propOrder = {"eClassifierETypes"})
public class ETypedElement extends ENamedElement {

    @XmlElement(name = "eType")
    protected List<EClassifier> eClassifierETypes;
    @XmlAttribute
    protected String eType;
    @XmlAttribute
    protected Integer lowerBound;
    @XmlAttribute
    protected Boolean ordered;
    @XmlAttribute
    protected Boolean unique;
    @XmlAttribute
    protected Integer upperBound;

    /**
     * Gets the value of the eClassifierETypes property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the eClassifierETypes property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEClassifierETypes().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EClassifier }
     */
    public List<EClassifier> getEClassifierETypes() {
        if (eClassifierETypes == null) {
            eClassifierETypes = new ArrayList<EClassifier>();
        }
        return this.eClassifierETypes;
    }

    /**
     * Gets the value of the eType property.
     *
     * @return possible object is {@link String }
     */
    public String getEType() {
        return eType;
    }

    /**
     * Sets the value of the eType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEType(final String value) {
        this.eType = value;
    }

    /**
     * Gets the value of the lowerBound property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getLowerBound() {
        return lowerBound;
    }

    /**
     * Sets the value of the lowerBound property.
     *
     * @param value allowed object is {@link Integer }
     */
    public void setLowerBound(final Integer value) {
        this.lowerBound = value;
    }

    /**
     * Gets the value of the ordered property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isOrdered() {
        return ordered;
    }

    /**
     * Sets the value of the ordered property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setOrdered(final Boolean value) {
        this.ordered = value;
    }

    /**
     * Gets the value of the unique property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isUnique() {
        return unique;
    }

    /**
     * Sets the value of the unique property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setUnique(final Boolean value) {
        this.unique = value;
    }

    /**
     * Gets the value of the upperBound property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getUpperBound() {
        return upperBound;
    }

    /**
     * Sets the value of the upperBound property.
     *
     * @param value allowed object is {@link Integer }
     */
    public void setUpperBound(final Integer value) {
        this.upperBound = value;
    }

}
