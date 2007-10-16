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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Defines an action in this tag library that is implemented
 * as a .tag file.
 * <p/>
 * The tag-file element has two required subelements:
 * <p/>
 * description       Optional tag-specific information
 * <p/>
 * display-name      A short name that is intended to be
 * displayed by tools
 * <p/>
 * icon              Optional icon element that can be used
 * by tools
 * <p/>
 * name              The unique action name
 * <p/>
 * path              Where to find the .tag file implementing this
 * action, relative to the root of the web
 * application or the root of the JAR file for a
 * tag library packaged in a JAR.  This must
 * begin with /WEB-INF/tags if the .tag file
 * resides in the WAR, or /META-INF/tags if the
 * .tag file resides in a JAR.
 * <p/>
 * example           Optional informal description of an
 * example of a use of this tag
 * <p/>
 * tag-extension     Zero or more extensions that provide extra
 * information about this tag, for tool
 * consumption
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tagFileType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "name",
    "path",
    "example",
    "tagExtension"
})
public class TagFile {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String path;
    protected String example;
    @XmlElement(name = "tag-extension")
    protected List<TldExtension> tagExtension;
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

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        this.path = value;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String value) {
        this.example = value;
    }

    public List<TldExtension> getTagExtension() {
        if (tagExtension == null) {
            tagExtension = new ArrayList<TldExtension>();
        }
        return this.tagExtension;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
