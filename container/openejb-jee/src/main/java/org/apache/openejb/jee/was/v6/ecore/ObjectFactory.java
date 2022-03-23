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
package org.apache.openejb.jee.was.v6.ecore;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.ecore
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

    private final static QName _ETypedElement_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "ETypedElement");
    private final static QName _EFactory_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EFactory");
    private final static QName _EOperation_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EOperation");
    private final static QName _EClassifier_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EClassifier");
    private final static QName _EStringToStringMapEntry_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EStringToStringMapEntry");
    private final static QName _EObject_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EObject");
    private final static QName _EAttribute_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EAttribute");
    private final static QName _EModelElement_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EModelElement");
    private final static QName _EEnum_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EEnum");
    private final static QName _EEnumLiteral_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EEnumLiteral");
    private final static QName _EReference_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EReference");
    private final static QName _EStructuralFeature_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EStructuralFeature");
    private final static QName _EAnnotation_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EAnnotation");
    private final static QName _ENamedElement_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "ENamedElement");
    private final static QName _EParameter_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EParameter");
    private final static QName _EDataType_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EDataType");
    private final static QName _EClass_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EClass");
    private final static QName _EPackage_QNAME = new QName(
        "http://www.eclipse.org/emf/2002/Ecore", "EPackage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.apache.openejb.jee.was.v6.ecore
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EFactory }
     */
    public EFactory createEFactory() {
        return new EFactory();
    }

    /**
     * Create an instance of {@link EStringToStringMapEntry }
     */
    public EStringToStringMapEntry createEStringToStringMapEntry() {
        return new EStringToStringMapEntry();
    }

    /**
     * Create an instance of {@link EOperation }
     */
    public EOperation createEOperation() {
        return new EOperation();
    }

    /**
     * Create an instance of {@link EAnnotation }
     */
    public EAnnotation createEAnnotation() {
        return new EAnnotation();
    }

    /**
     * Create an instance of {@link EClassifier }
     */
    public EClassifier createEClassifier() {
        return new EClassifier();
    }

    /**
     * Create an instance of {@link EClass }
     */
    public EClass createEClass() {
        return new EClass();
    }

    /**
     * Create an instance of {@link ETypedElement }
     */
    public ETypedElement createETypedElement() {
        return new ETypedElement();
    }

    /**
     * Create an instance of {@link EParameter }
     */
    public EParameter createEParameter() {
        return new EParameter();
    }

    /**
     * Create an instance of {@link EStructuralFeature }
     */
    public EStructuralFeature createEStructuralFeature() {
        return new EStructuralFeature();
    }

    /**
     * Create an instance of {@link EEnumLiteral }
     */
    public EEnumLiteral createEEnumLiteral() {
        return new EEnumLiteral();
    }

    /**
     * Create an instance of {@link EReference }
     */
    public EReference createEReference() {
        return new EReference();
    }

    /**
     * Create an instance of {@link EEnum }
     */
    public EEnum createEEnum() {
        return new EEnum();
    }

    /**
     * Create an instance of {@link ENamedElement }
     */
    public ENamedElement createENamedElement() {
        return new ENamedElement();
    }

    /**
     * Create an instance of {@link EModelElement }
     */
    public EModelElement createEModelElement() {
        return new EModelElement();
    }

    /**
     * Create an instance of {@link EPackage }
     */
    public EPackage createEPackage() {
        return new EPackage();
    }

    /**
     * Create an instance of {@link EDataType }
     */
    public EDataType createEDataType() {
        return new EDataType();
    }

    /**
     * Create an instance of {@link EObject }
     */
    public EObject createEObject() {
        return new EObject();
    }

    /**
     * Create an instance of {@link EAttribute }
     */
    public EAttribute createEAttribute() {
        return new EAttribute();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ETypedElement }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "ETypedElement")
    public JAXBElement<ETypedElement> createETypedElement(final ETypedElement value) {
        return new JAXBElement<ETypedElement>(_ETypedElement_QNAME,
            ETypedElement.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EFactory }{@code
     * >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EFactory")
    public JAXBElement<EFactory> createEFactory(final EFactory value) {
        return new JAXBElement<EFactory>(_EFactory_QNAME, EFactory.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EOperation }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EOperation")
    public JAXBElement<EOperation> createEOperation(final EOperation value) {
        return new JAXBElement<EOperation>(_EOperation_QNAME, EOperation.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EClassifier }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EClassifier")
    public JAXBElement<EClassifier> createEClassifier(final EClassifier value) {
        return new JAXBElement<EClassifier>(_EClassifier_QNAME,
            EClassifier.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link EStringToStringMapEntry }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EStringToStringMapEntry")
    public JAXBElement<EStringToStringMapEntry> createEStringToStringMapEntry(
        final EStringToStringMapEntry value) {
        return new JAXBElement<EStringToStringMapEntry>(
            _EStringToStringMapEntry_QNAME, EStringToStringMapEntry.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EObject }{@code
     * >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EObject")
    public JAXBElement<EObject> createEObject(final EObject value) {
        return new JAXBElement<EObject>(_EObject_QNAME, EObject.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EAttribute }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EAttribute")
    public JAXBElement<EAttribute> createEAttribute(final EAttribute value) {
        return new JAXBElement<EAttribute>(_EAttribute_QNAME, EAttribute.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EModelElement }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EModelElement")
    public JAXBElement<EModelElement> createEModelElement(final EModelElement value) {
        return new JAXBElement<EModelElement>(_EModelElement_QNAME,
            EModelElement.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EEnum }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EEnum")
    public JAXBElement<EEnum> createEEnum(final EEnum value) {
        return new JAXBElement<EEnum>(_EEnum_QNAME, EEnum.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EEnumLiteral }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EEnumLiteral")
    public JAXBElement<EEnumLiteral> createEEnumLiteral(final EEnumLiteral value) {
        return new JAXBElement<EEnumLiteral>(_EEnumLiteral_QNAME,
            EEnumLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EReference }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EReference")
    public JAXBElement<EReference> createEReference(final EReference value) {
        return new JAXBElement<EReference>(_EReference_QNAME, EReference.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link EStructuralFeature }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EStructuralFeature")
    public JAXBElement<EStructuralFeature> createEStructuralFeature(
        final EStructuralFeature value) {
        return new JAXBElement<EStructuralFeature>(_EStructuralFeature_QNAME,
            EStructuralFeature.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EAnnotation }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EAnnotation")
    public JAXBElement<EAnnotation> createEAnnotation(final EAnnotation value) {
        return new JAXBElement<EAnnotation>(_EAnnotation_QNAME,
            EAnnotation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ENamedElement }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "ENamedElement")
    public JAXBElement<ENamedElement> createENamedElement(final ENamedElement value) {
        return new JAXBElement<ENamedElement>(_ENamedElement_QNAME,
            ENamedElement.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EParameter }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EParameter")
    public JAXBElement<EParameter> createEParameter(final EParameter value) {
        return new JAXBElement<EParameter>(_EParameter_QNAME, EParameter.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EDataType }
     * {@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EDataType")
    public JAXBElement<EDataType> createEDataType(final EDataType value) {
        return new JAXBElement<EDataType>(_EDataType_QNAME, EDataType.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EClass }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EClass")
    public JAXBElement<EClass> createEClass(final EClass value) {
        return new JAXBElement<EClass>(_EClass_QNAME, EClass.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EPackage }{@code
     * >}
     */
    @XmlElementDecl(namespace = "http://www.eclipse.org/emf/2002/Ecore", name = "EPackage")
    public JAXBElement<EPackage> createEPackage(final EPackage value) {
        return new JAXBElement<EPackage>(_EPackage_QNAME, EPackage.class, null,
            value);
    }

}
