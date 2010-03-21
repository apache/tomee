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
 * The connectorType defines a resource adapter.
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.PROPERTY)
//@XmlType(name = "connectorType", propOrder = {
//        "displayNames",
//        "descriptions",
//        "icon",
//        "vendorName",
//        "version",
//        "eisType",
//        "version",
//        "license",
//        "resourceAdapter"
//})
public class Connector10 extends ConnectorBase {

    public Connector10() {
    }

    @XmlElement(name = "version")
    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    public void setResourceAdapterVersion(String value) {
        this.resourceAdapterVersion = value;
    }

    @XmlElement(name = "resourceadapter", required = true)
    public ResourceAdapter10 getResourceAdapter() {
        if (resourceAdapter == null){
            resourceAdapter = new ResourceAdapter10();
        }
        return (ResourceAdapter10) resourceAdapter;
    }

    public ResourceAdapter10 setResourceAdapter(ResourceAdapter10 value) {
        this.resourceAdapter = value;
        return (ResourceAdapter10) resourceAdapter;
    }

    @XmlElement(name="spec-version")
    public String getVersion() {
        if (version == null) {
            return "1.0";
        } else {
            return version;
        }
    }

    public void setVersion(String value) {
        this.version = value;
    }

}