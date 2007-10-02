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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.Map;

/**
 * The connectorType defines a resource adapter.
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connectorType", propOrder = {
        "descriptions",
        "displayNames",
        "icon",
        "vendorName",
        "eisType",
        "resourceAdapterVersion",
        "license",
        "resourceAdapter"
})
public class Connector {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(name = "vendor-name", required = true)
    protected String vendorName;
    @XmlElement(name = "eis-type", required = true)
    protected String eisType;
    @XmlElement(name = "resourceadapter-version", required = true)
    protected String resourceAdapterVersion;
    protected License license;
    @XmlElement(name = "resourceadapter", required = true)
    protected ResourceAdapter resourceAdapter;
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

    public Map<String,Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String value) {
        this.vendorName = value;
    }

    public String getEisType() {
        return eisType;
    }

    public void setEisType(String value) {
        this.eisType = value;
    }

    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    public void setResourceAdapterVersion(String value) {
        this.resourceAdapterVersion = value;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License value) {
        this.license = value;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter value) {
        this.resourceAdapter = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getVersion() {
        if (version == null) {
            return "1.5";
        } else {
            return version;
        }
    }

    public void setVersion(String value) {
        this.version = value;
    }

}
