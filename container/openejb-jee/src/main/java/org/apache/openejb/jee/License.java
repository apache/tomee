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
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * The licenseType specifies licensing requirements for the
 * resource adapter module. This type specifies whether a
 * license is required to deploy and use this resource adapter,
 * and an optional description of the licensing terms
 * (examples: duration of license, number of connection
 * restrictions). It is used by the license element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "licenseType", propOrder = {
        "description",
        "licenseRequired"
})
public class License {

    protected List<Text> description;
    @XmlElement(name = "license-required")
    protected boolean licenseRequired;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the licenseRequired property.
     */
    public boolean isLicenseRequired() {
        return licenseRequired;
    }

    /**
     * Sets the value of the licenseRequired property.
     */
    public void setLicenseRequired(boolean value) {
        this.licenseRequired = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
