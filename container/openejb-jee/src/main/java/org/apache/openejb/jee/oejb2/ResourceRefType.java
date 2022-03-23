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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for resource-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/&gt;
 *           &lt;element name="resource-link" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-refType", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", propOrder = {
    "refName",
    "pattern",
    "resourceLink",
    "url"
})
public class ResourceRefType {

    @XmlElement(name = "ref-name", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", required = true)
    protected String refName;
    @XmlElement(name = "pattern", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected PatternType pattern;
    @XmlElement(name = "resource-link", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String resourceLink;
    @XmlElement(name = "url", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String url;

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
     * Gets the value of the resourceLink property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getResourceLink() {
        return resourceLink;
    }

    /**
     * Sets the value of the resourceLink property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResourceLink(final String value) {
        this.resourceLink = value;
    }

    /**
     * Gets the value of the url property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUrl(final String value) {
        this.url = value;
    }

}
