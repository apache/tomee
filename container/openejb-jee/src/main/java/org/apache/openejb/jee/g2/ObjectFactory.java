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
import org.apache.openejb.jee.oej2.AbstractNamingEntryType;
import org.apache.openejb.jee.oej2.AbstractServiceType;
import org.apache.openejb.jee.oej2.PersistenceContextRefType;
import org.apache.openejb.jee.oej2.EntityManagerFactoryRefType;

import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.apache.openejb.jee.oej2 package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PersistenceContextRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "persistence-context-ref");
    private final static QName _EntityManagerFactoryRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "entity-manager-factory-ref");
    private final static QName _GeronimoEjbJar_QNAME = new QName("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", "ejb-jar");
    private final static QName _AbstractNamingEntry_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "abstract-naming-entry");
    private final static QName _Service_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "service");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.openejb.jee.oej2
     *
     */
    public ObjectFactory() {
    }


    /**
     * Create an instance of {@link org.apache.openejb.jee.oej2.EnvironmentType }
     *
     */
    public EnvironmentType createEnvironmentType() {
        return new EnvironmentType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oej2.AbstractServiceType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "service")
    public JAXBElement<AbstractServiceType> createService(AbstractServiceType value) {
        return new JAXBElement<AbstractServiceType>(_Service_QNAME, AbstractServiceType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oej2.OpenejbJarType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", name = "ejb-jar")
    public JAXBElement<GeronimoEjbJar> createEjbJar(GeronimoEjbJar value) {
        return new JAXBElement<GeronimoEjbJar>(_GeronimoEjbJar_QNAME, GeronimoEjbJar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oej2.PersistenceContextRefType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "persistence-context-ref", substitutionHeadNamespace = "http://geronimo.apache.org/xml/ns/naming-1.2", substitutionHeadName = "abstract-naming-entry")
    public JAXBElement<PersistenceContextRefType> createPersistenceContextRef(PersistenceContextRefType value) {
        return new JAXBElement<PersistenceContextRefType>(_PersistenceContextRef_QNAME, PersistenceContextRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.apache.openejb.jee.oej2.EntityManagerFactoryRefType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "entity-manager-factory-ref", substitutionHeadNamespace = "http://geronimo.apache.org/xml/ns/naming-1.2", substitutionHeadName = "abstract-naming-entry")
    public JAXBElement<EntityManagerFactoryRefType> createEntityManagerFactoryRef(EntityManagerFactoryRefType value) {
        return new JAXBElement<EntityManagerFactoryRefType>(_EntityManagerFactoryRef_QNAME, EntityManagerFactoryRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractNamingEntryType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "abstract-naming-entry")
    public JAXBElement<AbstractNamingEntryType> createAbstractNamingEntry(AbstractNamingEntryType value) {
        return new JAXBElement<AbstractNamingEntryType>(_AbstractNamingEntry_QNAME, AbstractNamingEntryType.class, null, value);
    }

}
