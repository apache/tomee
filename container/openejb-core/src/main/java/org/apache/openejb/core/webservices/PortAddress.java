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
package org.apache.openejb.core.webservices;

import javax.xml.namespace.QName;

public class PortAddress {
    private final String id;
    private final QName qname;
    private final String address;
    private final String serviceEndpointInterface;

    public PortAddress(String id, QName qname, String address, String serviceEndpointInterface) {
        this.id = id;
        this.qname = qname;
        this.address = address;
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public String getId() {
        return id;
    }

    public QName getQName() {
        return qname;
    }

    public String getAddress() {
        return address;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }
}
