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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "methodName",
    "queryParams",
    "queryFilter",
    "queryVariables",
    "queryOrdering"
})
@XmlRootElement(name = "finder")
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

    /**
     * Gets the value of the methodName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the value of the methodName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethodName(String value) {
        this.methodName = value;
    }

    /**
     * Gets the value of the queryParams property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryParams() {
        return queryParams;
    }

    /**
     * Sets the value of the queryParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryParams(String value) {
        this.queryParams = value;
    }

    /**
     * Gets the value of the queryFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryFilter() {
        return queryFilter;
    }

    /**
     * Sets the value of the queryFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryFilter(String value) {
        this.queryFilter = value;
    }

    /**
     * Gets the value of the queryVariables property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryVariables() {
        return queryVariables;
    }

    /**
     * Sets the value of the queryVariables property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryVariables(String value) {
        this.queryVariables = value;
    }

    /**
     * Gets the value of the queryOrdering property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryOrdering() {
        return queryOrdering;
    }

    /**
     * Sets the value of the queryOrdering property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryOrdering(String value) {
        this.queryOrdering = value;
    }

}
