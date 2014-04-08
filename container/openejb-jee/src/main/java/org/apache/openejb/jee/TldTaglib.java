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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * web-jsptaglibrary_2_1.xsd
 *
 * <p>Java class for tldTaglibType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tldTaglibType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="tlib-version" type="{http://java.sun.com/xml/ns/javaee}dewey-versionType"/>
 *         &lt;element name="short-name" type="{http://java.sun.com/xml/ns/javaee}tld-canonical-nameType"/>
 *         &lt;element name="uri" type="{http://java.sun.com/xml/ns/javaee}xsdAnyURIType" minOccurs="0"/>
 *         &lt;element name="validator" type="{http://java.sun.com/xml/ns/javaee}validatorType" minOccurs="0"/>
 *         &lt;element name="listener" type="{http://java.sun.com/xml/ns/javaee}listenerType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="tag" type="{http://java.sun.com/xml/ns/javaee}tagType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="tag-file" type="{http://java.sun.com/xml/ns/javaee}tagFileType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="function" type="{http://java.sun.com/xml/ns/javaee}functionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="taglib-extension" type="{http://java.sun.com/xml/ns/javaee}tld-extensionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://java.sun.com/xml/ns/javaee}dewey-versionType" fixed="2.1" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlRootElement(name = "taglib")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tldTaglibType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",      
    "tlibVersion",
    "jspVersion",
    "shortName",
    "uri",
    "validator",
    "listener",
    "tag",
    "tagFile",
    "function",
    "taglibExtension"
})
public class TldTaglib {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(name = "tlib-version", required = true)
    protected String tlibVersion;
    @XmlElement(name = "jsp-version")
    protected String jspVersion;
    @XmlElement(name = "short-name", required = true)
    protected String shortName;
    protected String uri;
    protected Validator validator;
    protected List<Listener> listener;
    protected List<Tag> tag;
    @XmlElement(name = "tag-file")
    protected List<TagFile> tagFile;
    protected List<Function> function;
    @XmlElement(name = "taglib-extension")
    protected List<TldExtension> taglibExtension;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(required = true)
    protected String version;

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

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getTlibVersion() {
        return tlibVersion;
    }

    public void setTlibVersion(String value) {
        this.tlibVersion = value;
    }

    public String getJspVersion() {
        return jspVersion;
    }

    public void setJspVersion(String jspVersion) {
        this.jspVersion = jspVersion;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String value) {
        this.shortName = value;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String value) {
        this.uri = value;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator value) {
        this.validator = value;
    }

    public List<Listener> getListener() {
        if (listener == null) {
            listener = new ArrayList<Listener>();
        }
        return this.listener;
    }

    public List<Tag> getTag() {
        if (tag == null) {
            tag = new ArrayList<Tag>();
        }
        return this.tag;
    }

    public List<TagFile> getTagFile() {
        if (tagFile == null) {
            tagFile = new ArrayList<TagFile>();
        }
        return this.tagFile;
    }

    public List<Function> getFunction() {
        if (function == null) {
            function = new ArrayList<Function>();
        }
        return this.function;
    }

    public List<TldExtension> getTaglibExtension() {
        if (taglibExtension == null) {
            taglibExtension = new ArrayList<TldExtension>();
        }
        return this.taglibExtension;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getVersion() {
        if (version == null) {
            return "2.1";
        } else {
            return version;
        }
    }

    public void setVersion(String value) {
        this.version = value;
    }
}
