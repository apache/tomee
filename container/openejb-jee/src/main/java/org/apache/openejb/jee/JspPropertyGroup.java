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
 * The jsp-property-groupType is used to group a number of
 * files so they can be given global property information.
 * All files so described are deemed to be JSP files.  The
 * following additional properties can be described:
 * <p/>
 * - Control whether EL is ignored.
 * - Control whether scripting elements are invalid.
 * - Indicate pageEncoding information.
 * - Indicate that a resource is a JSP document (XML).
 * - Prelude and Coda automatic includes.
 * - Control whether the character sequence #{ is allowed
 * when used as a String literal.
 * - Control whether template text containing only
 * whitespaces must be removed from the response output.
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
        "trimDirectiveWhitespaces"
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

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
