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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gbean-locatorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="gbean-locatorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/&gt;
 *           &lt;element name="gbean-link" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gbean-locatorType", propOrder = {
    "pattern",
    "gbeanLink"
})
public class GbeanLocatorType {

    protected PatternType pattern;
    @XmlElement(name = "gbean-link")
    protected String gbeanLink;

    /**
     * Gets the value of the pattern property.
     *
     * @return possible object is
     * {@link PatternType }
     */
    public PatternType getPattern() {
        return pattern;
    }

    /**
     * Sets the value of the pattern property.
     *
     * @param value allowed object is
     *              {@link PatternType }
     */
    public void setPattern(final PatternType value) {
        this.pattern = value;
    }

    /**
     * Gets the value of the gbeanLink property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getGbeanLink() {
        return gbeanLink;
    }

    /**
     * Sets the value of the gbeanLink property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGbeanLink(final String value) {
        this.gbeanLink = value;
    }

}
