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

package org.apache.openejb.jee.jba.cmp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://jboss.org}java-type"/>
 *         &lt;element ref="{http://jboss.org}jdbc-type"/>
 *         &lt;element ref="{http://jboss.org}sql-type"/>
 *         &lt;element ref="{http://jboss.org}param-setter" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}result-reader" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "javaType",
    "jdbcType",
    "sqlType",
    "paramSetter",
    "resultReader"
})
@XmlRootElement(name = "mapping")
public class Mapping {

    @XmlElement(name = "java-type", required = true)
    protected JavaType javaType;
    @XmlElement(name = "jdbc-type", required = true)
    protected JdbcType jdbcType;
    @XmlElement(name = "sql-type", required = true)
    protected SqlType sqlType;
    @XmlElement(name = "param-setter")
    protected ParamSetter paramSetter;
    @XmlElement(name = "result-reader")
    protected ResultReader resultReader;

    /**
     * Gets the value of the javaType property.
     * 
     * @return
     *     possible object is
     *     {@link JavaType }
     *     
     */
    public JavaType getJavaType() {
        return javaType;
    }

    /**
     * Sets the value of the javaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaType }
     *     
     */
    public void setJavaType(JavaType value) {
        this.javaType = value;
    }

    /**
     * Gets the value of the jdbcType property.
     * 
     * @return
     *     possible object is
     *     {@link JdbcType }
     *     
     */
    public JdbcType getJdbcType() {
        return jdbcType;
    }

    /**
     * Sets the value of the jdbcType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JdbcType }
     *     
     */
    public void setJdbcType(JdbcType value) {
        this.jdbcType = value;
    }

    /**
     * Gets the value of the sqlType property.
     * 
     * @return
     *     possible object is
     *     {@link SqlType }
     *     
     */
    public SqlType getSqlType() {
        return sqlType;
    }

    /**
     * Sets the value of the sqlType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SqlType }
     *     
     */
    public void setSqlType(SqlType value) {
        this.sqlType = value;
    }

    /**
     * Gets the value of the paramSetter property.
     * 
     * @return
     *     possible object is
     *     {@link ParamSetter }
     *     
     */
    public ParamSetter getParamSetter() {
        return paramSetter;
    }

    /**
     * Sets the value of the paramSetter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParamSetter }
     *     
     */
    public void setParamSetter(ParamSetter value) {
        this.paramSetter = value;
    }

    /**
     * Gets the value of the resultReader property.
     * 
     * @return
     *     possible object is
     *     {@link ResultReader }
     *     
     */
    public ResultReader getResultReader() {
        return resultReader;
    }

    /**
     * Sets the value of the resultReader property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultReader }
     *     
     */
    public void setResultReader(ResultReader value) {
        this.resultReader = value;
    }

}
