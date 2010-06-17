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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
 * &lt;complexType name="jsp-property-groupType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="url-pattern" type="{http://java.sun.com/xml/ns/javaee}url-patternType" maxOccurs="unbounded"/>
 *         &lt;element name="el-ignored" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="page-encoding" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;element name="scripting-invalid" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="is-xml" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="include-prelude" type="{http://java.sun.com/xml/ns/javaee}pathType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="include-coda" type="{http://java.sun.com/xml/ns/javaee}pathType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="deferred-syntax-allowed-as-literal" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="trim-directive-whitespaces" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="default-content-type" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;element name="buffer" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;element name="error-on-undeclared-namespace" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
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

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(Text[] text) {
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

    public Map<String,Icon> getIconMap() {
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

    public void setElIgnored(Boolean value) {
        this.elIgnored = value;
    }

    public String getPageEncoding() {
        return pageEncoding;
    }

    public void setPageEncoding(String value) {
        this.pageEncoding = value;
    }

    public Boolean isScriptingInvalid() {
        return scriptingInvalid;
    }

    public void setScriptingInvalid(Boolean value) {
        this.scriptingInvalid = value;
    }

    public Boolean isIsXml() {
        return isXml;
    }

    public void setIsXml(Boolean value) {
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

    public void setDeferredSyntaxAllowedAsLiteral(Boolean value) {
        this.deferredSyntaxAllowedAsLiteral = value;
    }

    public Boolean isTrimDirectiveWhitespaces() {
        return trimDirectiveWhitespaces;
    }

    public void setTrimDirectiveWhitespaces(Boolean value) {
        this.trimDirectiveWhitespaces = value;
    }

    public String getDefaultContentType() {
        return defaultContentType;
    }

    public void setDefaultContentType(String value) {
        this.defaultContentType = value;
    }

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String value) {
        this.buffer = value;
    }

    public Boolean getErrorOnUndeclaredNamespace() {
        return errorOnUndeclaredNamespace;
    }

    public void setErrorOnUndeclaredNamespace(Boolean value) {
        this.errorOnUndeclaredNamespace = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
