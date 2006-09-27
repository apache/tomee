/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.alt.config.ejb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.apache.openejb.alt.config.ejb package.
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

    private final static QName _MethodParam_QNAME = new QName("http://www.openejb.org/openejb-jar/1.1", "method-param");
    private final static QName _MethodName_QNAME = new QName("http://www.openejb.org/openejb-jar/1.1", "method-name");
    private final static QName _ObjectQl_QNAME = new QName("http://www.openejb.org/openejb-jar/1.1", "object-ql");
    private final static QName _Description_QNAME = new QName("http://www.openejb.org/openejb-jar/1.1", "description");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.openejb.alt.config.ejb
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EjbDeployment }
     */
    public EjbDeployment createEjbDeployment() {
        return new EjbDeployment();
    }

    /**
     * Create an instance of {@link OpenejbJar }
     */
    public OpenejbJar createOpenejbJar() {
        return new OpenejbJar();
    }

    /**
     * Create an instance of {@link ResourceLink }
     */
    public ResourceLink createResourceLink() {
        return new ResourceLink();
    }

    /**
     * Create an instance of {@link MethodParams }
     */
    public MethodParams createMethodParams() {
        return new MethodParams();
    }

    /**
     * Create an instance of {@link QueryMethod }
     */
    public QueryMethod createQueryMethod() {
        return new QueryMethod();
    }

    /**
     * Create an instance of {@link Query }
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "http://www.openejb.org/openejb-jar/1.1", name = "method-param")
    public JAXBElement<String> createMethodParam(String value) {
        return new JAXBElement<String>(_MethodParam_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "http://www.openejb.org/openejb-jar/1.1", name = "method-name")
    public JAXBElement<String> createMethodName(String value) {
        return new JAXBElement<String>(_MethodName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "http://www.openejb.org/openejb-jar/1.1", name = "object-ql")
    public JAXBElement<String> createObjectQl(String value) {
        return new JAXBElement<String>(_ObjectQl_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "http://www.openejb.org/openejb-jar/1.1", name = "description")
    public JAXBElement<String> createDescription(String value) {
        return new JAXBElement<String>(_Description_QNAME, String.class, null, value);
    }

}
