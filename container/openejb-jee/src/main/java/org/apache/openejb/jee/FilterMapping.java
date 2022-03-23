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
import java.util.ArrayList;
import java.util.List;

/**
 * web-common_3_0.xsd
 *
 * <p>Java class for filter-mappingType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="filter-mappingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filter-name" type="{http://java.sun.com/xml/ns/javaee}filter-nameType"/&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element name="url-pattern" type="{http://java.sun.com/xml/ns/javaee}url-patternType"/&gt;
 *           &lt;element name="servlet-name" type="{http://java.sun.com/xml/ns/javaee}servlet-nameType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="dispatcher" type="{http://java.sun.com/xml/ns/javaee}dispatcherType" maxOccurs="5" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "filter-mappingType", propOrder = {
    "filterName",
    "urlPattern",
    "servletName",
    "dispatcher"
})
public class FilterMapping {

    @XmlElement(name = "filter-name", required = true)
    protected String filterName;
    @XmlElement(name = "url-pattern")
    @XmlJavaTypeAdapter(TrimStringAdapter.class)
    protected List<String> urlPattern;
    @XmlElement(name = "servlet-name")
    protected List<String> servletName;
    protected List<Dispatcher> dispatcher;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(final String value) {
        this.filterName = value;
    }

    public List<String> getUrlPattern() {
        if (urlPattern == null) {
            urlPattern = new ArrayList<String>();
        }
        return this.urlPattern;
    }

    public List<String> getServletName() {
        if (servletName == null) {
            servletName = new ArrayList<String>();
        }
        return this.servletName;
    }

    public List<Dispatcher> getDispatcher() {
        if (dispatcher == null) {
            dispatcher = new ArrayList<Dispatcher>();
        }
        return this.dispatcher;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
