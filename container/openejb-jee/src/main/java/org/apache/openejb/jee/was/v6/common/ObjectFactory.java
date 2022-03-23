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
package org.apache.openejb.jee.was.v6.common;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.common
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

    private final static javax.xml.namespace.QName _QName_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "QName");
    private final static javax.xml.namespace.QName _Listener_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "Listener");
    private final static javax.xml.namespace.QName _SecurityRoleRef_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "SecurityRoleRef");
    private final static javax.xml.namespace.QName _Identity_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "Identity");
    private final static javax.xml.namespace.QName _Description_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "Description");
    private final static javax.xml.namespace.QName _EnvEntry_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "EnvEntry");
    private final static javax.xml.namespace.QName _DisplayName_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "DisplayName");
    private final static javax.xml.namespace.QName _ResourceEnvRef_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "ResourceEnvRef");
    private final static javax.xml.namespace.QName _DescriptionGroup_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "DescriptionGroup");
    private final static javax.xml.namespace.QName _SecurityIdentity_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "SecurityIdentity");
    private final static javax.xml.namespace.QName _RunAsSpecifiedIdentity_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "RunAsSpecifiedIdentity");
    private final static javax.xml.namespace.QName _IconType_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "IconType");
    private final static javax.xml.namespace.QName _UseCallerIdentity_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "UseCallerIdentity");
    private final static javax.xml.namespace.QName _MessageDestination_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "MessageDestination");
    private final static javax.xml.namespace.QName _MessageDestinationRef_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "MessageDestinationRef");
    private final static javax.xml.namespace.QName _CompatibilityDescriptionGroup_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "CompatibilityDescriptionGroup");
    private final static javax.xml.namespace.QName _ResourceRef_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "ResourceRef");
    private final static javax.xml.namespace.QName _ParamValue_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "ParamValue");
    private final static javax.xml.namespace.QName _EjbRef_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "EjbRef");
    private final static javax.xml.namespace.QName _EJBLocalRef_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "EJBLocalRef");
    private final static javax.xml.namespace.QName _SecurityRole_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "SecurityRole");
    private final static javax.xml.namespace.QName _JNDIEnvRefsGroup_QNAME = new javax.xml.namespace.QName(
        "common.xmi", "JNDIEnvRefsGroup");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.apache.openejb.jee.was.v6.common
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EJBLocalRef }
     */
    public EJBLocalRef createEJBLocalRef() {
        return new EJBLocalRef();
    }

    /**
     * Create an instance of {@link SecurityRole }
     */
    public SecurityRole createSecurityRole() {
        return new SecurityRole();
    }

    /**
     * Create an instance of {@link ResourceRef }
     */
    public ResourceRef createResourceRef() {
        return new ResourceRef();
    }

    /**
     * Create an instance of {@link ResourceEnvRef }
     */
    public ResourceEnvRef createResourceEnvRef() {
        return new ResourceEnvRef();
    }

    /**
     * Create an instance of {@link ParamValue }
     */
    public ParamValue createParamValue() {
        return new ParamValue();
    }

    /**
     * Create an instance of {@link DisplayName }
     */
    public DisplayName createDisplayName() {
        return new DisplayName();
    }

    /**
     * Create an instance of {@link CompatibilityDescriptionGroup }
     */
    public CompatibilityDescriptionGroup createCompatibilityDescriptionGroup() {
        return new CompatibilityDescriptionGroup();
    }

    /**
     * Create an instance of {@link Identity }
     */
    public Identity createIdentity() {
        return new Identity();
    }

    /**
     * Create an instance of {@link SecurityRoleRef }
     */
    public SecurityRoleRef createSecurityRoleRef() {
        return new SecurityRoleRef();
    }

    /**
     * Create an instance of {@link IconType }
     */
    public IconType createIconType() {
        return new IconType();
    }

    /**
     * Create an instance of {@link SecurityIdentity }
     */
    public SecurityIdentity createSecurityIdentity() {
        return new SecurityIdentity();
    }

    /**
     * Create an instance of {@link UseCallerIdentity }
     */
    public UseCallerIdentity createUseCallerIdentity() {
        return new UseCallerIdentity();
    }

    /**
     * Create an instance of {@link MessageDestinationRef }
     */
    public MessageDestinationRef createMessageDestinationRef() {
        return new MessageDestinationRef();
    }

    /**
     * Create an instance of {@link org.apache.openejb.jee.was.v6.common.QName }
     */
    public org.apache.openejb.jee.was.v6.common.QName createQName() {
        return new org.apache.openejb.jee.was.v6.common.QName();
    }

    /**
     * Create an instance of {@link MessageDestination }
     */
    public MessageDestination createMessageDestination() {
        return new MessageDestination();
    }

    /**
     * Create an instance of {@link EjbRef }
     */
    public EjbRef createEjbRef() {
        return new EjbRef();
    }

    /**
     * Create an instance of {@link Description }
     */
    public Description createDescription() {
        return new Description();
    }

    /**
     * Create an instance of {@link JNDIEnvRefsGroup }
     */
    public JNDIEnvRefsGroup createJNDIEnvRefsGroup() {
        return new JNDIEnvRefsGroup();
    }

    /**
     * Create an instance of {@link EnvEntry }
     */
    public EnvEntry createEnvEntry() {
        return new EnvEntry();
    }

    /**
     * Create an instance of {@link RunAsSpecifiedIdentity }
     */
    public RunAsSpecifiedIdentity createRunAsSpecifiedIdentity() {
        return new RunAsSpecifiedIdentity();
    }

    /**
     * Create an instance of {@link Listener }
     */
    public Listener createListener() {
        return new Listener();
    }

    /**
     * Create an instance of {@link DescriptionGroup }
     */
    public DescriptionGroup createDescriptionGroup() {
        return new DescriptionGroup();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link org.apache.openejb.jee.was.v6.common.QName }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "QName")
    public JAXBElement<org.apache.openejb.jee.was.v6.common.QName> createQName(
        final org.apache.openejb.jee.was.v6.common.QName value) {
        return new JAXBElement<org.apache.openejb.jee.was.v6.common.QName>(
            _QName_QNAME, org.apache.openejb.jee.was.v6.common.QName.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Listener }{@code
     * >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "Listener")
    public JAXBElement<Listener> createListener(final Listener value) {
        return new JAXBElement<Listener>(_Listener_QNAME, Listener.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SecurityRoleRef }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "SecurityRoleRef")
    public JAXBElement<SecurityRoleRef> createSecurityRoleRef(
        final SecurityRoleRef value) {
        return new JAXBElement<SecurityRoleRef>(_SecurityRoleRef_QNAME,
            SecurityRoleRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Identity }{@code
     * >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "Identity")
    public JAXBElement<Identity> createIdentity(final Identity value) {
        return new JAXBElement<Identity>(_Identity_QNAME, Identity.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Description }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "Description")
    public JAXBElement<Description> createDescription(final Description value) {
        return new JAXBElement<Description>(_Description_QNAME,
            Description.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvEntry }{@code
     * >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "EnvEntry")
    public JAXBElement<EnvEntry> createEnvEntry(final EnvEntry value) {
        return new JAXBElement<EnvEntry>(_EnvEntry_QNAME, EnvEntry.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DisplayName }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "DisplayName")
    public JAXBElement<DisplayName> createDisplayName(final DisplayName value) {
        return new JAXBElement<DisplayName>(_DisplayName_QNAME,
            DisplayName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceEnvRef }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "ResourceEnvRef")
    public JAXBElement<ResourceEnvRef> createResourceEnvRef(final ResourceEnvRef value) {
        return new JAXBElement<ResourceEnvRef>(_ResourceEnvRef_QNAME,
            ResourceEnvRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link DescriptionGroup }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "DescriptionGroup")
    public JAXBElement<DescriptionGroup> createDescriptionGroup(
        final DescriptionGroup value) {
        return new JAXBElement<DescriptionGroup>(_DescriptionGroup_QNAME,
            DescriptionGroup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link SecurityIdentity }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "SecurityIdentity")
    public JAXBElement<SecurityIdentity> createSecurityIdentity(
        final SecurityIdentity value) {
        return new JAXBElement<SecurityIdentity>(_SecurityIdentity_QNAME,
            SecurityIdentity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link RunAsSpecifiedIdentity }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "RunAsSpecifiedIdentity")
    public JAXBElement<RunAsSpecifiedIdentity> createRunAsSpecifiedIdentity(
        final RunAsSpecifiedIdentity value) {
        return new JAXBElement<RunAsSpecifiedIdentity>(
            _RunAsSpecifiedIdentity_QNAME, RunAsSpecifiedIdentity.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IconType }{@code
     * >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "IconType")
    public JAXBElement<IconType> createIconType(final IconType value) {
        return new JAXBElement<IconType>(_IconType_QNAME, IconType.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link UseCallerIdentity }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "UseCallerIdentity")
    public JAXBElement<UseCallerIdentity> createUseCallerIdentity(
        final UseCallerIdentity value) {
        return new JAXBElement<UseCallerIdentity>(_UseCallerIdentity_QNAME,
            UseCallerIdentity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link MessageDestination }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "MessageDestination")
    public JAXBElement<MessageDestination> createMessageDestination(
        final MessageDestination value) {
        return new JAXBElement<MessageDestination>(_MessageDestination_QNAME,
            MessageDestination.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link MessageDestinationRef }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "MessageDestinationRef")
    public JAXBElement<MessageDestinationRef> createMessageDestinationRef(
        final MessageDestinationRef value) {
        return new JAXBElement<MessageDestinationRef>(
            _MessageDestinationRef_QNAME, MessageDestinationRef.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link CompatibilityDescriptionGroup }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "CompatibilityDescriptionGroup")
    public JAXBElement<CompatibilityDescriptionGroup> createCompatibilityDescriptionGroup(
        final CompatibilityDescriptionGroup value) {
        return new JAXBElement<CompatibilityDescriptionGroup>(
            _CompatibilityDescriptionGroup_QNAME,
            CompatibilityDescriptionGroup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceRef }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "ResourceRef")
    public JAXBElement<ResourceRef> createResourceRef(final ResourceRef value) {
        return new JAXBElement<ResourceRef>(_ResourceRef_QNAME,
            ResourceRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParamValue }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "ParamValue")
    public JAXBElement<ParamValue> createParamValue(final ParamValue value) {
        return new JAXBElement<ParamValue>(_ParamValue_QNAME, ParamValue.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbRef }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "EjbRef")
    public JAXBElement<EjbRef> createEjbRef(final EjbRef value) {
        return new JAXBElement<EjbRef>(_EjbRef_QNAME, EjbRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EJBLocalRef }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "EJBLocalRef")
    public JAXBElement<EJBLocalRef> createEJBLocalRef(final EJBLocalRef value) {
        return new JAXBElement<EJBLocalRef>(_EJBLocalRef_QNAME,
            EJBLocalRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SecurityRole }
     * {@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "SecurityRole")
    public JAXBElement<SecurityRole> createSecurityRole(final SecurityRole value) {
        return new JAXBElement<SecurityRole>(_SecurityRole_QNAME,
            SecurityRole.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link JNDIEnvRefsGroup }{@code >}
     */
    @XmlElementDecl(namespace = "common.xmi", name = "JNDIEnvRefsGroup")
    public JAXBElement<JNDIEnvRefsGroup> createJNDIEnvRefsGroup(
        final JNDIEnvRefsGroup value) {
        return new JAXBElement<JNDIEnvRefsGroup>(_JNDIEnvRefsGroup_QNAME,
            JNDIEnvRefsGroup.class, null, value);
    }

}
