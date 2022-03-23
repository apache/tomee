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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p class="changed_added_2_2">Any view that matches any of the
 * url-patterns in this element may only be reached from another JSF
 * view in the same web application. Because the runtime is aware of
 * which views are protected, any navigation from an unprotected
 * view to a protected view is automatically subject to
 * protection.</p>
 *
 *
 *
 * <p>Java class for faces-config-protected-viewsType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-protected-viewsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="url-pattern" type="{http://xmlns.jcp.org/xml/ns/javaee}url-patternType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-protected-viewsType", propOrder = {
    "urlPattern"
})
public class FacesConfigProtectedViews {

    @XmlElement(name = "url-pattern", required = true)
    protected List<UrlPattern> urlPattern;

    /**
     * Gets the value of the urlPattern property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the urlPattern property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUrlPattern().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link UrlPattern }
     */
    public List<UrlPattern> getUrlPattern() {
        if (urlPattern == null) {
            urlPattern = new ArrayList<UrlPattern>();
        }
        return this.urlPattern;
    }

}
