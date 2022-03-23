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

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.openejb.jee.was.v6.ecore.EOperation;

/**
 *
 * Java class for Method complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="Method"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EOperation"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="parameters" type="{java.xmi}JavaParameter"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="javaExceptions" type="{java.xmi}JavaClass"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="source" type="{java.xmi}Block"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="abstract" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="constructor" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="final" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="javaExceptions" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="javaVisibility" type="{java.xmi}JavaVisibilityKind" /&gt;
 *       &lt;attribute name="native" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="static" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="synchronized" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Method", propOrder = {"parameters",
    "javaClassJavaExceptions", "sources"})
public class Method extends EOperation {

    protected List<JavaParameter> parameters;
    @XmlElement(name = "javaExceptions")
    protected List<JavaClass> javaClassJavaExceptions;
    @XmlElement(name = "source")
    protected List<Block> sources;
    @XmlAttribute(name = "abstract")
    protected Boolean isAbstract;
    @XmlAttribute
    protected Boolean constructor;
    @XmlAttribute(name = "final")
    protected Boolean isFinal;
    @XmlAttribute
    protected String javaExceptions;
    @XmlAttribute
    protected JavaVisibilityEnum javaVisibility;
    @XmlAttribute(name = "native")
    protected Boolean isNative;
    @XmlAttribute
    protected String source;
    @XmlAttribute(name = "static")
    protected Boolean isStatic;
    @XmlAttribute(name = "synchronized")
    protected Boolean isSynchronized;

    /**
     * Gets the value of the parameters property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the parameters property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getParameters().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaParameter }
     */
    public List<JavaParameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<JavaParameter>();
        }
        return this.parameters;
    }

    /**
     * Gets the value of the javaClassJavaExceptions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the javaClassJavaExceptions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getJavaClassJavaExceptions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getJavaClassJavaExceptions() {
        if (javaClassJavaExceptions == null) {
            javaClassJavaExceptions = new ArrayList<JavaClass>();
        }
        return this.javaClassJavaExceptions;
    }

    /**
     * Gets the value of the sources property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the sources property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSources().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link Block }
     */
    public List<Block> getSources() {
        if (sources == null) {
            sources = new ArrayList<Block>();
        }
        return this.sources;
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
     * Gets the value of the constructor property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isConstructor() {
        return constructor;
    }

    /**
     * Sets the value of the constructor property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setConstructor(final Boolean value) {
        this.constructor = value;
    }

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
     * Gets the value of the javaExceptions property.
     *
     * @return possible object is {@link String }
     */
    public String getJavaExceptions() {
        return javaExceptions;
    }

    /**
     * Sets the value of the javaExceptions property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJavaExceptions(final String value) {
        this.javaExceptions = value;
    }

    /**
     * Gets the value of the javaVisibility property.
     *
     * @return possible object is {@link JavaVisibilityEnum }
     */
    public JavaVisibilityEnum getJavaVisibility() {
        return javaVisibility;
    }

    /**
     * Sets the value of the javaVisibility property.
     *
     * @param value allowed object is {@link JavaVisibilityEnum }
     */
    public void setJavaVisibility(final JavaVisibilityEnum value) {
        this.javaVisibility = value;
    }

    /**
     * Gets the value of the isNative property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsNative() {
        return isNative;
    }

    /**
     * Sets the value of the isNative property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsNative(final Boolean value) {
        this.isNative = value;
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

    /**
     * Gets the value of the isStatic property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsStatic() {
        return isStatic;
    }

    /**
     * Sets the value of the isStatic property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsStatic(final Boolean value) {
        this.isStatic = value;
    }

    /**
     * Gets the value of the isSynchronized property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsSynchronized() {
        return isSynchronized;
    }

    /**
     * Sets the value of the isSynchronized property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsSynchronized(final Boolean value) {
        this.isSynchronized = value;
    }

}
