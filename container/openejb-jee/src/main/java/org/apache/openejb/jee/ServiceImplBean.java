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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The service-impl-bean element defines the web service implementation.
 * A service implementation can be an EJB bean class or JAX-RPC web
 * component.  Existing EJB implementations are exposed as a web service
 * using an ejb-link.
 * <p/>
 * Used in: port-component
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-impl-beanType", propOrder = {"ejbLink", "servletLink"})
public class ServiceImplBean {
    @XmlElement(name = "ejb-link")
    protected String ejbLink;
    @XmlElement(name = "servlet-link")
    protected String servletLink;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbLink(String value) {
        this.ejbLink = value;
    }

    public String getServletLink() {
        return servletLink;
    }

    public void setServletLink(String value) {
        this.servletLink = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
