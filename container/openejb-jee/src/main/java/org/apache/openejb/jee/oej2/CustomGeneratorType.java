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

package org.apache.openejb.jee.oej2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Handles a user-provided generator.  You deploy any old generator
 *                 as a GBean, and then point to that GBean here.  The generator
 *                 should implement org.tranql.pkgenerator.PrimaryKeyGenerator.
 *             
 * 
 * <p>Java class for custom-generatorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="custom-generatorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="generator-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="primary-key-class" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "custom-generatorType", namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1", propOrder = {
    "generatorName",
    "primaryKeyClass"
})
public class CustomGeneratorType {

    @XmlElement(name = "generator-name", namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1", required = true)
    protected java.lang.String generatorName;
    @XmlElement(name = "primary-key-class", namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1", required = true)
    protected java.lang.String primaryKeyClass;

    /**
     * Gets the value of the generatorName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getGeneratorName() {
        return generatorName;
    }

    /**
     * Sets the value of the generatorName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setGeneratorName(java.lang.String value) {
        this.generatorName = value;
    }

    /**
     * Gets the value of the primaryKeyClass property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    /**
     * Sets the value of the primaryKeyClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setPrimaryKeyClass(java.lang.String value) {
        this.primaryKeyClass = value;
    }

}
