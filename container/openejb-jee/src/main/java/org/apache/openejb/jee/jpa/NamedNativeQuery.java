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

package org.apache.openejb.jee.jpa;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Target({TYPE}) @Retention(RUNTIME)
 * public @interface NamedNativeQuery {
 * String name();
 * String query();
 * QueryHint[] hints() default {};
 * Class resultClass() default void.class;
 * String resultSetMapping() default ""; //named SqlResultSetMapping
 * }
 *
 *
 *
 * <p>Java class for named-native-query complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="named-native-query"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="query" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="hint" type="{http://java.sun.com/xml/ns/persistence/orm}query-hint" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="result-class" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="result-set-mapping" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "named-native-query", propOrder = {
    "description",
    "query",
    "hint"
})
public class NamedNativeQuery {

    protected String description;
    @XmlElement(required = true)
    protected String query;
    protected List<QueryHint> hint;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "result-class")
    protected String resultClass;
    @XmlAttribute(name = "result-set-mapping")
    protected String resultSetMapping;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(final String value) {
        this.description = value;
    }

    /**
     * Gets the value of the query property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQuery(final String value) {
        this.query = value;
    }

    /**
     * Gets the value of the hint property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hint property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHint().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link QueryHint }
     */
    public List<QueryHint> getHint() {
        if (hint == null) {
            hint = new ArrayList<QueryHint>();
        }
        return this.hint;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the resultClass property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getResultClass() {
        return resultClass;
    }

    /**
     * Sets the value of the resultClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResultClass(final String value) {
        this.resultClass = value;
    }

    /**
     * Gets the value of the resultSetMapping property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getResultSetMapping() {
        return resultSetMapping;
    }

    /**
     * Sets the value of the resultSetMapping property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResultSetMapping(final String value) {
        this.resultSetMapping = value;
    }

}
