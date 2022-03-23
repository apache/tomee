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
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;

/**
 *
 * Java class for Replace complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="Replace"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.omg.org/XMI}Difference"&gt;
 *       &lt;attribute name="position" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="replacement" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Replace")
public class Replace extends Difference {

    @XmlAttribute
    protected String position;
    @XmlAttribute(name = "replacement")
    @XmlIDREF
    protected List<Object> replacements;

    /**
     * Gets the value of the position property.
     *
     * @return possible object is {@link String }
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPosition(final String value) {
        this.position = value;
    }

    /**
     * Gets the value of the replacements property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the replacements property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getReplacements().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link Object }
     */
    public List<Object> getReplacements() {
        if (replacements == null) {
            replacements = new ArrayList<Object>();
        }
        return this.replacements;
    }

}
