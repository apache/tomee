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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "methodName",
    "queryParams",
    "queryFilter",
    "queryVariables",
    "queryOrdering"
})
public class Finder {
    @XmlElement(name = "method-name", required = true)
    protected String methodName;
    @XmlElement(name = "query-params")
    protected String queryParams;
    @XmlElement(name = "query-filter")
    protected String queryFilter;
    @XmlElement(name = "query-variables")
    protected String queryVariables;
    @XmlElement(name = "query-ordering")
    protected String queryOrdering;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String value) {
        this.methodName = value;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String value) {
        this.queryParams = value;
    }

    public String getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(String value) {
        this.queryFilter = value;
    }

    public String getQueryVariables() {
        return queryVariables;
    }

    public void setQueryVariables(String value) {
        this.queryVariables = value;
    }

    public String getQueryOrdering() {
        return queryOrdering;
    }

    public void setQueryOrdering(String value) {
        this.queryOrdering = value;
    }
}
