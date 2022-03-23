/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.jee;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for queryType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="queryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" minOccurs="0"/&gt;
 *         &lt;element name="query-method" type="{http://java.sun.com/xml/ns/javaee}query-methodType"/&gt;
 *         &lt;element name="result-type-mapping" type="{http://java.sun.com/xml/ns/javaee}result-type-mappingType" minOccurs="0"/&gt;
 *         &lt;element name="ejb-ql" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryType", propOrder = {
    "description",
    "queryMethod",
    "resultTypeMapping",
    "ejbQl"
})
public class Query {

    protected Text description;
    @XmlElement(name = "query-method", required = true)
    protected QueryMethod queryMethod;
    @XmlElement(name = "result-type-mapping")
    protected ResultTypeMapping resultTypeMapping;
    @XmlElement(name = "ejb-ql", required = true)
    protected String ejbQl;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public Text getDescription() {
        return description;
    }

    public void setDescription(final Text value) {
        this.description = value;
    }

    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    public void setQueryMethod(final QueryMethod value) {
        this.queryMethod = value;
    }

    public ResultTypeMapping getResultTypeMapping() {
        return resultTypeMapping;
    }

    public void setResultTypeMapping(final ResultTypeMapping value) {
        this.resultTypeMapping = value;
    }

    public String getEjbQl() {
        return ejbQl;
    }

    public void setEjbQl(final String value) {
        this.ejbQl = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
