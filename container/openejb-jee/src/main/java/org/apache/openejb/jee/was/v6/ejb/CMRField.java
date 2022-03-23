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
package org.apache.openejb.jee.was.v6.ejb;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.openejb.jee.was.v6.java.JavaClass;

/**
 * @since J2EE1.3 Describes the bean provider's view of a relationship. It
 * consists of an optional description, and the name and the class type
 * of a field in the source of a role of a relationship. The
 * CMRField::name element corresponds to the name used for the get and
 * set accessor methods for the relationship. The CMRField::type element
 * is used only for collection-valued CMRFields. It specifies the type of
 * the collection that is used (a java class name).
 *
 *
 *
 *
 * Java class for CMRField complex type.
 *
 *
 * The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name="CMRField"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ejb.xmi}CMPAttribute"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="collectionType" type="{java.xmi}JavaClass"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="collectionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CMRField", propOrder = {"collectionTypes"})
public class CMRField extends CMPAttribute {

    @XmlElement(name = "collectionType")
    protected List<JavaClass> collectionTypes;
    @XmlAttribute
    protected String collectionType;

    /**
     * Gets the value of the collectionTypes property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the collectionTypes property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getCollectionTypes().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getCollectionTypes() {
        if (collectionTypes == null) {
            collectionTypes = new ArrayList<JavaClass>();
        }
        return this.collectionTypes;
    }

    /**
     * Gets the value of the collectionType property.
     *
     * @return possible object is {@link String }
     */
    public String getCollectionType() {
        return collectionType;
    }

    /**
     * Sets the value of the collectionType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCollectionType(final String value) {
        this.collectionType = value;
    }

}
