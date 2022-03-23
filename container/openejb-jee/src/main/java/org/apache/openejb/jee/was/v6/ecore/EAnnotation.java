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
 * Java class for EAnnotation complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EAnnotation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EModelElement"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="details" type="{http://www.eclipse.org/emf/2002/Ecore}EStringToStringMapEntry"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="contents" type="{http://www.eclipse.org/emf/2002/Ecore}EObject"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="references" type="{http://www.eclipse.org/emf/2002/Ecore}EObject"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="references" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EAnnotation", propOrder = {"details", "contents",
    "eObjectReferences"})
public class EAnnotation extends EModelElement {

    protected List<EStringToStringMapEntry> details;
    protected List<EObject> contents;
    @XmlElement(name = "references")
    protected List<EObject> eObjectReferences;
    @XmlAttribute
    protected String references;
    @XmlAttribute
    protected String source;

    /**
     * Gets the value of the details property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the details property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDetails().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EStringToStringMapEntry }
     */
    public List<EStringToStringMapEntry> getDetails() {
        if (details == null) {
            details = new ArrayList<EStringToStringMapEntry>();
        }
        return this.details;
    }

    /**
     * Gets the value of the contents property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the contents property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getContents().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link EObject }
     */
    public List<EObject> getContents() {
        if (contents == null) {
            contents = new ArrayList<EObject>();
        }
        return this.contents;
    }

    /**
     * Gets the value of the eObjectReferences property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the eObjectReferences property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEObjectReferences().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link EObject }
     */
    public List<EObject> getEObjectReferences() {
        if (eObjectReferences == null) {
            eObjectReferences = new ArrayList<EObject>();
        }
        return this.eObjectReferences;
    }

    /**
     * Gets the value of the references property.
     *
     * @return possible object is {@link String }
     */
    public String getReferences() {
        return references;
    }

    /**
     * Sets the value of the references property.
     *
     * @param value allowed object is {@link String }
     */
    public void setReferences(final String value) {
        this.references = value;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link String }
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSource(final String value) {
        this.source = value;
    }

}
