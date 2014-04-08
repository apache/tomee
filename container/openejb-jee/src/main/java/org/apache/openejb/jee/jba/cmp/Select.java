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

package org.apache.openejb.jee.jba.cmp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://jboss.org}distinct" minOccurs="0"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element ref="{http://jboss.org}ejb-name"/>
 *           &lt;element ref="{http://jboss.org}field-name" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://jboss.org}alias" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}additional-columns" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "distinct",
    "ejbName",
    "fieldName",
    "alias",
    "additionalColumns"
})
@XmlRootElement(name = "select")
public class Select {

    protected Distinct distinct;
    @XmlElement(name = "ejb-name")
    protected EjbName ejbName;
    @XmlElement(name = "field-name")
    protected FieldName fieldName;
    protected Alias alias;
    @XmlElement(name = "additional-columns")
    protected AdditionalColumns additionalColumns;

    /**
     * Gets the value of the distinct property.
     * 
     * @return
     *     possible object is
     *     {@link Distinct }
     *     
     */
    public Distinct getDistinct() {
        return distinct;
    }

    /**
     * Sets the value of the distinct property.
     * 
     * @param value
     *     allowed object is
     *     {@link Distinct }
     *     
     */
    public void setDistinct(Distinct value) {
        this.distinct = value;
    }

    /**
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link EjbName }
     *     
     */
    public EjbName getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     * 
     * @param value
     *     allowed object is
     *     {@link EjbName }
     *     
     */
    public void setEjbName(EjbName value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the fieldName property.
     * 
     * @return
     *     possible object is
     *     {@link FieldName }
     *     
     */
    public FieldName getFieldName() {
        return fieldName;
    }

    /**
     * Sets the value of the fieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldName }
     *     
     */
    public void setFieldName(FieldName value) {
        this.fieldName = value;
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link Alias }
     *     
     */
    public Alias getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link Alias }
     *     
     */
    public void setAlias(Alias value) {
        this.alias = value;
    }

    /**
     * Gets the value of the additionalColumns property.
     * 
     * @return
     *     possible object is
     *     {@link AdditionalColumns }
     *     
     */
    public AdditionalColumns getAdditionalColumns() {
        return additionalColumns;
    }

    /**
     * Sets the value of the additionalColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdditionalColumns }
     *     
     */
    public void setAdditionalColumns(AdditionalColumns value) {
        this.additionalColumns = value;
    }

}
