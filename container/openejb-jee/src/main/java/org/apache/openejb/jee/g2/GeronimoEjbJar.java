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
package org.apache.openejb.jee.g2;

import org.apache.openejb.jee.oej2.EnvironmentType;
import org.apache.openejb.jee.oej2.MessageDestinationType;
import org.apache.openejb.jee.oej2.AbstractSecurityType;
import org.apache.openejb.jee.oej2.AbstractServiceType;
import org.apache.openejb.jee.oej2.AbstractNamingEntryType;
import org.apache.openejb.jee.oej2.EjbRefType;
import org.apache.openejb.jee.oej2.EjbLocalRefType;
import org.apache.openejb.jee.oej2.ServiceRefType;
import org.apache.openejb.jee.oej2.ResourceRefType;
import org.apache.openejb.jee.oej2.ResourceEnvRefType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.JAXBElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "geronimo-ejb-jarType", namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", propOrder = {
    "environment",
    "messageDestination",
    "security",
    "service"
})
public class GeronimoEjbJar {
    @XmlElement(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected EnvironmentType environment;

    @XmlElementRef(name = "abstract-naming-entry", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractNamingEntryType>> abstractNamingEntry;

    @XmlElement(name = "ejb-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbRefType> ejbRef;

    @XmlElement(name = "ejb-local-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbLocalRefType> ejbLocalRef;

    @XmlElement(name = "service-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ServiceRefType> serviceRef;

    @XmlElement(name = "resource-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceRefType> resourceRef;

    @XmlElement(name = "resource-env-ref", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceEnvRefType> resourceEnvRef;

    @XmlElement(name = "message-destination", namespace="http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<MessageDestinationType> messageDestination;

    @XmlElement(namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2")
    protected AbstractSecurityType security;

    @XmlElementRef(name = "service", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractServiceType>> service;
}
