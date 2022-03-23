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
package org.apache.openejb.jee.was.v6.xmi;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.xmi package.
 *
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Model_QNAME = new QName(
        "http://www.omg.org/XMI", "Model");
    private final static QName _PackageReference_QNAME = new QName(
        "http://www.omg.org/XMI", "PackageReference");
    private final static QName _Difference_QNAME = new QName(
        "http://www.omg.org/XMI", "Difference");
    private final static QName _XMI_QNAME = new QName("http://www.omg.org/XMI",
        "XMI");
    private final static QName _MetaModel_QNAME = new QName(
        "http://www.omg.org/XMI", "MetaModel");
    private final static QName _Extension_QNAME = new QName(
        "http://www.omg.org/XMI", "Extension");
    private final static QName _Delete_QNAME = new QName(
        "http://www.omg.org/XMI", "Delete");
    private final static QName _Add_QNAME = new QName("http://www.omg.org/XMI",
        "Add");
    private final static QName _Import_QNAME = new QName(
        "http://www.omg.org/XMI", "Import");
    private final static QName _Documentation_QNAME = new QName(
        "http://www.omg.org/XMI", "Documentation");
    private final static QName _Replace_QNAME = new QName(
        "http://www.omg.org/XMI", "Replace");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.apache.openejb.jee.was.v6.xmi
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Difference.Target }
     */
    public Difference.Target createDifferenceTarget() {
        return new Difference.Target();
    }

    /**
     * Create an instance of {@link Delete }
     */
    public Delete createDelete() {
        return new Delete();
    }

    /**
     * Create an instance of {@link Add }
     */
    public Add createAdd() {
        return new Add();
    }

    /**
     * Create an instance of {@link Model }
     */
    public Model createModel() {
        return new Model();
    }

    /**
     * Create an instance of {@link MetaModel }
     */
    public MetaModel createMetaModel() {
        return new MetaModel();
    }

    /**
     * Create an instance of {@link XMI }
     */
    public XMI createXMI() {
        return new XMI();
    }

    /**
     * Create an instance of {@link PackageReference }
     */
    public PackageReference createPackageReference() {
        return new PackageReference();
    }

    /**
     * Create an instance of {@link Replace }
     */
    public Replace createReplace() {
        return new Replace();
    }

    /**
     * Create an instance of {@link Extension }
     */
    public Extension createExtension() {
        return new Extension();
    }

    /**
     * Create an instance of {@link Documentation }
     */
    public Documentation createDocumentation() {
        return new Documentation();
    }

    /**
     * Create an instance of {@link Import }
     */
    public Import createImport() {
        return new Import();
    }

    /**
     * Create an instance of {@link Difference }
     */
    public Difference createDifference() {
        return new Difference();
    }

    /**
     * Create an instance of {@link Any }
     */
    public Any createAny() {
        return new Any();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Model }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Model")
    public JAXBElement<Model> createModel(final Model value) {
        return new JAXBElement<Model>(_Model_QNAME, Model.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link PackageReference }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "PackageReference")
    public JAXBElement<PackageReference> createPackageReference(
        final PackageReference value) {
        return new JAXBElement<PackageReference>(_PackageReference_QNAME,
            PackageReference.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Difference }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Difference")
    public JAXBElement<Difference> createDifference(final Difference value) {
        return new JAXBElement<Difference>(_Difference_QNAME, Difference.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMI }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "XMI")
    public JAXBElement<XMI> createXMI(final XMI value) {
        return new JAXBElement<XMI>(_XMI_QNAME, XMI.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MetaModel }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "MetaModel")
    public JAXBElement<MetaModel> createMetaModel(final MetaModel value) {
        return new JAXBElement<MetaModel>(_MetaModel_QNAME, MetaModel.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Extension }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Extension")
    public JAXBElement<Extension> createExtension(final Extension value) {
        return new JAXBElement<Extension>(_Extension_QNAME, Extension.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Delete }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Delete")
    public JAXBElement<Delete> createDelete(final Delete value) {
        return new JAXBElement<Delete>(_Delete_QNAME, Delete.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Add }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Add")
    public JAXBElement<Add> createAdd(final Add value) {
        return new JAXBElement<Add>(_Add_QNAME, Add.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Import }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Import")
    public JAXBElement<Import> createImport(final Import value) {
        return new JAXBElement<Import>(_Import_QNAME, Import.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Documentation }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Documentation")
    public JAXBElement<Documentation> createDocumentation(final Documentation value) {
        return new JAXBElement<Documentation>(_Documentation_QNAME,
            Documentation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Replace }{@code
     * >}
     */
    @XmlElementDecl(namespace = "http://www.omg.org/XMI", name = "Replace")
    public JAXBElement<Replace> createReplace(final Replace value) {
        return new JAXBElement<Replace>(_Replace_QNAME, Replace.class, null,
            value);
    }

}
