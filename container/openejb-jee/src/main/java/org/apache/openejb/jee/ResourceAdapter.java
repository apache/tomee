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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * connector_1_6.xsd
 *
 * <p>Java class for resourceadapterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resourceadapterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="resourceadapter-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="config-property" type="{http://java.sun.com/xml/ns/javaee}config-propertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="outbound-resourceadapter" type="{http://java.sun.com/xml/ns/javaee}outbound-resourceadapterType" minOccurs="0"/>
 *         &lt;element name="inbound-resourceadapter" type="{http://java.sun.com/xml/ns/javaee}inbound-resourceadapterType" minOccurs="0"/>
 *         &lt;element name="adminobject" type="{http://java.sun.com/xml/ns/javaee}adminobjectType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-permission" type="{http://java.sun.com/xml/ns/javaee}security-permissionType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "resourceadapterType", propOrder = {
    "resourceAdapterClass",
    "configProperty",
    "outboundResourceAdapter",
    "inboundResourceAdapter",
    "adminObject",
    "securityPermission"
})
public class ResourceAdapter {

    @XmlElement(name = "resourceadapter-class")
    protected String resourceAdapterClass;
    @XmlElement(name = "config-property")
    protected List<ConfigProperty> configProperty;
    @XmlElement(name = "outbound-resourceadapter")
    protected OutboundResourceAdapter outboundResourceAdapter;
    @XmlElement(name = "inbound-resourceadapter")
    protected InboundResourceadapter inboundResourceAdapter;
    @XmlElement(name = "adminobject")
    protected List<AdminObject> adminObject;
    @XmlElement(name = "security-permission")
    protected List<SecurityPermission> securityPermission;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public ResourceAdapter() {
    }

    public ResourceAdapter(String resourceAdapterClass) {
        this.resourceAdapterClass = resourceAdapterClass;
    }

    public ResourceAdapter(Class resourceAdapterClass) {
        this(resourceAdapterClass.getName());
    }

    public static ResourceAdapter newResourceAdapter(ResourceAdapter10 source) {
        ResourceAdapter resourceAdapter = new ResourceAdapter();
        resourceAdapter.getSecurityPermission().addAll(source.getSecurityPermission());
        resourceAdapter.setId(source.getId());
        OutboundResourceAdapter outboundResourceAdapter = new OutboundResourceAdapter();
        outboundResourceAdapter.getAuthenticationMechanism().addAll(source.getAuthenticationMechanism());
        outboundResourceAdapter.setTransactionSupport(source.getTransactionSupport());
        outboundResourceAdapter.setReauthenticationSupport(source.isReauthenticationSupport());
        outboundResourceAdapter.getConnectionDefinition().add(source.getConnectionDefinition());
        resourceAdapter.setOutboundResourceAdapter(outboundResourceAdapter);
        return resourceAdapter;
    }

    public String getResourceAdapterClass() {
        return resourceAdapterClass;
    }

    public void setResourceAdapterClass(String value) {
        this.resourceAdapterClass = value;
    }

    public List<ConfigProperty> getConfigProperty() {
        if (configProperty == null) {
            configProperty = new ArrayList<ConfigProperty>();
        }
        return this.configProperty;
    }

    public OutboundResourceAdapter getOutboundResourceAdapter() {
        return outboundResourceAdapter;
    }

    public OutboundResourceAdapter setOutboundResourceAdapter(OutboundResourceAdapter value) {
        this.outboundResourceAdapter = value;
        return outboundResourceAdapter;
    }

    public InboundResourceadapter getInboundResourceAdapter() {
        return inboundResourceAdapter;
    }

    public InboundResourceadapter setInboundResourceAdapter(InboundResourceadapter value) {
        this.inboundResourceAdapter = value;
        return inboundResourceAdapter;
    }

    public List<AdminObject> getAdminObject() {
        if (adminObject == null) {
            adminObject = new ArrayList<AdminObject>();
        }
        return this.adminObject;
    }

    public List<SecurityPermission> getSecurityPermission() {
        if (securityPermission == null) {
            securityPermission = new ArrayList<SecurityPermission>();
        }
        return this.securityPermission;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
