/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.wsclient;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.wsclient
 * package.
 *
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _WebServicesClient_QNAME = new QName(
        "webservice_client.xmi", "WebServicesClient");
    private final static QName _ComponentScopedRefs_QNAME = new QName(
        "webservice_client.xmi", "ComponentScopedRefs");
    private final static QName _Handler_QNAME = new QName(
        "webservice_client.xmi", "Handler");
    private final static QName _PortComponentRef_QNAME = new QName(
        "webservice_client.xmi", "PortComponentRef");
    private final static QName _ServiceRef_QNAME = new QName(
        "webservice_client.xmi", "ServiceRef");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.apache.openejb.jee.was.v6.wsclient
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PortComponentRef }
     */
    public PortComponentRef createPortComponentRef() {
        return new PortComponentRef();
    }

    /**
     * Create an instance of {@link ServiceRef }
     */
    public ServiceRef createServiceRef() {
        return new ServiceRef();
    }

    /**
     * Create an instance of {@link ComponentScopedRefs }
     */
    public ComponentScopedRefs createComponentScopedRefs() {
        return new ComponentScopedRefs();
    }

    /**
     * Create an instance of {@link WebServicesClient }
     */
    public WebServicesClient createWebServicesClient() {
        return new WebServicesClient();
    }

    /**
     * Create an instance of {@link Handler }
     */
    public Handler createHandler() {
        return new Handler();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link WebServicesClient }{@code >}
     */
    @XmlElementDecl(namespace = "webservice_client.xmi", name = "WebServicesClient")
    public JAXBElement<WebServicesClient> createWebServicesClient(
        final WebServicesClient value) {
        return new JAXBElement<WebServicesClient>(_WebServicesClient_QNAME,
            WebServicesClient.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link ComponentScopedRefs }{@code >}
     */
    @XmlElementDecl(namespace = "webservice_client.xmi", name = "ComponentScopedRefs")
    public JAXBElement<ComponentScopedRefs> createComponentScopedRefs(
        final ComponentScopedRefs value) {
        return new JAXBElement<ComponentScopedRefs>(_ComponentScopedRefs_QNAME,
            ComponentScopedRefs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Handler }{@code
     * >}
     */
    @XmlElementDecl(namespace = "webservice_client.xmi", name = "Handler")
    public JAXBElement<Handler> createHandler(final Handler value) {
        return new JAXBElement<Handler>(_Handler_QNAME, Handler.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link PortComponentRef }{@code >}
     */
    @XmlElementDecl(namespace = "webservice_client.xmi", name = "PortComponentRef")
    public JAXBElement<PortComponentRef> createPortComponentRef(
        final PortComponentRef value) {
        return new JAXBElement<PortComponentRef>(_PortComponentRef_QNAME,
            PortComponentRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceRef }
     * {@code >}
     */
    @XmlElementDecl(namespace = "webservice_client.xmi", name = "ServiceRef")
    public JAXBElement<ServiceRef> createServiceRef(final ServiceRef value) {
        return new JAXBElement<ServiceRef>(_ServiceRef_QNAME, ServiceRef.class,
            null, value);
    }

}
