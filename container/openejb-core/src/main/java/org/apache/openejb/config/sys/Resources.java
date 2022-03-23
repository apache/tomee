/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config.sys;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Container" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Resource" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.openejb.org/System/Configuration}Service" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"container", "resource", "service", "connector", "jndiProvider"})
@XmlRootElement(name = "resources")
public class Resources {

    @XmlElement(name = "Resource")
    protected List<Resource> resource;

    @XmlElement(name = "Container")
    protected List<Container> container;

    @XmlElement(name = "Service")
    protected List<Service> service;

    @XmlElement(name = "Connector")
    protected List<Connector> connector;

    @XmlElement(name = "JndiProvider")
    protected List<JndiProvider> jndiProvider;

    public List<Resource> getResource() {
        if (resource == null) {
            resource = new ArrayList<>();
        }

        return this.resource;
    }

    public List<Container> getContainer() {
        if (container == null) {
            container = new ArrayList<>();
        }

        return this.container;
    }

    public List<Service> getService() {
        if (service == null) {
            service = new ArrayList<>();
        }
        return service;
    }

    public List<Connector> getConnector() {
        if (connector == null) {
            connector = new ArrayList<>();
        }
        return this.connector;
    }

    public List<JndiProvider> getJndiProvider() {
        if (jndiProvider == null) {
            jndiProvider = new ArrayList<>();
        }
        return this.jndiProvider;
    }

    public void add(final Object service) {
        if (service instanceof Resource) {
            getResource().add((Resource) service);
        } else if (service instanceof Service) {
            getService().add((Service) service);
        } else if (service instanceof Connector) {
            getConnector().add((Connector) service);
        } else if (JndiProvider.class.isInstance(service)) {
            getJndiProvider().add(JndiProvider.class.cast(service));
        }
    }
}
