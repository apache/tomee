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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * web-common_3_0.xsd
 *
 * <p>Java class for cookie-configType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="cookie-configType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}cookie-nameType" minOccurs="0"/&gt;
 *         &lt;element name="domain" type="{http://java.sun.com/xml/ns/javaee}cookie-domainType" minOccurs="0"/&gt;
 *         &lt;element name="path" type="{http://java.sun.com/xml/ns/javaee}cookie-pathType" minOccurs="0"/&gt;
 *         &lt;element name="comment" type="{http://java.sun.com/xml/ns/javaee}cookie-commentType" minOccurs="0"/&gt;
 *         &lt;element name="http-only" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="secure" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="max-age" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cookie-configType", propOrder = {
    "name",
    "domain",
    "path",
    "comment",
    "httpOnly",
    "secure",
    "maxAge"
})
public class CookieConfig {

    protected String name;
    protected String domain;
    protected String path;
    protected String comment;
    @XmlElement(name = "http-only")
    protected Boolean httpOnly;
    protected Boolean secure;
    @XmlElement(name = "max-age")
    protected Integer maxAge;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String value) {
        this.domain = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String value) {
        this.path = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String value) {
        this.comment = value;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(final Boolean value) {
        this.httpOnly = value;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(final Boolean value) {
        this.secure = value;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(final Integer value) {
        this.maxAge = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(final java.lang.String value) {
        this.id = value;
    }

}
