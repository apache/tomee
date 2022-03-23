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
package org.apache.openejb.jee.was.v6.xmi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 *
 * Java class for Difference complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="Difference"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="target"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;any/&gt;
 *                   &lt;/choice&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="difference" type="{http://www.omg.org/XMI}Difference"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="container" type="{http://www.omg.org/XMI}Difference"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/&gt;
 *       &lt;attribute name="container" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="target" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Difference", propOrder = {"differenceTargets", "differences",
    "differenceContainers"})
public class Difference {

    @XmlElement(name = "target")
    protected List<Difference.Target> differenceTargets;
    @XmlElement(name = "difference")
    protected List<Difference> differences;
    @XmlElement(name = "container")
    protected List<Difference> differenceContainers;
    @XmlAttribute(name = "container")
    @XmlIDREF
    protected List<Object> containers;
    @XmlAttribute(name = "target")
    @XmlIDREF
    protected List<Object> targets;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected QName type;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String version;
    @XmlAttribute
    protected String href;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    @XmlIDREF
    protected Object idref;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String label;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String uuid;

    /**
     * Gets the value of the differenceTargets property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the differenceTargets property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDifferenceTargets().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Difference.Target }
     */
    public List<Difference.Target> getDifferenceTargets() {
        if (differenceTargets == null) {
            differenceTargets = new ArrayList<Difference.Target>();
        }
        return this.differenceTargets;
    }

    /**
     * Gets the value of the differences property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the differences property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDifferences().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Difference }
     */
    public List<Difference> getDifferences() {
        if (differences == null) {
            differences = new ArrayList<Difference>();
        }
        return this.differences;
    }

    /**
     * Gets the value of the differenceContainers property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the differenceContainers property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDifferenceContainers().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Difference }
     */
    public List<Difference> getDifferenceContainers() {
        if (differenceContainers == null) {
            differenceContainers = new ArrayList<Difference>();
        }
        return this.differenceContainers;
    }

    /**
     * Gets the value of the containers property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the containers property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getContainers().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link Object }
     */
    public List<Object> getContainers() {
        if (containers == null) {
            containers = new ArrayList<Object>();
        }
        return this.containers;
    }

    /**
     * Gets the value of the targets property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the targets property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getTargets().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link Object }
     */
    public List<Object> getTargets() {
        if (targets == null) {
            targets = new ArrayList<Object>();
        }
        return this.targets;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link QName }
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link QName }
     */
    public void setType(final QName value) {
        this.type = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is {@link String }
     */
    public String getVersion() {
        if (version == null) {
            return "2.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is {@link String }
     */
    public void setVersion(final String value) {
        this.version = value;
    }

    /**
     * Gets the value of the href property.
     *
     * @return possible object is {@link String }
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHref(final String value) {
        this.href = value;
    }

    /**
     * Gets the value of the idref property.
     *
     * @return possible object is {@link Object }
     */
    public Object getIdref() {
        return idref;
    }

    /**
     * Sets the value of the idref property.
     *
     * @param value allowed object is {@link Object }
     */
    public void setIdref(final Object value) {
        this.idref = value;
    }

    /**
     * Gets the value of the label property.
     *
     * @return possible object is {@link String }
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLabel(final String value) {
        this.label = value;
    }

    /**
     * Gets the value of the uuid property.
     *
     * @return possible object is {@link String }
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUuid(final String value) {
        this.uuid = value;
    }

    /**
     *
     * Java class for anonymous complex type.
     *
     *
     * The following schema fragment specifies the expected content contained
     * within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *         &lt;any/&gt;
     *       &lt;/choice&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"elements"})
    public static class Target {

        @XmlAnyElement
        protected List<Element> elements;
        @XmlAnyAttribute
        private final Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the elements property.
         *
         *
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the elements property.
         *
         *
         * For example, to add a new item, do as follows:
         *
         * <pre>
         * getElements().add(newItem);
         * </pre>
         *
         *
         *
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         */
        public List<Element> getElements() {
            if (elements == null) {
                elements = new ArrayList<Element>();
            }
            return this.elements;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed
         * property on this class.
         *
         *
         * the map is keyed by the name of the attribute and the value is the
         * string value of the attribute.
         *
         * the map returned by this method is live, and you can add new
         * attribute by updating the map directly. Because of this design,
         * there's no setter.
         *
         * @return always non-null
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }

}
