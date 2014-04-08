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

package org.apache.openejb.jee.oejb2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gbeanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="gbeanType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://geronimo.apache.org/xml/ns/deployment-1.2}abstract-serviceType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="attribute" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}attributeType"/>
 *         &lt;element name="xml-attribute" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}xml-attributeType"/>
 *         &lt;element name="reference" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}referenceType"/>
 *         &lt;element name="references" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}referencesType"/>
 *         &lt;element name="xml-reference" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}xml-attributeType"/>
 *         &lt;element name="dependency" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/>
 *       &lt;/choice>
 *       &lt;attribute name="class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gbeanType", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", propOrder = {
    "attributeOrXmlAttributeOrReference"
})
public class GbeanType
    extends AbstractServiceType
{

    @XmlElementRefs({
        @XmlElementRef(name = "reference", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class),
        @XmlElementRef(name = "attribute", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class),
        @XmlElementRef(name = "xml-attribute", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class),
        @XmlElementRef(name = "xml-reference", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class),
        @XmlElementRef(name = "dependency", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class),
        @XmlElementRef(name = "references", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> attributeOrXmlAttributeOrReference;
    @XmlAttribute(name = "class", required = true)
    protected String clazz;
    @XmlAttribute(required = true)
    protected String name;

    /**
     * Gets the value of the attributeOrXmlAttributeOrReference property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeOrXmlAttributeOrReference property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeOrXmlAttributeOrReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link XmlAttributeType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     * {@link JAXBElement }{@code <}{@link PatternType }{@code >}
     * {@link JAXBElement }{@code <}{@link AttributeType }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlAttributeType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReferencesType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getAttributeOrXmlAttributeOrReference() {
        if (attributeOrXmlAttributeOrReference == null) {
            attributeOrXmlAttributeOrReference = new ArrayList<JAXBElement<?>>();
        }
        return this.attributeOrXmlAttributeOrReference;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
