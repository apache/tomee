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
import javax.validation.executable.ExecutableType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for default-validated-executable-typesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="default-validated-executable-typesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="executable-type" maxOccurs="unbounded">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="NONE"/>
 *               &lt;enumeration value="CONSTRUCTORS"/>
 *               &lt;enumeration value="NON_GETTER_METHODS"/>
 *               &lt;enumeration value="GETTER_METHODS"/>
 *               &lt;enumeration value="ALL"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "default-validated-executable-typesType", namespace = "http://jboss.org/xml/ns/javax/validation/configuration", propOrder = {
    "executableType"
})
public class DefaultValidatedExecutableTypesType {

    @XmlElement(name = "executable-type", required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected List<ExecutableType> executableType;

    /**
     * Gets the value of the executableType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the executableType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExecutableType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<ExecutableType> getExecutableType() {
        if (executableType == null) {
            executableType = new ArrayList<ExecutableType>();
        }
        return this.executableType;
    }

}
