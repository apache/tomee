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
 * Indicates that an arbitrary SQL statement should be used to
 * generate the next ID.
 *
 *
 * <p>Java class for sql-generatorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="sql-generatorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sql" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="return-type" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sql-generatorType", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1", propOrder = {
    "sql",
    "returnType"
})
public class SqlGeneratorType {

    @XmlElement(name = "sql", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1", required = true)
    protected String sql;
    @XmlElement(name = "return-type", namespace = "http://tomee.apache.org/xml/ns/pkgen-2.1", required = true)
    protected String returnType;

    /**
     * Gets the value of the sql property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSql() {
        return sql;
    }

    /**
     * Sets the value of the sql property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSql(final String value) {
        this.sql = value;
    }

    /**
     * Gets the value of the returnType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Sets the value of the returnType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReturnType(final String value) {
        this.returnType = value;
    }

}
