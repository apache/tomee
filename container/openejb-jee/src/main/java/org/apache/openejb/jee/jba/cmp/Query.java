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
 *         &lt;element ref="{http://jboss.org}description" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}query-method"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://jboss.org}jboss-ql"/>
 *           &lt;element ref="{http://jboss.org}dynamic-ql"/>
 *           &lt;element ref="{http://jboss.org}declared-sql"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://jboss.org}read-ahead" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}ql-compiler" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}lazy-resultset-loading" minOccurs="0"/>
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
    "description",
    "queryMethod",
    "jbossQl",
    "dynamicQl",
    "declaredSql",
    "readAhead",
    "qlCompiler",
    "lazyResultsetLoading"
})
@XmlRootElement(name = "query")
public class Query {

    protected Description description;
    @XmlElement(name = "query-method", required = true)
    protected QueryMethod queryMethod;
    @XmlElement(name = "jboss-ql")
    protected JbossQl jbossQl;
    @XmlElement(name = "dynamic-ql")
    protected DynamicQl dynamicQl;
    @XmlElement(name = "declared-sql")
    protected DeclaredSql declaredSql;
    @XmlElement(name = "read-ahead")
    protected ReadAhead readAhead;
    @XmlElement(name = "ql-compiler")
    protected QlCompiler qlCompiler;
    @XmlElement(name = "lazy-resultset-loading")
    protected LazyResultsetLoading lazyResultsetLoading;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the queryMethod property.
     * 
     * @return
     *     possible object is
     *     {@link QueryMethod }
     *     
     */
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    /**
     * Sets the value of the queryMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryMethod }
     *     
     */
    public void setQueryMethod(QueryMethod value) {
        this.queryMethod = value;
    }

    /**
     * Gets the value of the jbossQl property.
     * 
     * @return
     *     possible object is
     *     {@link JbossQl }
     *     
     */
    public JbossQl getJbossQl() {
        return jbossQl;
    }

    /**
     * Sets the value of the jbossQl property.
     * 
     * @param value
     *     allowed object is
     *     {@link JbossQl }
     *     
     */
    public void setJbossQl(JbossQl value) {
        this.jbossQl = value;
    }

    /**
     * Gets the value of the dynamicQl property.
     * 
     * @return
     *     possible object is
     *     {@link DynamicQl }
     *     
     */
    public DynamicQl getDynamicQl() {
        return dynamicQl;
    }

    /**
     * Sets the value of the dynamicQl property.
     * 
     * @param value
     *     allowed object is
     *     {@link DynamicQl }
     *     
     */
    public void setDynamicQl(DynamicQl value) {
        this.dynamicQl = value;
    }

    /**
     * Gets the value of the declaredSql property.
     * 
     * @return
     *     possible object is
     *     {@link DeclaredSql }
     *     
     */
    public DeclaredSql getDeclaredSql() {
        return declaredSql;
    }

    /**
     * Sets the value of the declaredSql property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeclaredSql }
     *     
     */
    public void setDeclaredSql(DeclaredSql value) {
        this.declaredSql = value;
    }

    /**
     * Gets the value of the readAhead property.
     * 
     * @return
     *     possible object is
     *     {@link ReadAhead }
     *     
     */
    public ReadAhead getReadAhead() {
        return readAhead;
    }

    /**
     * Sets the value of the readAhead property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReadAhead }
     *     
     */
    public void setReadAhead(ReadAhead value) {
        this.readAhead = value;
    }

    /**
     * Gets the value of the qlCompiler property.
     * 
     * @return
     *     possible object is
     *     {@link QlCompiler }
     *     
     */
    public QlCompiler getQlCompiler() {
        return qlCompiler;
    }

    /**
     * Sets the value of the qlCompiler property.
     * 
     * @param value
     *     allowed object is
     *     {@link QlCompiler }
     *     
     */
    public void setQlCompiler(QlCompiler value) {
        this.qlCompiler = value;
    }

    /**
     * Gets the value of the lazyResultsetLoading property.
     * 
     * @return
     *     possible object is
     *     {@link LazyResultsetLoading }
     *     
     */
    public LazyResultsetLoading getLazyResultsetLoading() {
        return lazyResultsetLoading;
    }

    /**
     * Sets the value of the lazyResultsetLoading property.
     * 
     * @param value
     *     allowed object is
     *     {@link LazyResultsetLoading }
     *     
     */
    public void setLazyResultsetLoading(LazyResultsetLoading value) {
        this.lazyResultsetLoading = value;
    }

}
