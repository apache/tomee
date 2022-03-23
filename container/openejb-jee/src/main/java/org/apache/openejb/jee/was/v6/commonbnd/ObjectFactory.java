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
package org.apache.openejb.jee.was.v6.commonbnd;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.commonbnd
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

    private final static QName _Property_QNAME = new QName("commonbnd.xmi",
        "Property");
    private final static QName _AbstractAuthData_QNAME = new QName(
        "commonbnd.xmi", "AbstractAuthData");
    private final static QName _ResourceEnvRefBinding_QNAME = new QName(
        "commonbnd.xmi", "ResourceEnvRefBinding");
    private final static QName _ResourceRefBinding_QNAME = new QName(
        "commonbnd.xmi", "ResourceRefBinding");
    private final static QName _BasicAuthData_QNAME = new QName(
        "commonbnd.xmi", "BasicAuthData");
    private final static QName _MessageDestinationRefBinding_QNAME = new QName(
        "commonbnd.xmi", "MessageDestinationRefBinding");
    private final static QName _EjbRefBinding_QNAME = new QName(
        "commonbnd.xmi", "EjbRefBinding");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.apache.openejb.jee.was.v6.commonbnd
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResourceEnvRefBinding }
     */
    public ResourceEnvRefBinding createResourceEnvRefBinding() {
        return new ResourceEnvRefBinding();
    }

    /**
     * Create an instance of {@link BasicAuthData }
     */
    public BasicAuthData createBasicAuthData() {
        return new BasicAuthData();
    }

    /**
     * Create an instance of {@link MessageDestinationRefBinding }
     */
    public MessageDestinationRefBinding createMessageDestinationRefBinding() {
        return new MessageDestinationRefBinding();
    }

    /**
     * Create an instance of {@link AbstractAuthData }
     */
    public AbstractAuthData createAbstractAuthData() {
        return new AbstractAuthData();
    }

    /**
     * Create an instance of {@link Property }
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link ResourceRefBinding }
     */
    public ResourceRefBinding createResourceRefBinding() {
        return new ResourceRefBinding();
    }

    /**
     * Create an instance of {@link EjbRefBinding }
     */
    public EjbRefBinding createEjbRefBinding() {
        return new EjbRefBinding();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Property }{@code
     * >}
     */
    @XmlElementDecl(namespace = "commonbnd.xmi", name = "Property")
    public JAXBElement<Property> createProperty(final Property value) {
        return new JAXBElement<Property>(_Property_QNAME, Property.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link AbstractAuthData }{@code >}
     */
    @XmlElementDecl(namespace = "commonbnd.xmi", name = "AbstractAuthData")
    public JAXBElement<AbstractAuthData> createAbstractAuthData(
        final AbstractAuthData value) {
        return new JAXBElement<AbstractAuthData>(_AbstractAuthData_QNAME,
            AbstractAuthData.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link ResourceEnvRefBinding }{@code >}
     */
    @XmlElementDecl(namespace = "commonbnd.xmi", name = "ResourceEnvRefBinding")
    public JAXBElement<ResourceEnvRefBinding> createResourceEnvRefBinding(
        final ResourceEnvRefBinding value) {
        return new JAXBElement<ResourceEnvRefBinding>(
            _ResourceEnvRefBinding_QNAME, ResourceEnvRefBinding.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link ResourceRefBinding }{@code >}
     */
    @XmlElementDecl(namespace = "commonbnd.xmi", name = "ResourceRefBinding")
    public JAXBElement<ResourceRefBinding> createResourceRefBinding(
        final ResourceRefBinding value) {
        return new JAXBElement<ResourceRefBinding>(_ResourceRefBinding_QNAME,
            ResourceRefBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicAuthData }
     * {@code >}
     */
    @XmlElementDecl(namespace = "commonbnd.xmi", name = "BasicAuthData")
    public JAXBElement<BasicAuthData> createBasicAuthData(final BasicAuthData value) {
        return new JAXBElement<BasicAuthData>(_BasicAuthData_QNAME,
            BasicAuthData.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link MessageDestinationRefBinding }{@code >}
     */
    @XmlElementDecl(namespace = "commonbnd.xmi", name = "MessageDestinationRefBinding")
    public JAXBElement<MessageDestinationRefBinding> createMessageDestinationRefBinding(
        final MessageDestinationRefBinding value) {
        return new JAXBElement<MessageDestinationRefBinding>(
            _MessageDestinationRefBinding_QNAME,
            MessageDestinationRefBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbRefBinding }
     * {@code >}
     */
    @XmlElementDecl(namespace = "commonbnd.xmi", name = "EjbRefBinding")
    public JAXBElement<EjbRefBinding> createEjbRefBinding(final EjbRefBinding value) {
        return new JAXBElement<EjbRefBinding>(_EjbRefBinding_QNAME,
            EjbRefBinding.class, null, value);
    }

}
