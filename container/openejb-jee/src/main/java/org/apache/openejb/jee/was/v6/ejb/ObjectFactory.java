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
package org.apache.openejb.jee.was.v6.ejb;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.apache.openejb.jee.was.v6.ejb package.
 *
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CMPAttribute_QNAME = new QName("ejb.xmi",
        "CMPAttribute");
    private final static QName _ExcludeList_QNAME = new QName("ejb.xmi",
        "ExcludeList");
    private final static QName _EJBJar_QNAME = new QName("ejb.xmi", "EJBJar");
    private final static QName _MessageDriven_QNAME = new QName("ejb.xmi",
        "MessageDriven");
    private final static QName _EJBMethodCategory_QNAME = new QName("ejb.xmi",
        "EJBMethodCategory");
    private final static QName _EnterpriseBean_QNAME = new QName("ejb.xmi",
        "EnterpriseBean");
    private final static QName _ContainerManagedEntity_QNAME = new QName(
        "ejb.xmi", "ContainerManagedEntity");
    private final static QName _MessageDrivenDestination_QNAME = new QName(
        "ejb.xmi", "MessageDrivenDestination");
    private final static QName _EJBRelation_QNAME = new QName("ejb.xmi",
        "EJBRelation");
    private final static QName _EJBRelationshipRole_QNAME = new QName(
        "ejb.xmi", "EJBRelationshipRole");
    private final static QName _RoleSource_QNAME = new QName("ejb.xmi",
        "RoleSource");
    private final static QName _ActivationConfigProperty_QNAME = new QName(
        "ejb.xmi", "ActivationConfigProperty");
    private final static QName _Session_QNAME = new QName("ejb.xmi", "Session");
    private final static QName _MethodElement_QNAME = new QName("ejb.xmi",
        "MethodElement");
    private final static QName _MethodTransaction_QNAME = new QName("ejb.xmi",
        "MethodTransaction");
    private final static QName _AssemblyDescriptor_QNAME = new QName("ejb.xmi",
        "AssemblyDescriptor");
    private final static QName _Entity_QNAME = new QName("ejb.xmi", "Entity");
    private final static QName _Query_QNAME = new QName("ejb.xmi", "Query");
    private final static QName _MethodPermission_QNAME = new QName("ejb.xmi",
        "MethodPermission");
    private final static QName _Relationships_QNAME = new QName("ejb.xmi",
        "Relationships");
    private final static QName _CMRField_QNAME = new QName("ejb.xmi",
        "CMRField");
    private final static QName _QueryMethod_QNAME = new QName("ejb.xmi",
        "QueryMethod");
    private final static QName _ActivationConfig_QNAME = new QName("ejb.xmi",
        "ActivationConfig");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.apache.openejb.jee.was.v6.ejb
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ActivationConfig }
     */
    public ActivationConfig createActivationConfig() {
        return new ActivationConfig();
    }

    /**
     * Create an instance of {@link MessageDrivenDestination }
     */
    public MessageDrivenDestination createMessageDrivenDestination() {
        return new MessageDrivenDestination();
    }

    /**
     * Create an instance of {@link Session }
     */
    public Session createSession() {
        return new Session();
    }

    /**
     * Create an instance of {@link EnterpriseBean }
     */
    public EnterpriseBean createEnterpriseBean() {
        return new EnterpriseBean();
    }

    /**
     * Create an instance of {@link Query }
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link MessageDriven }
     */
    public MessageDriven createMessageDriven() {
        return new MessageDriven();
    }

    /**
     * Create an instance of {@link EJBRelation }
     */
    public EJBRelation createEJBRelation() {
        return new EJBRelation();
    }

    /**
     * Create an instance of {@link AssemblyDescriptor }
     */
    public AssemblyDescriptor createAssemblyDescriptor() {
        return new AssemblyDescriptor();
    }

    /**
     * Create an instance of {@link EJBMethodCategory }
     */
    public EJBMethodCategory createEJBMethodCategory() {
        return new EJBMethodCategory();
    }

    /**
     * Create an instance of {@link MethodTransaction }
     */
    public MethodTransaction createMethodTransaction() {
        return new MethodTransaction();
    }

    /**
     * Create an instance of {@link CMRField }
     */
    public CMRField createCMRField() {
        return new CMRField();
    }

    /**
     * Create an instance of {@link MethodElement }
     */
    public MethodElement createMethodElement() {
        return new MethodElement();
    }

    /**
     * Create an instance of {@link ContainerManagedEntity }
     */
    public ContainerManagedEntity createContainerManagedEntity() {
        return new ContainerManagedEntity();
    }

    /**
     * Create an instance of {@link MethodPermission }
     */
    public MethodPermission createMethodPermission() {
        return new MethodPermission();
    }

    /**
     * Create an instance of {@link Relationships }
     */
    public Relationships createRelationships() {
        return new Relationships();
    }

    /**
     * Create an instance of {@link QueryMethod }
     */
    public QueryMethod createQueryMethod() {
        return new QueryMethod();
    }

    /**
     * Create an instance of {@link ExcludeList }
     */
    public ExcludeList createExcludeList() {
        return new ExcludeList();
    }

    /**
     * Create an instance of {@link RoleSource }
     */
    public RoleSource createRoleSource() {
        return new RoleSource();
    }

    /**
     * Create an instance of {@link EJBJar }
     */
    public EJBJar createEJBJar() {
        return new EJBJar();
    }

    /**
     * Create an instance of {@link Entity }
     */
    public Entity createEntity() {
        return new Entity();
    }

    /**
     * Create an instance of {@link ActivationConfigProperty }
     */
    public ActivationConfigProperty createActivationConfigProperty() {
        return new ActivationConfigProperty();
    }

    /**
     * Create an instance of {@link CMPAttribute }
     */
    public CMPAttribute createCMPAttribute() {
        return new CMPAttribute();
    }

    /**
     * Create an instance of {@link EJBRelationshipRole }
     */
    public EJBRelationshipRole createEJBRelationshipRole() {
        return new EJBRelationshipRole();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CMPAttribute }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "CMPAttribute")
    public JAXBElement<CMPAttribute> createCMPAttribute(final CMPAttribute value) {
        return new JAXBElement<CMPAttribute>(_CMPAttribute_QNAME,
            CMPAttribute.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExcludeList }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "ExcludeList")
    public JAXBElement<ExcludeList> createExcludeList(final ExcludeList value) {
        return new JAXBElement<ExcludeList>(_ExcludeList_QNAME,
            ExcludeList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EJBJar }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "EJBJar")
    public JAXBElement<EJBJar> createEJBJar(final EJBJar value) {
        return new JAXBElement<EJBJar>(_EJBJar_QNAME, EJBJar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageDriven }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "MessageDriven")
    public JAXBElement<MessageDriven> createMessageDriven(final MessageDriven value) {
        return new JAXBElement<MessageDriven>(_MessageDriven_QNAME,
            MessageDriven.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link EJBMethodCategory }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "EJBMethodCategory")
    public JAXBElement<EJBMethodCategory> createEJBMethodCategory(
        final EJBMethodCategory value) {
        return new JAXBElement<EJBMethodCategory>(_EJBMethodCategory_QNAME,
            EJBMethodCategory.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnterpriseBean }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "EnterpriseBean")
    public JAXBElement<EnterpriseBean> createEnterpriseBean(final EnterpriseBean value) {
        return new JAXBElement<EnterpriseBean>(_EnterpriseBean_QNAME,
            EnterpriseBean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link ContainerManagedEntity }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "ContainerManagedEntity")
    public JAXBElement<ContainerManagedEntity> createContainerManagedEntity(
        final ContainerManagedEntity value) {
        return new JAXBElement<ContainerManagedEntity>(
            _ContainerManagedEntity_QNAME, ContainerManagedEntity.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link MessageDrivenDestination }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "MessageDrivenDestination")
    public JAXBElement<MessageDrivenDestination> createMessageDrivenDestination(
        final MessageDrivenDestination value) {
        return new JAXBElement<MessageDrivenDestination>(
            _MessageDrivenDestination_QNAME,
            MessageDrivenDestination.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EJBRelation }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "EJBRelation")
    public JAXBElement<EJBRelation> createEJBRelation(final EJBRelation value) {
        return new JAXBElement<EJBRelation>(_EJBRelation_QNAME,
            EJBRelation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link EJBRelationshipRole }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "EJBRelationshipRole")
    public JAXBElement<EJBRelationshipRole> createEJBRelationshipRole(
        final EJBRelationshipRole value) {
        return new JAXBElement<EJBRelationshipRole>(_EJBRelationshipRole_QNAME,
            EJBRelationshipRole.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RoleSource }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "RoleSource")
    public JAXBElement<RoleSource> createRoleSource(final RoleSource value) {
        return new JAXBElement<RoleSource>(_RoleSource_QNAME, RoleSource.class,
            null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link ActivationConfigProperty }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "ActivationConfigProperty")
    public JAXBElement<ActivationConfigProperty> createActivationConfigProperty(
        final ActivationConfigProperty value) {
        return new JAXBElement<ActivationConfigProperty>(
            _ActivationConfigProperty_QNAME,
            ActivationConfigProperty.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Session }{@code
     * >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "Session")
    public JAXBElement<Session> createSession(final Session value) {
        return new JAXBElement<Session>(_Session_QNAME, Session.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MethodElement }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "MethodElement")
    public JAXBElement<MethodElement> createMethodElement(final MethodElement value) {
        return new JAXBElement<MethodElement>(_MethodElement_QNAME,
            MethodElement.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link MethodTransaction }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "MethodTransaction")
    public JAXBElement<MethodTransaction> createMethodTransaction(
        final MethodTransaction value) {
        return new JAXBElement<MethodTransaction>(_MethodTransaction_QNAME,
            MethodTransaction.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link AssemblyDescriptor }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "AssemblyDescriptor")
    public JAXBElement<AssemblyDescriptor> createAssemblyDescriptor(
        final AssemblyDescriptor value) {
        return new JAXBElement<AssemblyDescriptor>(_AssemblyDescriptor_QNAME,
            AssemblyDescriptor.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Entity }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "Entity")
    public JAXBElement<Entity> createEntity(final Entity value) {
        return new JAXBElement<Entity>(_Entity_QNAME, Entity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Query }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "Query")
    public JAXBElement<Query> createQuery(final Query value) {
        return new JAXBElement<Query>(_Query_QNAME, Query.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link MethodPermission }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "MethodPermission")
    public JAXBElement<MethodPermission> createMethodPermission(
        final MethodPermission value) {
        return new JAXBElement<MethodPermission>(_MethodPermission_QNAME,
            MethodPermission.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Relationships }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "Relationships")
    public JAXBElement<Relationships> createRelationships(final Relationships value) {
        return new JAXBElement<Relationships>(_Relationships_QNAME,
            Relationships.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CMRField }{@code
     * >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "CMRField")
    public JAXBElement<CMRField> createCMRField(final CMRField value) {
        return new JAXBElement<CMRField>(_CMRField_QNAME, CMRField.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryMethod }
     * {@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "QueryMethod")
    public JAXBElement<QueryMethod> createQueryMethod(final QueryMethod value) {
        return new JAXBElement<QueryMethod>(_QueryMethod_QNAME,
            QueryMethod.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}
     * {@link ActivationConfig }{@code >}
     */
    @XmlElementDecl(namespace = "ejb.xmi", name = "ActivationConfig")
    public JAXBElement<ActivationConfig> createActivationConfig(
        final ActivationConfig value) {
        return new JAXBElement<ActivationConfig>(_ActivationConfig_QNAME,
            ActivationConfig.class, null, value);
    }

}
