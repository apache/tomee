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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * web-common_3_0.xsd
 * 
 * <p>Java class for web-resource-collectionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="web-resource-collectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="web-resource-name" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="url-pattern" type="{http://java.sun.com/xml/ns/javaee}url-patternType" maxOccurs="unbounded"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="http-method" type="{http://java.sun.com/xml/ns/javaee}http-methodType" maxOccurs="unbounded"/>
 *           &lt;element name="http-method-omission" type="{http://java.sun.com/xml/ns/javaee}http-methodType" maxOccurs="unbounded"/>
 *         &lt;/choice>
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
@XmlType(name = "web-resource-collectionType", propOrder = {
        "webResourceName",
        "descriptions",
        "urlPattern",
        "httpMethod",
        "httpMethodOmission"
})
public class WebResourceCollection {

    @XmlElement(name = "web-resource-name", required = true)
    protected String webResourceName;
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "url-pattern", required = true)
    protected List<String> urlPattern;
    @XmlElement(name = "http-method")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected List<String> httpMethod;
    @XmlElement(name = "http-method-omission")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected List<String> httpMethodOmission;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public String getWebResourceName() {
        return webResourceName;
    }

    public void setWebResourceName(String value) {
        this.webResourceName = value;
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public List<String> getUrlPattern() {
        if (urlPattern == null) {
            urlPattern = new ArrayList<String>();
        }
        return this.urlPattern;
    }

    public List<String> getHttpMethod() {
        if (httpMethod == null) {
            httpMethod = new ArrayList<String>();
        }
        return this.httpMethod;
    }

    public List<String> getHttpMethodOmission() {
        if (httpMethodOmission == null) {
            httpMethodOmission = new ArrayList<String>();
        }
        return this.httpMethodOmission;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}

