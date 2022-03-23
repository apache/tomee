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
 * Indicates that a separate table holds a list of table name/ID
 * pairs and the server should fetch the next ID from that table.
 *
 *
 * <p>Java class for sequence-tableType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="sequence-tableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="table-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="sequence-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="batch-size" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sequence-tableType", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1", propOrder = {
    "tableName",
    "sequenceName",
    "batchSize"
})
public class SequenceTableType {

    @XmlElement(name = "table-name", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1", required = true)
    protected String tableName;
    @XmlElement(name = "sequence-name", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1", required = true)
    protected String sequenceName;
    @XmlElement(name = "batch-size", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1")
    protected int batchSize;

    /**
     * Gets the value of the tableName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTableName(final String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the sequenceName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSequenceName() {
        return sequenceName;
    }

    /**
     * Sets the value of the sequenceName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSequenceName(final String value) {
        this.sequenceName = value;
    }

    /**
     * Gets the value of the batchSize property.
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Sets the value of the batchSize property.
     */
    public void setBatchSize(final int value) {
        this.batchSize = value;
    }

}
