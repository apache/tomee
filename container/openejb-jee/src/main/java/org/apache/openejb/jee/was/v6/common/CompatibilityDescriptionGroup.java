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
package org.apache.openejb.jee.was.v6.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 *
 * Java class for CompatibilityDescriptionGroup complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="CompatibilityDescriptionGroup"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{common.xmi}DescriptionGroup"&gt;
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="largeIcon" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="smallIcon" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompatibilityDescriptionGroup")
public class CompatibilityDescriptionGroup extends DescriptionGroup {

    @XmlAttribute
    protected String description;
    @XmlAttribute
    protected String displayName;
    @XmlAttribute
    protected String largeIcon;
    @XmlAttribute
    protected String smallIcon;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDescription(final String value) {
        this.description = value;
    }

    /**
     * Gets the value of the displayName property.
     *
     * @return possible object is {@link String }
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDisplayName(final String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the largeIcon property.
     *
     * @return possible object is {@link String }
     */
    public String getLargeIcon() {
        return largeIcon;
    }

    /**
     * Sets the value of the largeIcon property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLargeIcon(final String value) {
        this.largeIcon = value;
    }

    /**
     * Gets the value of the smallIcon property.
     *
     * @return possible object is {@link String }
     */
    public String getSmallIcon() {
        return smallIcon;
    }

    /**
     * Sets the value of the smallIcon property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSmallIcon(final String value) {
        this.smallIcon = value;
    }

}
