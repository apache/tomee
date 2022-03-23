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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * connector_1_6.xsd
 *
 * <p>Java class for connectorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="connectorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="module-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="vendor-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/&gt;
 *         &lt;element name="eis-type" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/&gt;
 *         &lt;element name="resourceadapter-version" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/&gt;
 *         &lt;element name="license" type="{http://java.sun.com/xml/ns/javaee}licenseType" minOccurs="0"/&gt;
 *         &lt;element name="resourceadapter" type="{http://java.sun.com/xml/ns/javaee}resourceadapterType"/&gt;
 *         &lt;element name="required-work-context" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" use="required" type="{http://java.sun.com/xml/ns/javaee}dewey-versionType" fixed="1.6" /&gt;
 *       &lt;attribute name="metadata-complete" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connectorType", propOrder = {
    "moduleName",
    "descriptions",
    "displayNames",
    "icon",
    "vendorName",
    "eisType",
    "resourceAdapterVersion",
    "license",
    "resourceAdapter",
    "requiredWorkContext"
})
public class Connector implements NamedModule {

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
    @XmlElement(name = "resourceadapter-version")
    protected String resourceAdapterVersion = "";
    protected License license;
    @XmlElement(name = "resourceadapter", required = true)
    protected ResourceAdapter resourceAdapter;
    @XmlElement(name = "required-work-context")
    protected List<String> requiredWorkContext;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(required = true)
    protected String version;
    @XmlAttribute(name = "metadata-complete")
    protected Boolean metadataComplete;

    public Connector() {
    }

    public Connector(final String id) {
        this.id = id;
    }

    public static Connector newConnector(final Connector10 source) {
        final Connector connector = new Connector();
        connector.setDescriptions(source.getDescriptions());
        connector.setDisplayNames(source.getDisplayNames());
        connector.getIcons().addAll(source.getIcons());
        connector.setVendorName(source.getVendorName());
        connector.setEisType(source.getEisType());
        connector.setResourceAdapterVersion(source.getVersion());
        connector.setLicense(source.getLicense());
        connector.setId(source.getId());
        connector.setVersion(source.getSpecVersion());
        connector.setResourceAdapter(ResourceAdapter.newResourceAdapter(source.getResourceAdapter()));
        return connector;
    }


    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

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

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(final String value) {
        this.vendorName = value;
    }

    public String getEisType() {
        return eisType;
    }

    public void setEisType(final String value) {
        this.eisType = value;
    }

    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    public void setResourceAdapterVersion(final String value) {
        this.resourceAdapterVersion = value;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(final License value) {
        this.license = value;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(final ResourceAdapter resourceAdapter) {
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

    public void setId(final String value) {
        this.id = value;
    }

    public String getVersion() {
        if (version == null) {
            return "1.6";
        } else {
            return version;
        }
    }

    public void setVersion(final String value) {
        this.version = value;
    }

    public Boolean isMetadataComplete() {
        return metadataComplete;
    }

    public void setMetadataComplete(final Boolean metadataComplete) {
        this.metadataComplete = metadataComplete;
    }
}
