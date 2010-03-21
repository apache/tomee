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
 * The resourceadapterType specifies information about the
 * resource adapter. The information includes fully qualified
 * resource adapter Java class name, configuration properties,
 * information specific to the implementation of the resource
 * adapter library as specified through the
 * outbound-resourceadapter and inbound-resourceadapter
 * elements, and an optional set of administered objects.
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlSeeAlso(ResourceAdapter.class)
public class ResourceAdapterBase {

    @XmlElement(name = "resourceadapter-class")
    protected String resourceAdapterClass;
    @XmlTransient
    protected List<ConfigProperty> configProperty;
    @XmlElement(name = "outbound-resourceadapter")
    protected OutboundResourceAdapter outboundResourceAdapter;
    @XmlElement(name = "inbound-resourceadapter")
    protected InboundResource inboundResourceAdapter;
    @XmlElement(name = "adminobject")
    protected List<AdminObject> adminObject;
    @XmlElement(name = "security-permission")
    protected List<SecurityPermission> securityPermission;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public ResourceAdapterBase() {
    }

    public ResourceAdapterBase(String resourceAdapterClass) {
        this.resourceAdapterClass = resourceAdapterClass;
    }

    public ResourceAdapterBase(Class resourceAdapterClass) {
        this(resourceAdapterClass.getName());
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

    public InboundResource getInboundResourceAdapter() {
        return inboundResourceAdapter;
    }

    public InboundResource setInboundResourceAdapter(InboundResource value) {
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
