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
package org.apache.openejb.jee.was.v6.java;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.java
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

    private final static QName _JavaEvent_QNAME = new QName("java.xmi",
        "JavaEvent");
    private final static QName _JavaParameter_QNAME = new QName("java.xmi",
        "JavaParameter");
    private final static QName _JavaClass_QNAME = new QName("java.xmi",
        "JavaClass");
    private final static QName _Initializer_QNAME = new QName("java.xmi",
        "Initializer");
    private final static QName _Field_QNAME = new QName("java.xmi", "Field");
    private final static QName _JavaDataType_QNAME = new QName("java.xmi",
        "JavaDataType");
    private final static QName _Statement_QNAME = new QName("java.xmi",
        "Statement");
    private final static QName _Comment_QNAME = new QName("java.xmi", "Comment");
    private final static QName _Method_QNAME = new QName("java.xmi", "Method");
    private final static QName _JavaPackage_QNAME = new QName("java.xmi",
        "JavaPackage");
    private final static QName _ArrayType_QNAME = new QName("java.xmi",
        "ArrayType");
    private final static QName _Block_QNAME = new QName("java.xmi", "Block");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.apache.openejb.jee.was.v6.java
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JavaDataType }
     */
    public JavaDataType createJavaDataType() {
        return new JavaDataType();
    }

    /**
     * Create an instance of {@link JavaParameter }
     */
    public JavaParameter createJavaParameter() {
        return new JavaParameter();
    }

    /**
     * Create an instance of {@link JavaEvent }
     */
    public JavaEvent createJavaEvent() {
        return new JavaEvent();
    }

    /**
     * Create an instance of {@link JavaClass }
     */
    public JavaClass createJavaClass() {
        return new JavaClass();
    }

    /**
     * Create an instance of {@link JavaPackage }
     */
    public JavaPackage createJavaPackage() {
        return new JavaPackage();
    }

    /**
     * Create an instance of {@link Statement }
     */
    public Statement createStatement() {
        return new Statement();
    }

    /**
     * Create an instance of {@link Comment }
     */
    public Comment createComment() {
        return new Comment();
    }

    /**
     * Create an instance of {@link Method }
     */
    public Method createMethod() {
        return new Method();
    }

    /**
     * Create an instance of {@link Block }
     */
    public Block createBlock() {
        return new Block();
    }

    /**
     * Create an instance of {@link Field }
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link Initializer }
     */
    public Initializer createInitializer() {
        return new Initializer();
    }

    /**
     * Create an instance of {@link ArrayType }
     */
    public ArrayType createArrayType() {
        return new ArrayType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JavaEvent }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "JavaEvent")
    public JAXBElement<JavaEvent> createJavaEvent(final JavaEvent value) {
        return new JAXBElement<JavaEvent>(_JavaEvent_QNAME, JavaEvent.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JavaParameter }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "JavaParameter")
    public JAXBElement<JavaParameter> createJavaParameter(final JavaParameter value) {
        return new JAXBElement<JavaParameter>(_JavaParameter_QNAME,
            JavaParameter.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JavaClass }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "JavaClass")
    public JAXBElement<JavaClass> createJavaClass(final JavaClass value) {
        return new JAXBElement<JavaClass>(_JavaClass_QNAME, JavaClass.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Initializer }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "Initializer")
    public JAXBElement<Initializer> createInitializer(final Initializer value) {
        return new JAXBElement<Initializer>(_Initializer_QNAME,
            Initializer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Field }{@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "Field")
    public JAXBElement<Field> createField(final Field value) {
        return new JAXBElement<Field>(_Field_QNAME, Field.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JavaDataType }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "JavaDataType")
    public JAXBElement<JavaDataType> createJavaDataType(final JavaDataType value) {
        return new JAXBElement<JavaDataType>(_JavaDataType_QNAME,
            JavaDataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Statement }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "Statement")
    public JAXBElement<Statement> createStatement(final Statement value) {
        return new JAXBElement<Statement>(_Statement_QNAME, Statement.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Comment }{@code
     * >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "Comment")
    public JAXBElement<Comment> createComment(final Comment value) {
        return new JAXBElement<Comment>(_Comment_QNAME, Comment.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "Method")
    public JAXBElement<Method> createMethod(final Method value) {
        return new JAXBElement<Method>(_Method_QNAME, Method.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JavaPackage }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "JavaPackage")
    public JAXBElement<JavaPackage> createJavaPackage(final JavaPackage value) {
        return new JAXBElement<JavaPackage>(_JavaPackage_QNAME,
            JavaPackage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayType }
     * {@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "ArrayType")
    public JAXBElement<ArrayType> createArrayType(final ArrayType value) {
        return new JAXBElement<ArrayType>(_ArrayType_QNAME, ArrayType.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Block }{@code >}
     */
    @XmlElementDecl(namespace = "java.xmi", name = "Block")
    public JAXBElement<Block> createBlock(final Block value) {
        return new JAXBElement<Block>(_Block_QNAME, Block.class, null, value);
    }

}
