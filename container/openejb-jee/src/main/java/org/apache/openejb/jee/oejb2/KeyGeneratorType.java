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
 * Primary Key generation element.
 *
 * If this is present, a key generator GBean will be created
 * and configured to generate IDs for the surrounding object.
 *
 *
 * <p>Java class for key-generatorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="key-generatorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="uuid" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType"/&gt;
 *         &lt;element name="sequence-table" type="{http://tomee.apache.org/xml/ns/pkgen-2.1}sequence-tableType"/&gt;
 *         &lt;element name="auto-increment-table" type="{http://tomee.apache.org/xml/ns/pkgen-2.1}auto-increment-tableType"/&gt;
 *         &lt;element name="sql-generator" type="{http://tomee.apache.org/xml/ns/pkgen-2.1}sql-generatorType"/&gt;
 *         &lt;element name="custom-generator" type="{http://tomee.apache.org/xml/ns/pkgen-2.1}custom-generatorType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "key-generatorType", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1", propOrder = {
    "uuid",
    "sequenceTable",
    "autoIncrementTable",
    "sqlGenerator",
    "customGenerator"
})
public class KeyGeneratorType {

    @XmlElement(name = "uuid", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1")
    protected EmptyType uuid;
    @XmlElement(name = "sequence-table", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1")
    protected SequenceTableType sequenceTable;
    @XmlElement(name = "auto-increment-table", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1")
    protected AutoIncrementTableType autoIncrementTable;
    @XmlElement(name = "sql-generator", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1")
    protected SqlGeneratorType sqlGenerator;
    @XmlElement(name = "custom-generator", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1")
    protected CustomGeneratorType customGenerator;

    /**
     * Gets the value of the uuid property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isUuid() {
        return uuid != null;
    }

    /**
     * Sets the value of the uuid property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setUuid(final boolean value) {
        this.uuid = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the sequenceTable property.
     *
     * @return possible object is
     * {@link SequenceTableType }
     */
    public SequenceTableType getSequenceTable() {
        return sequenceTable;
    }

    /**
     * Sets the value of the sequenceTable property.
     *
     * @param value allowed object is
     *              {@link SequenceTableType }
     */
    public void setSequenceTable(final SequenceTableType value) {
        this.sequenceTable = value;
    }

    /**
     * Gets the value of the autoIncrementTable property.
     *
     * @return possible object is
     * {@link AutoIncrementTableType }
     */
    public AutoIncrementTableType getAutoIncrementTable() {
        return autoIncrementTable;
    }

    /**
     * Sets the value of the autoIncrementTable property.
     *
     * @param value allowed object is
     *              {@link AutoIncrementTableType }
     */
    public void setAutoIncrementTable(final AutoIncrementTableType value) {
        this.autoIncrementTable = value;
    }

    /**
     * Gets the value of the sqlGenerator property.
     *
     * @return possible object is
     * {@link SqlGeneratorType }
     */
    public SqlGeneratorType getSqlGenerator() {
        return sqlGenerator;
    }

    /**
     * Sets the value of the sqlGenerator property.
     *
     * @param value allowed object is
     *              {@link SqlGeneratorType }
     */
    public void setSqlGenerator(final SqlGeneratorType value) {
        this.sqlGenerator = value;
    }

    /**
     * Gets the value of the customGenerator property.
     *
     * @return possible object is
     * {@link CustomGeneratorType }
     */
    public CustomGeneratorType getCustomGenerator() {
        return customGenerator;
    }

    /**
     * Sets the value of the customGenerator property.
     *
     * @param value allowed object is
     *              {@link CustomGeneratorType }
     */
    public void setCustomGenerator(final CustomGeneratorType value) {
        this.customGenerator = value;
    }

}
