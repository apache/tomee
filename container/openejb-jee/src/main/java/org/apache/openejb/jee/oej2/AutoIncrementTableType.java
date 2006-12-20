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

package org.apache.openejb.jee.oej2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Handles the case where an arbitrary SQL statement is executed,
 *                 and the JDBC driver returns a new automatically generated ID.
 *                 This should not be used when the destination table itself
 *                 generates the ID (see database-generatedType), but it could be
 *                 used for a web session ID or something where there is no
 *                 naturally matching database table (but you could create one
 *                 with an AUTO_INCREMENT key, specify an insert statement here,
 *                 and then capture the newly returned ID and use it as your
 *                 web session ID).
 *             
 * 
 * <p>Java class for auto-increment-tableType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="auto-increment-tableType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sql" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="return-type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "auto-increment-tableType", namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1", propOrder = {
    "sql",
    "returnType"
})
public class AutoIncrementTableType {

    @XmlElement(namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1", required = true)
    protected java.lang.String sql;
    @XmlElement(name = "return-type", namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1", required = true)
    protected java.lang.String returnType;

    /**
     * Gets the value of the sql property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getSql() {
        return sql;
    }

    /**
     * Sets the value of the sql property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setSql(java.lang.String value) {
        this.sql = value;
    }

    /**
     * Gets the value of the returnType property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getReturnType() {
        return returnType;
    }

    /**
     * Sets the value of the returnType property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setReturnType(java.lang.String value) {
        this.returnType = value;
    }

}
