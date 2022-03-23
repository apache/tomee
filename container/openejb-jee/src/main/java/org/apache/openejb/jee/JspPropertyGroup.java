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
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;

/**
 * jsp_2_2.xsd
 *
 * <p>Java class for jsp-property-groupType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="jsp-property-groupType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="url-pattern" type="{http://java.sun.com/xml/ns/javaee}url-patternType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="el-ignored" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="page-encoding" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="scripting-invalid" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="is-xml" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="include-prelude" type="{http://java.sun.com/xml/ns/javaee}pathType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="include-coda" type="{http://java.sun.com/xml/ns/javaee}pathType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="deferred-syntax-allowed-as-literal" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="trim-directive-whitespaces" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="default-content-type" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="buffer" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="error-on-undeclared-namespace" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jsp-property-groupType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "urlPattern",
    "elIgnored",
    "pageEncoding",
    "scriptingInvalid",
    "isXml",
    "includePrelude",
    "includeCoda",
    "deferredSyntaxAllowedAsLiteral",
    "trimDirectiveWhitespaces",
    "defaultContentType",
    "buffer",
    "errorOnUndeclaredNamespace"

})
public class JspPropertyGroup {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(name = "url-pattern", required = true)
    @XmlJavaTypeAdapter(TrimStringAdapter.class)
    protected List<String> urlPattern;
    @XmlElement(name = "el-ignored")
    protected Boolean elIgnored;
    @XmlElement(name = "page-encoding")
    protected String pageEncoding;
    @XmlElement(name = "scripting-invalid")
    protected Boolean scriptingInvalid;
    @XmlElement(name = "is-xml")
    protected Boolean isXml;
    @XmlElement(name = "include-prelude")
    protected List<String> includePrelude;
    @XmlElement(name = "include-coda")
    protected List<String> includeCoda;
    @XmlElement(name = "deferred-syntax-allowed-as-literal")
    protected Boolean deferredSyntaxAllowedAsLiteral;
    @XmlElement(name = "trim-directive-whitespaces")
    protected Boolean trimDirectiveWhitespaces;
    @XmlElement(name = "default-content-type")
    protected String defaultContentType;
    protected String buffer;
    @XmlElement(name = "error-on-undeclared-namespace")
    protected Boolean errorOnUndeclaredNamespace;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(final Text[] text) {
        displayName.set(text);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public List<String> getUrlPattern() {
        if (urlPattern == null) {
            urlPattern = new ArrayList<String>();
        }
        return this.urlPattern;
    }

    public Boolean isElIgnored() {
        return elIgnored;
    }

    public void setElIgnored(final Boolean value) {
        this.elIgnored = value;
    }

    public String getPageEncoding() {
        return pageEncoding;
    }

    public void setPageEncoding(final String value) {
        this.pageEncoding = value;
    }

    public Boolean isScriptingInvalid() {
        return scriptingInvalid;
    }

    public void setScriptingInvalid(final Boolean value) {
        this.scriptingInvalid = value;
    }

    public Boolean isIsXml() {
        return isXml;
    }

    public void setIsXml(final Boolean value) {
        this.isXml = value;
    }

    public List<String> getIncludePrelude() {
        if (includePrelude == null) {
            includePrelude = new ArrayList<String>();
        }
        return this.includePrelude;
    }

    public List<String> getIncludeCoda() {
        if (includeCoda == null) {
            includeCoda = new ArrayList<String>();
        }
        return this.includeCoda;
    }

    public Boolean isDeferredSyntaxAllowedAsLiteral() {
        return deferredSyntaxAllowedAsLiteral;
    }

    public void setDeferredSyntaxAllowedAsLiteral(final Boolean value) {
        this.deferredSyntaxAllowedAsLiteral = value;
    }

    public Boolean isTrimDirectiveWhitespaces() {
        return trimDirectiveWhitespaces;
    }

    public void setTrimDirectiveWhitespaces(final Boolean value) {
        this.trimDirectiveWhitespaces = value;
    }

    public String getDefaultContentType() {
        return defaultContentType;
    }

    public void setDefaultContentType(final String value) {
        this.defaultContentType = value;
    }

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(final String value) {
        this.buffer = value;
    }

    public Boolean getErrorOnUndeclaredNamespace() {
        return errorOnUndeclaredNamespace;
    }

    public void setErrorOnUndeclaredNamespace(final Boolean value) {
        this.errorOnUndeclaredNamespace = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
