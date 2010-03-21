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
//@XmlType(name = "resourceadapterType", propOrder = {
//    "resourceAdapterClass",
//    "configProperty",
//    "outboundResourceAdapter",
//    "inboundResourceAdapter",
//    "adminObject",
//    "securityPermission"
//})
public class ResourceAdapter extends ResourceAdapterBase {

    public ResourceAdapter() {
    }

    public ResourceAdapter(Class resourceAdapterClass) {
        super(resourceAdapterClass);
    }

    public ResourceAdapter(String resourceAdapterClass) {
        super(resourceAdapterClass);
    }

    @XmlElement(name = "config-property")
    public List<ConfigProperty> getConfigProperty() {
        return super.getConfigProperty();
    }

}