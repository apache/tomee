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
package org.apache.openejb.jee.wls;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for field-map complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="field-map"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cmp-field" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dbms-column" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dbms-column-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="dbms-default-value" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "field-map", propOrder = {
    "cmpField",
    "dbmsColumn",
    "dbmsColumnType",
    "dbmsDefaultValue",
    "groupName"
})
public class FieldMap {

    @XmlElement(name = "cmp-field", required = true)
    protected String cmpField;
    @XmlElement(name = "dbms-column", required = true)
    protected String dbmsColumn;
    @XmlElement(name = "dbms-column-type")
    protected String dbmsColumnType;
    @XmlElement(name = "dbms-default-value")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean dbmsDefaultValue;
    @XmlElement(name = "group-name")
    protected String groupName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the cmpField property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCmpField() {
        return cmpField;
    }

    /**
     * Sets the value of the cmpField property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCmpField(final String value) {
        this.cmpField = value;
    }

    /**
     * Gets the value of the dbmsColumn property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDbmsColumn() {
        return dbmsColumn;
    }

    /**
     * Sets the value of the dbmsColumn property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDbmsColumn(final String value) {
        this.dbmsColumn = value;
    }

    /**
     * Gets the value of the dbmsColumnType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDbmsColumnType() {
        return dbmsColumnType;
    }

    /**
     * Sets the value of the dbmsColumnType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDbmsColumnType(final String value) {
        this.dbmsColumnType = value;
    }

    /**
     * Gets the value of the dbmsDefaultValue property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getDbmsDefaultValue() {
        return dbmsDefaultValue;
    }

    /**
     * Sets the value of the dbmsDefaultValue property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setDbmsDefaultValue(final Boolean value) {
        this.dbmsDefaultValue = value;
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

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
