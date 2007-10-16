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
 * The taglib tag is the document root, it defines:
 * <p/>
 * description     a simple string describing the "use" of this
 * taglib, should be user discernable
 * <p/>
 * display-name    the display-name element contains a
 * short name that is intended to be displayed
 * by tools
 * <p/>
 * icon            optional icon that can be used by tools
 * <p/>
 * tlib-version    the version of the tag library implementation
 * <p/>
 * short-name      a simple default short name that could be
 * used by a JSP authoring tool to create
 * names with a mnemonic value; for example,
 * the it may be used as the prefered prefix
 * value in taglib directives
 * <p/>
 * uri             a uri uniquely identifying this taglib
 * <p/>
 * validator       optional TagLibraryValidator information
 * <p/>
 * listener        optional event listener specification
 * <p/>
 * tag             tags in this tag library
 * <p/>
 * tag-file        tag files in this tag library
 * <p/>
 * function        zero or more EL functions defined in this
 * tag library
 * <p/>
 * taglib-extension zero or more extensions that provide extra
 * information about this taglib, for tool
 * consumption
 */
@XmlRootElement(name = "taglib")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tldTaglibType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",      
    "tlibVersion",
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
