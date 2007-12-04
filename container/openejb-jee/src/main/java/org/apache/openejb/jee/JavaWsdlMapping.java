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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The element describes the Java mapping to a known WSDL document.
 * <p/>
 * It contains the mapping between package names and XML namespaces,
 * WSDL root types and Java artifacts, and the set of mappings for
 * services.
 */
@XmlRootElement(name = "java-wsdl-mapping")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "java-wsdl-mappingType", propOrder = {
    "packageMapping",
    "javaXmlTypeMapping",
    "exceptionMapping",
    "serviceInterfaceMapping",
    "serviceEndpointInterfaceMapping"
})
public class JavaWsdlMapping {
    @XmlElement(name = "package-mapping", required = true)
    protected KeyedCollection<String, PackageMapping> packageMapping;
    @XmlElement(name = "java-xml-type-mapping")
    protected List<JavaXmlTypeMapping> javaXmlTypeMapping;
    @XmlElement(name = "exception-mapping")
    protected KeyedCollection<QName,ExceptionMapping> exceptionMapping;
    @XmlElement(name = "service-interface-mapping")
    protected List<ServiceInterfaceMapping> serviceInterfaceMapping;
    @XmlElement(name = "service-endpoint-interface-mapping")
    protected KeyedCollection<String, ServiceEndpointInterfaceMapping> serviceEndpointInterfaceMapping;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(required = true)
    protected String version;

    public Collection<PackageMapping> getPackageMapping() {
        if (packageMapping == null) {
            packageMapping = new KeyedCollection<String, PackageMapping>();
        }
        return this.packageMapping;
    }

    public Map<String, PackageMapping> getPackageMappingMap() {
        if (packageMapping == null) {
            packageMapping = new KeyedCollection<String, PackageMapping>();
        }
        return this.packageMapping.toMap();
    }

    public List<JavaXmlTypeMapping> getJavaXmlTypeMapping() {
        if (javaXmlTypeMapping == null) {
            javaXmlTypeMapping = new ArrayList<JavaXmlTypeMapping>();
        }
        return this.javaXmlTypeMapping;
    }

    public Collection<ExceptionMapping> getExceptionMapping() {
        if (exceptionMapping == null) {
            exceptionMapping = new KeyedCollection<QName,ExceptionMapping>();
        }
        return this.exceptionMapping;
    }

    public Map<QName,ExceptionMapping> getExceptionMappingMap() {
        if (exceptionMapping == null) {
            exceptionMapping = new KeyedCollection<QName,ExceptionMapping>();
        }
        return this.exceptionMapping.toMap();
    }

    public List<ServiceInterfaceMapping> getServiceInterfaceMapping() {
        if (serviceInterfaceMapping == null) {
            serviceInterfaceMapping = new ArrayList<ServiceInterfaceMapping>();
        }
        return this.serviceInterfaceMapping;
    }

    public Collection<ServiceEndpointInterfaceMapping> getServiceEndpointInterfaceMapping() {
        if (serviceEndpointInterfaceMapping == null) {
            serviceEndpointInterfaceMapping = new KeyedCollection<String, ServiceEndpointInterfaceMapping>();
        }
        return this.serviceEndpointInterfaceMapping;
    }

    public Map<String, ServiceEndpointInterfaceMapping> getServiceEndpointInterfaceMappingMap() {
        if (serviceEndpointInterfaceMapping == null) {
            serviceEndpointInterfaceMapping = new KeyedCollection<String, ServiceEndpointInterfaceMapping>();
        }
        return this.serviceEndpointInterfaceMapping.toMap();
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getVersion() {
        if (version == null) {
            return "1.1";
        } else {
            return version;
        }
    }

    public void setVersion(String value) {
        this.version = value;
    }
}
