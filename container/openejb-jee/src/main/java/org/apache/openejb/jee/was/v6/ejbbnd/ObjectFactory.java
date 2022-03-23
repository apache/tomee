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
package org.apache.openejb.jee.was.v6.ejbbnd;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.ejbbnd
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

    private final static QName _EJBJarBinding_QNAME = new QName("ejbbnd.xmi",
        "EJBJarBinding");
    private final static QName _MessageDrivenBeanBinding_QNAME = new QName(
        "ejbbnd.xmi", "MessageDrivenBeanBinding");
    private final static QName _CMPConnectionFactoryBinding_QNAME = new QName(
        "ejbbnd.xmi", "CMPConnectionFactoryBinding");
    private final static QName _EnterpriseBeanBinding_QNAME = new QName(
        "ejbbnd.xmi", "EnterpriseBeanBinding");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.apache.openejb.jee.was.v6.ejbbnd
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EnterpriseBeanBinding }
     */
    public EnterpriseBeanBinding createEnterpriseBeanBinding() {
        return new EnterpriseBeanBinding();
    }

    /**
     * Create an instance of {@link CMPConnectionFactoryBinding }
     */
    public CMPConnectionFactoryBinding createCMPConnectionFactoryBinding() {
        return new CMPConnectionFactoryBinding();
    }

    /**
     * Create an instance of {@link EJBJarBinding }
     */
    public EJBJarBinding createEJBJarBinding() {
        return new EJBJarBinding();
    }

    /**
     * Create an instance of {@link MessageDrivenBeanBinding }
     */
    public MessageDrivenBeanBinding createMessageDrivenBeanBinding() {
        return new MessageDrivenBeanBinding();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EJBJarBinding }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejbbnd.xmi", name = "EJBJarBinding")
    public JAXBElement<EJBJarBinding> createEJBJarBinding(final EJBJarBinding value) {
        return new JAXBElement<EJBJarBinding>(_EJBJarBinding_QNAME,
            EJBJarBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link MessageDrivenBeanBinding }{@code >}
     */
    @XmlElementDecl(namespace = "ejbbnd.xmi", name = "MessageDrivenBeanBinding")
    public JAXBElement<MessageDrivenBeanBinding> createMessageDrivenBeanBinding(
        final MessageDrivenBeanBinding value) {
        return new JAXBElement<MessageDrivenBeanBinding>(
            _MessageDrivenBeanBinding_QNAME,
            MessageDrivenBeanBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link CMPConnectionFactoryBinding }{@code >}
     */
    @XmlElementDecl(namespace = "ejbbnd.xmi", name = "CMPConnectionFactoryBinding")
    public JAXBElement<CMPConnectionFactoryBinding> createCMPConnectionFactoryBinding(
        final CMPConnectionFactoryBinding value) {
        return new JAXBElement<CMPConnectionFactoryBinding>(
            _CMPConnectionFactoryBinding_QNAME,
            CMPConnectionFactoryBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link EnterpriseBeanBinding }{@code >}
     */
    @XmlElementDecl(namespace = "ejbbnd.xmi", name = "EnterpriseBeanBinding")
    public JAXBElement<EnterpriseBeanBinding> createEnterpriseBeanBinding(
        final EnterpriseBeanBinding value) {
        return new JAXBElement<EnterpriseBeanBinding>(
            _EnterpriseBeanBinding_QNAME, EnterpriseBeanBinding.class,
            null, value);
    }

}
