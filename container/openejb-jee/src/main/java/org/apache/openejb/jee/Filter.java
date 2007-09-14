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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "filterType", propOrder = {
        "descriptions",
        "displayNames",
        "icons",
        "filterName",
        "filterClass",
        "initParam"
})
public class Filter {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlTransient
    protected LocalList<String, Icon> icon = new LocalList<String, Icon>(Icon.class);

    @XmlElement(name = "filter-name", required = true)
    protected String filterName;
    @XmlElement(name = "filter-class", required = true)
    protected String filterClass;
    @XmlElement(name = "init-param")
    protected List<ParamValue> initParam;
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

    @XmlElement(name = "icon", required = true)
    public Icon[] getIcons() {
        return icon.toArray();
    }

    public void setIcons(Icon[] text) {
        icon.set(text);
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String value) {
        this.filterName = value;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String value) {
        this.filterClass = value;
    }

    public List<ParamValue> getInitParam() {
        if (initParam == null) {
            initParam = new ArrayList<ParamValue>();
        }
        return this.initParam;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
