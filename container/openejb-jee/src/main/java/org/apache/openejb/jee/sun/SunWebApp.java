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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contextRoot",
    "securityRoleMapping",
    "servlet",
    "idempotentUrlPattern",
    "sessionConfig",
    "ejbRef",
    "resourceRef",
    "resourceEnvRef",
    "serviceRef",
    "messageDestinationRef",
    "cache",
    "classLoader",
    "jspConfig",
    "localeCharsetInfo",
    "parameterEncoding",
    "property",
    "messageDestination",
    "webserviceDescription"
})
@XmlRootElement(name = "sun-web-app")
public class SunWebApp {
    @XmlAttribute(name = "error-url")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String errorUrl;
    @XmlAttribute(name = "httpservlet-security-provider")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String httpservletSecurityProvider;
    @XmlElement(name = "context-root")
    protected String contextRoot;
    @XmlElement(name = "security-role-mapping")
    protected List<SecurityRoleMapping> securityRoleMapping;
    protected List<Servlet> servlet;
    @XmlElement(name = "idempotent-url-pattern")
    protected List<IdempotentUrlPattern> idempotentUrlPattern;
    @XmlElement(name = "session-config")
    protected SessionConfig sessionConfig;
    @XmlElement(name = "ejb-ref")
    protected List<EjbRef> ejbRef;
    @XmlElement(name = "resource-ref")
    protected List<ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref")
    protected List<ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "service-ref")
    protected List<ServiceRef> serviceRef;
    @XmlElement(name = "message-destination-ref")
    protected List<MessageDestinationRef> messageDestinationRef;
    protected Cache cache;
    @XmlElement(name = "class-loader")
    protected ClassLoader classLoader;
    @XmlElement(name = "jsp-config")
    protected JspConfig jspConfig;
    @XmlElement(name = "locale-charset-info")
    protected LocaleCharsetInfo localeCharsetInfo;
    @XmlElement(name = "parameter-encoding")
    protected ParameterEncoding parameterEncoding;
    protected List<Property> property;
    @XmlElement(name = "message-destination")
    protected List<MessageDestination> messageDestination;
    @XmlElement(name = "webservice-description")
    protected List<WebserviceDescription> webserviceDescription;

    public String getErrorUrl() {
        if (errorUrl == null) {
            return "";
        } else {
            return errorUrl;
        }
    }

    public void setErrorUrl(String value) {
        this.errorUrl = value;
    }

    public String getHttpservletSecurityProvider() {
        return httpservletSecurityProvider;
    }

    public void setHttpservletSecurityProvider(String value) {
        this.httpservletSecurityProvider = value;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String value) {
        this.contextRoot = value;
    }

    public List<SecurityRoleMapping> getSecurityRoleMapping() {
        if (securityRoleMapping == null) {
            securityRoleMapping = new ArrayList<SecurityRoleMapping>();
        }
        return this.securityRoleMapping;
    }

    public List<Servlet> getServlet() {
        if (servlet == null) {
            servlet = new ArrayList<Servlet>();
        }
        return this.servlet;
    }

    public List<IdempotentUrlPattern> getIdempotentUrlPattern() {
        if (idempotentUrlPattern == null) {
            idempotentUrlPattern = new ArrayList<IdempotentUrlPattern>();
        }
        return this.idempotentUrlPattern;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    public void setSessionConfig(SessionConfig value) {
        this.sessionConfig = value;
    }

    public List<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRef>();
        }
        return this.ejbRef;
    }

    public List<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRef>();
        }
        return this.resourceRef;
    }

    public List<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public List<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRef>();
        }
        return this.serviceRef;
    }

    public List<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache value) {
        this.cache = value;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader value) {
        this.classLoader = value;
    }

    public JspConfig getJspConfig() {
        return jspConfig;
    }

    public void setJspConfig(JspConfig value) {
        this.jspConfig = value;
    }

    public LocaleCharsetInfo getLocaleCharsetInfo() {
        return localeCharsetInfo;
    }

    public void setLocaleCharsetInfo(LocaleCharsetInfo value) {
        this.localeCharsetInfo = value;
    }

    public ParameterEncoding getParameterEncoding() {
        return parameterEncoding;
    }

    public void setParameterEncoding(ParameterEncoding value) {
        this.parameterEncoding = value;
    }

    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

    public List<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestination>();
        }
        return this.messageDestination;
    }

    public List<WebserviceDescription> getWebserviceDescription() {
        if (webserviceDescription == null) {
            webserviceDescription = new ArrayList<WebserviceDescription>();
        }
        return this.webserviceDescription;
    }
}
