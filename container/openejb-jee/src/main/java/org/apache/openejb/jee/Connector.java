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

import javax.xml.bind.annotation.*;

/**
 * connector_1_6.xsd
 *
 * <p>Java class for connectorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="connectorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="module-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="vendor-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="eis-type" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="resourceadapter-version" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="license" type="{http://java.sun.com/xml/ns/javaee}licenseType" minOccurs="0"/>
 *         &lt;element name="resourceadapter" type="{http://java.sun.com/xml/ns/javaee}resourceadapterType"/>
 *         &lt;element name="required-work-context" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://java.sun.com/xml/ns/javaee}dewey-versionType" fixed="1.6" />
 *       &lt;attribute name="metadata-complete" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.NONE)
//@XmlType(name = "connectorType", propOrder = {
//        "moduleName",
//        "descriptions",
//        "displayNames",
//        "icon",
//        "vendorName",
//        "eisType",
//        "resourceAdapterVersion",
//        "license",
//        "resourceAdapter",
//        "requiredWorkContext"
//})
public class Connector extends ConnectorBase {

    public Connector() {
    }

    public Connector(String id) {
        super(id);
    }

    @XmlElement(name = "resourceadapter-version")
    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    public void setResourceAdapterVersion(String value) {
        this.resourceAdapterVersion = value;
    }

    @XmlElement(name = "resourceadapter", required = true)
    public ResourceadapterX getResourceAdapter() {
        if (resourceAdapter == null) {
            resourceAdapter = new ResourceadapterX();
        }
        return (ResourceadapterX) resourceAdapter;
    }

    public ResourceadapterX setResourceAdapter(ResourceadapterX resourceAdapter16) {
        this.resourceAdapter = resourceAdapter16;
        return (ResourceadapterX) this.resourceAdapter;
    }

    @XmlAttribute(required = true)
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
    
}