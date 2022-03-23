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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for referenceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="referenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"&gt;
 *       &lt;attribute name="nameAttribute" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "referenceType", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
public class ReferenceType
    extends DepPatternType {

    @XmlAttribute(name = "name")
    protected String nameAttribute;

    /**
     * Gets the value of the nameAttribute property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getNameAttribute() {
        return nameAttribute;
    }

    /**
     * Sets the value of the nameAttribute property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNameAttribute(final String value) {
        this.nameAttribute = value;
    }

}
