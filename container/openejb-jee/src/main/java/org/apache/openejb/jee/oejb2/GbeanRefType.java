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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gbean-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="gbean-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://geronimo.apache.org/xml/ns/naming-1.2}abstract-naming-entryType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ref-type" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gbean-refType", propOrder = {
    "refName",
    "refType",
    "pattern"
})
public class GbeanRefType
    extends AbstractNamingEntryType {

    @XmlElement(name = "ref-name", required = true)
    protected String refName;
    @XmlElement(name = "ref-type")
    protected List<String> refType;
    protected List<PatternType> pattern;

    /**
     * Gets the value of the refName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRefName() {
        return refName;
    }

    /**
     * Sets the value of the refName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRefName(final String value) {
        this.refName = value;
    }

    /**
     * Gets the value of the refType property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the refType property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRefType().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getRefType() {
        if (refType == null) {
            refType = new ArrayList<String>();
        }
        return this.refType;
    }

    /**
     * Gets the value of the pattern property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pattern property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPattern().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link PatternType }
     */
    public List<PatternType> getPattern() {
        if (pattern == null) {
            pattern = new ArrayList<PatternType>();
        }
        return this.pattern;
    }

}
