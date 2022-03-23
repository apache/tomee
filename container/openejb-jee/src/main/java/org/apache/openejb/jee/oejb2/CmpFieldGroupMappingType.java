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
 * <p>Java class for cmp-field-group-mappingType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="cmp-field-group-mappingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="cmp-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmp-field-group-mappingType", propOrder = {
    "groupName",
    "cmpFieldName"
})
public class CmpFieldGroupMappingType {

    @XmlElement(name = "group-name", required = true)
    protected String groupName;
    @XmlElement(name = "cmp-field-name", required = true)
    protected String cmpFieldName;

    /**
     * Gets the value of the groupName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGroupName(final String value) {
        this.groupName = value;
    }

    /**
     * Gets the value of the cmpFieldName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCmpFieldName() {
        return cmpFieldName;
    }

    /**
     * Sets the value of the cmpFieldName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCmpFieldName(final String value) {
        this.cmpFieldName = value;
    }

}
