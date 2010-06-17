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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base class for Connector (1.6) and Connector10 (1.0) jaxb clases
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ConnectorBase {

    @XmlElement(name = "module-name")
    protected String moduleName;
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(name = "vendor-name")
    protected String vendorName = "";
    @XmlElement(name = "eis-type")
    protected String eisType = "";
    @XmlTransient
    protected String resourceAdapterVersion = "";
    protected License license;
    @XmlTransient
    protected ResourceAdapterBase resourceAdapter;
    @XmlElement(name = "required-work-context")
    protected List<String> requiredWorkContext;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlTransient
    protected String version;
    @XmlAttribute(name = "metadata-complete")
    protected Boolean metadataComplete;

    public ConnectorBase() {
    }

    public ConnectorBase(String id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
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

    public ResourceAdapterBase getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapterBase resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public List<String> getRequiredWorkContext() {
        if (requiredWorkContext == null) {
            requiredWorkContext = new ArrayList<String>();
        }
        return requiredWorkContext;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getVersion() {
        if (version == null) {
            return "1.6";
        } else {
            return version;
        }
    }

    public void setVersion(String value) {
        this.version = value;
    }

    public Boolean isMetadataComplete() {
        return metadataComplete;
    }

    public void setMetadataComplete(Boolean metadataComplete) {
        this.metadataComplete = metadataComplete;
    }
}
