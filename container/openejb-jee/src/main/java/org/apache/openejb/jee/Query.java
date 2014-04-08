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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 * 
 * <p>Java class for queryType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="queryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" minOccurs="0"/>
 *         &lt;element name="query-method" type="{http://java.sun.com/xml/ns/javaee}query-methodType"/>
 *         &lt;element name="result-type-mapping" type="{http://java.sun.com/xml/ns/javaee}result-type-mappingType" minOccurs="0"/>
 *         &lt;element name="ejb-ql" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
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

    public void setDescription(Text value) {
        this.description = value;
    }

    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    public void setQueryMethod(QueryMethod value) {
        this.queryMethod = value;
    }

    public ResultTypeMapping getResultTypeMapping() {
        return resultTypeMapping;
    }

    public void setResultTypeMapping(ResultTypeMapping value) {
        this.resultTypeMapping = value;
    }

    public String getEjbQl() {
        return ejbQl;
    }

    public void setEjbQl(String value) {
        this.ejbQl = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
