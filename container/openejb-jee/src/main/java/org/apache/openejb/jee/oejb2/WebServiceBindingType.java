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
package org.apache.openejb.jee.oejb2;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "web-service-bindingType", namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", propOrder = {
    "ejbName",
    "webServiceAddress",
    "webServiceVirtualHost",
    "webServiceSecurity"
})
//@XmlRootElement(name = "web-service-binding", namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0")
public class WebServiceBindingType {

    @XmlElement(name="ejb-name")
    protected String ejbName;

    @XmlElement(name = "web-service-address")
    protected String webServiceAddress;

    @XmlElement(name = "web-service-virtual-host")
    protected List<String> webServiceVirtualHost;

    @XmlElement(name = "web-service-security")
    protected WebServiceSecurityType webServiceSecurity;

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public String getWebServiceAddress() {
        return webServiceAddress;
    }

    public void setWebServiceAddress(String webServiceAddress) {
        this.webServiceAddress = webServiceAddress;
    }

    public List<String> getWebServiceVirtualHost() {
        if (webServiceVirtualHost == null){
            webServiceVirtualHost = new ArrayList<String>();
        }
        return webServiceVirtualHost;
    }

    public void setWebServiceVirtualHost(List<String> webServiceVirtualHost) {
        this.webServiceVirtualHost = webServiceVirtualHost;
    }

    public WebServiceSecurityType getWebServiceSecurity() {
        return webServiceSecurity;
    }

    public void setWebServiceSecurity(WebServiceSecurityType webServiceSecurity) {
        this.webServiceSecurity = webServiceSecurity;
    }

    public void setWebServiceSecurity(org.apache.openejb.jee.oejb2.WebServiceSecurityType webServiceSecurity) {
        if (webServiceSecurity == null) return;
        this.webServiceSecurity = new WebServiceSecurityType(webServiceSecurity);
    }

    public boolean containsData() {
        return webServiceAddress != null || getWebServiceVirtualHost().size() > 0 || webServiceSecurity != null;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "web-service-securityType", namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", propOrder = {
        "securityRealmName",
        "realmName",
        "transportGuarantee",
        "authMethod"
    })
    public static class WebServiceSecurityType {

        @XmlElement(name = "security-realm-name", required = true)
        protected String securityRealmName;
        @XmlElement(name = "realm-name")
        protected String realmName;
        @XmlElement(name = "transport-guarantee", required = true)
        protected TransportGuaranteeType transportGuarantee;
        @XmlElement(name = "auth-method", required = true)
        protected AuthMethodType authMethod;

        public WebServiceSecurityType() {
        }

        public WebServiceSecurityType(org.apache.openejb.jee.oejb2.WebServiceSecurityType s) {
            this.securityRealmName = s.getSecurityRealmName();
            this.realmName = s.getRealmName();
            this.transportGuarantee = s.getTransportGuarantee();
            this.authMethod = s.getAuthMethod();
        }

        public String getSecurityRealmName() {
            return securityRealmName;
        }

        public void setSecurityRealmName(String value) {
            this.securityRealmName = value;
        }

        public String getRealmName() {
            return realmName;
        }

        public void setRealmName(String value) {
            this.realmName = value;
        }

        public TransportGuaranteeType getTransportGuarantee() {
            return transportGuarantee;
        }

        public void setTransportGuarantee(TransportGuaranteeType value) {
            this.transportGuarantee = value;
        }

        public AuthMethodType getAuthMethod() {
            return authMethod;
        }

        public void setAuthMethod(AuthMethodType value) {
            this.authMethod = value;
        }
    }
}
