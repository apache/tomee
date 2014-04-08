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
package org.apache.openejb.jee.sun;

import org.apache.openejb.jee.KeyedCollection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "servletName",
    "principalName",
    "webserviceEndpoint"
})
public class Servlet {
    @XmlElement(name = "servlet-name", required = true)
    protected ServletName servletName;
    @XmlElement(name = "principal-name")
    protected PrincipalName principalName;
    @XmlElement(name = "webservice-endpoint")
    protected KeyedCollection<String,WebserviceEndpoint> webserviceEndpoint;

    public ServletName getServletName() {
        return servletName;
    }

    public void setServletName(ServletName value) {
        this.servletName = value;
    }

    public PrincipalName getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(PrincipalName value) {
        this.principalName = value;
    }

    public Collection<WebserviceEndpoint> getWebserviceEndpoint() {
        if (webserviceEndpoint == null) {
            webserviceEndpoint = new KeyedCollection<String,WebserviceEndpoint>();
        }
        return this.webserviceEndpoint;
    }

    public Map<String,WebserviceEndpoint> getWebserviceEndpointMap() {
        if (webserviceEndpoint == null) {
            webserviceEndpoint = new KeyedCollection<String,WebserviceEndpoint>();
        }
        return this.webserviceEndpoint.toMap();
    }
}
