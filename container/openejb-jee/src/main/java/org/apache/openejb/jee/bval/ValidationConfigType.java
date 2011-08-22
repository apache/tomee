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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.bval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for validation-configType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="validation-configType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="default-provider" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="message-interpolator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="traversable-resolver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="constraint-validator-factory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="constraint-mapping" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="property" type="{http://jboss.org/xml/ns/javax/validation/configuration}propertyType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "validation-configType", namespace = "http://jboss.org/xml/ns/javax/validation/configuration", propOrder = {
    "defaultProvider",
    "messageInterpolator",
    "traversableResolver",
    "constraintValidatorFactory",
    "constraintMapping",
    "property"
})
public class ValidationConfigType {

    @XmlElement(name = "default-provider", namespace = "http://jboss.org/xml/ns/javax/validation/configuration")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String defaultProvider;
    @XmlElement(name = "message-interpolator", namespace = "http://jboss.org/xml/ns/javax/validation/configuration")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String messageInterpolator;
    @XmlElement(name = "traversable-resolver", namespace = "http://jboss.org/xml/ns/javax/validation/configuration")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String traversableResolver;
    @XmlElement(name = "constraint-validator-factory", namespace = "http://jboss.org/xml/ns/javax/validation/configuration")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String constraintValidatorFactory;
    @XmlElementRef(name = "constraint-mapping", namespace = "http://jboss.org/xml/ns/javax/validation/configuration", type = JAXBElement.class)
    protected List<JAXBElement<String>> constraintMapping;
    @XmlElement(namespace = "http://jboss.org/xml/ns/javax/validation/configuration")
    protected List<PropertyType> property;

    /**
     * Gets the value of the defaultProvider property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultProvider() {
        return defaultProvider;
    }

    /**
     * Sets the value of the defaultProvider property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultProvider(String value) {
        this.defaultProvider = value;
    }

    /**
     * Gets the value of the messageInterpolator property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMessageInterpolator() {
        return messageInterpolator;
    }

    /**
     * Sets the value of the messageInterpolator property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMessageInterpolator(String value) {
        this.messageInterpolator = value;
    }

    /**
     * Gets the value of the traversableResolver property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTraversableResolver() {
        return traversableResolver;
    }

    /**
     * Sets the value of the traversableResolver property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTraversableResolver(String value) {
        this.traversableResolver = value;
    }

    /**
     * Gets the value of the constraintValidatorFactory property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getConstraintValidatorFactory() {
        return constraintValidatorFactory;
    }

    /**
     * Sets the value of the constraintValidatorFactory property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setConstraintValidatorFactory(String value) {
        this.constraintValidatorFactory = value;
    }

    /**
     * Gets the value of the constraintMapping property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the constraintMapping property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConstraintMapping().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     *
     */
    public List<JAXBElement<String>> getConstraintMapping() {
        if (constraintMapping == null) {
            constraintMapping = new ArrayList<JAXBElement<String>>();
        }
        return this.constraintMapping;
    }

    /**
     * Gets the value of the property property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyType }
     *
     *
     */
    public List<PropertyType> getProperty() {
        if (property == null) {
            property = new ArrayList<PropertyType>();
        }
        return this.property;
    }

}
