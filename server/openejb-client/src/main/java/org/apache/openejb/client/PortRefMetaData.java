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
package org.apache.openejb.client;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

public class PortRefMetaData implements Serializable {
    private static final long serialVersionUID = 4343767807431809218L;

    private QName qname;
    private String serviceEndpointInterface;
    private boolean enableMtom;
    private final Properties properties = new Properties();
    private final List<String> addresses = new ArrayList<String>(1);

    public QName getQName() {
        return qname;
    }

    public void setQName(QName qname) {
        this.qname = qname;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public boolean isEnableMtom() {
        return enableMtom;
    }

    public void setEnableMtom(boolean value) {
        this.enableMtom = value;
    }

    public Properties getProperties() {
        return properties;
    }

    public List<String> getAddresses() {
        return addresses;
    }
}
