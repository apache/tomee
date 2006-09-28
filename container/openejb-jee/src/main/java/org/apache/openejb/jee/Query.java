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
 * The queryType defines a finder or select
 * query. It contains
 * - an optional description of the query
 * - the specification of the finder or select
 * method it is used by
 * - an optional specification of the result type
 * mapping, if the query is for a select method
 * and entity objects are returned.
 * - the EJB QL query string that defines the query.
 * <p/>
 * Queries that are expressible in EJB QL must use the ejb-ql
 * element to specify the query. If a query is not expressible
 * in EJB QL, the description element should be used to
 * describe the semantics of the query and the ejb-ql element
 * should be empty.
 * <p/>
 * The result-type-mapping is an optional element. It can only
 * be present if the query-method specifies a select method
 * that returns entity objects.  The default value for the
 * result-type-mapping element is "Local".
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
