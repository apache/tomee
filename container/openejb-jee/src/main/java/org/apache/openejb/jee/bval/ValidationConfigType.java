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
package org.apache.openejb.jee.bval;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour validation-configType complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="validation-configType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="default-provider" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="message-interpolator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="traversable-resolver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="constraint-validator-factory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="parameter-name-provider" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="clock-provider" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="value-extractor" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="executable-validation" type="{http://xmlns.jcp.org/xml/ns/validation/configuration}executable-validationType" minOccurs="0"/&gt;
 *         &lt;element name="constraint-mapping" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="property" type="{http://xmlns.jcp.org/xml/ns/validation/configuration}propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" type="{http://xmlns.jcp.org/xml/ns/validation/configuration}versionType" fixed="2.0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "validation-configType", namespace = "http://xmlns.jcp.org/xml/ns/validation/configuration", propOrder = {
        "defaultProvider",
        "messageInterpolator",
        "traversableResolver",
        "constraintValidatorFactory",
        "parameterNameProvider",
        "clockProvider",
        "valueExtractor",
        "executableValidation",
        "constraintMapping",
        "property"
})
public class ValidationConfigType {

    @XmlElement(name = "default-provider")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected String defaultProvider;
    @XmlElement(name = "message-interpolator")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected String messageInterpolator;
    @XmlElement(name = "traversable-resolver")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected String traversableResolver;
    @XmlElement(name = "constraint-validator-factory")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected String constraintValidatorFactory;
    @XmlElement(name = "parameter-name-provider")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected String parameterNameProvider;
    @XmlElement(name = "clock-provider")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected String clockProvider;
    @XmlElement(name = "value-extractor")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected List<String> valueExtractor;
    @XmlElement(name = "executable-validation")
    protected ExecutableValidationType executableValidation;
    @XmlElement(name = "constraint-mapping")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected List<String> constraintMapping;
    protected List<PropertyType> property;
    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter.class)
    protected String version;

    /**
     * Obtient la valeur de la propriété defaultProvider.
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
     * Définit la valeur de la propriété defaultProvider.
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
     * Obtient la valeur de la propriété messageInterpolator.
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
     * Définit la valeur de la propriété messageInterpolator.
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
     * Obtient la valeur de la propriété traversableResolver.
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
     * Définit la valeur de la propriété traversableResolver.
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
     * Obtient la valeur de la propriété constraintValidatorFactory.
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
     * Définit la valeur de la propriété constraintValidatorFactory.
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
     * Obtient la valeur de la propriété parameterNameProvider.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getParameterNameProvider() {
        return parameterNameProvider;
    }

    /**
     * Définit la valeur de la propriété parameterNameProvider.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setParameterNameProvider(String value) {
        this.parameterNameProvider = value;
    }

    /**
     * Obtient la valeur de la propriété clockProvider.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClockProvider() {
        return clockProvider;
    }

    /**
     * Définit la valeur de la propriété clockProvider.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClockProvider(String value) {
        this.clockProvider = value;
    }

    /**
     * Gets the value of the valueExtractor property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueExtractor property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueExtractor().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getValueExtractor() {
        if (valueExtractor == null) {
            valueExtractor = new ArrayList<String>();
        }
        return this.valueExtractor;
    }

    /**
     * Obtient la valeur de la propriété executableValidation.
     *
     * @return
     *     possible object is
     *     {@link ExecutableValidationType }
     *
     */
    public ExecutableValidationType getExecutableValidation() {
        return executableValidation;
    }

    /**
     * Définit la valeur de la propriété executableValidation.
     *
     * @param value
     *     allowed object is
     *     {@link ExecutableValidationType }
     *
     */
    public void setExecutableValidation(ExecutableValidationType value) {
        this.executableValidation = value;
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
     * {@link String }
     *
     *
     */
    public List<String> getConstraintMapping() {
        if (constraintMapping == null) {
            constraintMapping = new ArrayList<String>();
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

    /**
     * Obtient la valeur de la propriété version.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVersion() {
        if (version == null) {
            return "2.0";
        } else {
            return version;
        }
    }

    /**
     * Définit la valeur de la propriété version.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
