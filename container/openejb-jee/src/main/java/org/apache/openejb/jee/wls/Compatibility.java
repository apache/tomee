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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for compatibility complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="compatibility"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="serialize-byte-array-to-oracle-blob" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="serialize-char-array-to-bytes" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="allow-readonly-create-and-remove" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="disable-string-trimming" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="finders-return-nulls" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "compatibility", propOrder = {
    "serializeByteArrayToOracleBlob",
    "serializeCharArrayToBytes",
    "allowReadonlyCreateAndRemove",
    "disableStringTrimming",
    "findersReturnNulls"
})
public class Compatibility {

    @XmlElement(name = "serialize-byte-array-to-oracle-blob")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean serializeByteArrayToOracleBlob;
    @XmlElement(name = "serialize-char-array-to-bytes")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean serializeCharArrayToBytes;
    @XmlElement(name = "allow-readonly-create-and-remove")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean allowReadonlyCreateAndRemove;
    @XmlElement(name = "disable-string-trimming")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean disableStringTrimming;
    @XmlElement(name = "finders-return-nulls")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean findersReturnNulls;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the serializeByteArrayToOracleBlob property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getSerializeByteArrayToOracleBlob() {
        return serializeByteArrayToOracleBlob;
    }

    /**
     * Sets the value of the serializeByteArrayToOracleBlob property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSerializeByteArrayToOracleBlob(final Boolean value) {
        this.serializeByteArrayToOracleBlob = value;
    }

    /**
     * Gets the value of the serializeCharArrayToBytes property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getSerializeCharArrayToBytes() {
        return serializeCharArrayToBytes;
    }

    /**
     * Sets the value of the serializeCharArrayToBytes property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSerializeCharArrayToBytes(final Boolean value) {
        this.serializeCharArrayToBytes = value;
    }

    /**
     * Gets the value of the allowReadonlyCreateAndRemove property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getAllowReadonlyCreateAndRemove() {
        return allowReadonlyCreateAndRemove;
    }

    /**
     * Sets the value of the allowReadonlyCreateAndRemove property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setAllowReadonlyCreateAndRemove(final Boolean value) {
        this.allowReadonlyCreateAndRemove = value;
    }

    /**
     * Gets the value of the disableStringTrimming property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getDisableStringTrimming() {
        return disableStringTrimming;
    }

    /**
     * Sets the value of the disableStringTrimming property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setDisableStringTrimming(final Boolean value) {
        this.disableStringTrimming = value;
    }

    /**
     * Gets the value of the findersReturnNulls property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getFindersReturnNulls() {
        return findersReturnNulls;
    }

    /**
     * Sets the value of the findersReturnNulls property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setFindersReturnNulls(final Boolean value) {
        this.findersReturnNulls = value;
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
