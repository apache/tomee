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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for groupType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="groupType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="cmp-field-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="cmr-field" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="cmr-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "groupType", propOrder = {
    "groupName",
    "cmpFieldName",
    "cmrField"
})
public class GroupType {

    @XmlElement(name = "group-name", required = true)
    protected String groupName;
    @XmlElement(name = "cmp-field-name")
    protected List<String> cmpFieldName;
    @XmlElement(name = "cmr-field")
    protected List<GroupType.CmrField> cmrField;

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
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmpFieldName property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmpFieldName().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCmpFieldName() {
        if (cmpFieldName == null) {
            cmpFieldName = new ArrayList<String>();
        }
        return this.cmpFieldName;
    }

    /**
     * Gets the value of the cmrField property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmrField property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmrField().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link GroupType.CmrField }
     */
    public List<GroupType.CmrField> getCmrField() {
        if (cmrField == null) {
            cmrField = new ArrayList<GroupType.CmrField>();
        }
        return this.cmrField;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="cmr-field-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cmrFieldName",
        "groupName"
    })
    public static class CmrField {

        @XmlElement(name = "cmr-field-name", required = true)
        protected String cmrFieldName;
        @XmlElement(name = "group-name")
        protected String groupName;

        /**
         * Gets the value of the cmrFieldName property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCmrFieldName() {
            return cmrFieldName;
        }

        /**
         * Sets the value of the cmrFieldName property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCmrFieldName(final String value) {
            this.cmrFieldName = value;
        }

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

    }

}
