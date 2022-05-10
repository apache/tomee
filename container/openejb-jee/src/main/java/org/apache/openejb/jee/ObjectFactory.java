/**
 *
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

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.apache.openejb.jee2 package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Application_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "application");
    private final static QName _ApplicationClient_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "application-client");
    private final static QName _EjbJar_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-jar");
    private final static QName _EjbRelationTypeEjbRelationName_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-relation-name");
    private final static QName _EjbRelationTypeEjbRelationshipRole_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-relationship-role");
    private final static QName _EjbRelationTypeDescription_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "description");
    private final static QName _WebResourceCollectionTypeHttpMethod_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "http-method");
    private final static QName _TldTaglib_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "web-app");
    private final static QName _WebApp_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "taglib");
    private final static QName _Connector_QNAME = new QName("http://java.sun.com/xml/ns/j2ee", "connector");
    private final static QName _JavaWsdlMapping_QNAME = new QName("http://java.sun.com/xml/ns/j2ee", "java-wsdl-mapping");
    private final static QName _Webservices_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "webservices");
    private final static QName _FacesConfig_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "faces-config");
    private final static QName _WebFragment_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "web-fragment");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.openejb.jee2
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Application }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "application")
    public JAXBElement<Application> createApplication(final Application value) {
        return new JAXBElement<Application>(_Application_QNAME, Application.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplicationClient }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "application-client")
    public JAXBElement<ApplicationClient> createApplicationClient(final ApplicationClient value) {
        return new JAXBElement<ApplicationClient>(_ApplicationClient_QNAME, ApplicationClient.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbJar }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-jar")
    public JAXBElement<EjbJar> createEjbJar(final EjbJar value) {
        return new JAXBElement<EjbJar>(_EjbJar_QNAME, EjbJar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-relation-name", scope = EjbRelation.class)
    public JAXBElement<String> createEjbRelationTypeEjbRelationName(final String value) {
        return new JAXBElement<String>(_EjbRelationTypeEjbRelationName_QNAME, String.class, EjbRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbRelationshipRole }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-relationship-role", scope = EjbRelation.class)
    public JAXBElement<EjbRelationshipRole> createEjbRelationTypeEjbRelationshipRole(final EjbRelationshipRole value) {
        return new JAXBElement<EjbRelationshipRole>(_EjbRelationTypeEjbRelationshipRole_QNAME, EjbRelationshipRole.class, EjbRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "description", scope = EjbRelation.class)
    public JAXBElement<Text> createEjbRelationTypeDescription(final Text value) {
        return new JAXBElement<Text>(_EjbRelationTypeDescription_QNAME, Text.class, EjbRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "http-method", scope = WebResourceCollection.class)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createWebResourceCollectionTypeHttpMethod(final String value) {
        return new JAXBElement<String>(_WebResourceCollectionTypeHttpMethod_QNAME, String.class, WebResourceCollection.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebApp }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "web-app")
    public JAXBElement<WebApp> createWebApp(final WebApp value) {
        return new JAXBElement<WebApp>(_WebApp_QNAME, WebApp.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TldTaglib }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "taglib")
    public JAXBElement<TldTaglib> createTldTaglib(final TldTaglib value) {
        return new JAXBElement<TldTaglib>(_TldTaglib_QNAME, TldTaglib.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Connector }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/j2ee", name = "connector")
    public JAXBElement<Connector> createConnector(final Connector value) {
        return new JAXBElement<Connector>(_Connector_QNAME, Connector.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JavaWsdlMapping }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/j2ee", name = "java-wsdl-mapping")
    public JAXBElement<JavaWsdlMapping> createConnector(final JavaWsdlMapping value) {
        return new JAXBElement<JavaWsdlMapping>(_JavaWsdlMapping_QNAME, JavaWsdlMapping.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Webservices }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "webservices")
    public JAXBElement<Webservices> createConnector(final Webservices value) {
        return new JAXBElement<Webservices>(_Webservices_QNAME, Webservices.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FacesConfig }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "faces-config")
    public JAXBElement<FacesConfig> createFacesConfig(final FacesConfig value) {
        return new JAXBElement<FacesConfig>(_FacesConfig_QNAME, FacesConfig.class, null, value);
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebFragment }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "web-fragment")
    public JAXBElement<WebFragment> createWebFragment(final WebFragment value) {
        return new JAXBElement<WebFragment>(_WebFragment_QNAME, WebFragment.class, null, value);
    }
}
