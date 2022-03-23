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
package org.apache.openejb.jee.wls;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for weblogic-query complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="weblogic-query"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.bea.com/ns/weblogic/90}description" minOccurs="0"/&gt;
 *         &lt;element name="query-method" type="{http://www.bea.com/ns/weblogic/90}query-method"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="ejb-ql-query" type="{http://www.bea.com/ns/weblogic/90}ejb-ql-query"/&gt;
 *           &lt;element name="sql-query" type="{http://www.bea.com/ns/weblogic/90}sql-query"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="max-elements" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="include-updates" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="sql-select-distinct" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="enable-query-caching" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "weblogic-query", propOrder = {
    "description",
    "queryMethod",
    "ejbQlQuery",
    "sqlQuery",
    "maxElements",
    "includeUpdates",
    "sqlSelectDistinct",
    "enableQueryCaching"
})
public class WeblogicQuery {

    protected Description description;
    @XmlElement(name = "query-method", required = true)
    protected QueryMethod queryMethod;
    @XmlElement(name = "ejb-ql-query")
    protected EjbQlQuery ejbQlQuery;
    @XmlElement(name = "sql-query")
    protected SqlQuery sqlQuery;
    @XmlElement(name = "max-elements")
    protected BigInteger maxElements;
    @XmlElement(name = "include-updates")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean includeUpdates;
    @XmlElement(name = "sql-select-distinct")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean sqlSelectDistinct;
    @XmlElement(name = "enable-query-caching")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean enableQueryCaching;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link Description }
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link Description }
     */
    public void setDescription(final Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the queryMethod property.
     *
     * @return possible object is
     * {@link QueryMethod }
     */
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    /**
     * Sets the value of the queryMethod property.
     *
     * @param value allowed object is
     *              {@link QueryMethod }
     */
    public void setQueryMethod(final QueryMethod value) {
        this.queryMethod = value;
    }

    /**
     * Gets the value of the ejbQlQuery property.
     *
     * @return possible object is
     * {@link EjbQlQuery }
     */
    public EjbQlQuery getEjbQlQuery() {
        return ejbQlQuery;
    }

    /**
     * Sets the value of the ejbQlQuery property.
     *
     * @param value allowed object is
     *              {@link EjbQlQuery }
     */
    public void setEjbQlQuery(final EjbQlQuery value) {
        this.ejbQlQuery = value;
    }

    /**
     * Gets the value of the sqlQuery property.
     *
     * @return possible object is
     * {@link SqlQuery }
     */
    public SqlQuery getSqlQuery() {
        return sqlQuery;
    }

    /**
     * Sets the value of the sqlQuery property.
     *
     * @param value allowed object is
     *              {@link SqlQuery }
     */
    public void setSqlQuery(final SqlQuery value) {
        this.sqlQuery = value;
    }

    /**
     * Gets the value of the maxElements property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getMaxElements() {
        return maxElements;
    }

    /**
     * Sets the value of the maxElements property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setMaxElements(final BigInteger value) {
        this.maxElements = value;
    }

    /**
     * Gets the value of the includeUpdates property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getIncludeUpdates() {
        return includeUpdates;
    }

    /**
     * Sets the value of the includeUpdates property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIncludeUpdates(final Boolean value) {
        this.includeUpdates = value;
    }

    /**
     * Gets the value of the sqlSelectDistinct property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getSqlSelectDistinct() {
        return sqlSelectDistinct;
    }

    /**
     * Sets the value of the sqlSelectDistinct property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSqlSelectDistinct(final Boolean value) {
        this.sqlSelectDistinct = value;
    }

    /**
     * Gets the value of the enableQueryCaching property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getEnableQueryCaching() {
        return enableQueryCaching;
    }

    /**
     * Sets the value of the enableQueryCaching property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setEnableQueryCaching(final Boolean value) {
        this.enableQueryCaching = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
