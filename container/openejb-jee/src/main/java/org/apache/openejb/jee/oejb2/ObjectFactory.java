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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.oejb2;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
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

    private final static QName _GbeanTypeReferences_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "references");
    private final static QName _GbeanTypeXmlReference_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "xml-reference");
    private final static QName _GbeanTypeDependency_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "dependency");
    private final static QName _GbeanTypeAttribute_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "attribute");
    private final static QName _GbeanTypeReference_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "reference");
    private final static QName _GbeanTypeXmlAttribute_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "xml-attribute");
    private final static QName _ResourceEnvRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "resource-env-ref");
    private final static QName _ResourceRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "resource-ref");
    private final static QName _PersistenceContextRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "persistence-context-ref");
    private final static QName _EntityManagerFactoryRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "entity-manager-factory-ref");
    private final static QName _AbstractNamingEntry_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "abstract-naming-entry");
    private final static QName _Dependencies_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "dependencies");
    private final static QName _Application_QNAME = new QName("http://geronimo.apache.org/xml/ns/j2ee/application-1.2", "application");
    private final static QName _MessageDestination_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "message-destination");
    private final static QName _Module_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "module");
    private final static QName _Clustering_QNAME = new QName("http://geronimo.apache.org/xml/ns/j2ee/application-1.2", "clustering");
    private final static QName _KeyGenerator_QNAME = new QName("http://openejb.apache.org/xml/ns/pkgen-2.1", "key-generator");
    private final static QName _Environment_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "environment");
    private final static QName _Security_QNAME = new QName("http://geronimo.apache.org/xml/ns/j2ee/application-1.2", "security");
    private final static QName _ResourceAdapter_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "resource-adapter");
    private final static QName _EjbLocalRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "ejb-local-ref");
    private final static QName _ServerEnvironment_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "server-environment");
    private final static QName _Service_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "service");
    private final static QName _Gbean_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "gbean");
    private final static QName _Workmanager_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "workmanager");
    private final static QName _ServiceRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "service-ref");
    private final static QName _CmpConnectionFactory_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "cmp-connection-factory");
    private final static QName _ClientEnvironment_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "client-environment");
    private final static QName _GbeanRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "gbean-ref");
    private final static QName _OpenejbJar_QNAME = new QName("http://openejb.apache.org/xml/ns/openejb-jar-2.2", "openejb-jar");
    private final static QName _EjbRef_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "ejb-ref");
    private final static QName _WebContainer_QNAME = new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "web-container");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.openejb.jee.oej2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ActivationConfigPropertyType }
     * 
     */
    public ActivationConfigPropertyType createActivationConfigPropertyType() {
        return new ActivationConfigPropertyType();
    }

    /**
     * Create an instance of {@link AutoIncrementTableType }
     * 
     */
    public AutoIncrementTableType createAutoIncrementTableType() {
        return new AutoIncrementTableType();
    }

    /**
     * Create an instance of {@link GroupType.CmrField }
     * 
     */
    public GroupType.CmrField createGroupTypeCmrField() {
        return new GroupType.CmrField();
    }

    /**
     * Create an instance of {@link EnvironmentType }
     * 
     */
    public EnvironmentType createEnvironmentType() {
        return new EnvironmentType();
    }

    /**
     * Create an instance of {@link ReferencesType }
     * 
     */
    public ReferencesType createReferencesType() {
        return new ReferencesType();
    }

    /**
     * Create an instance of {@link QueryType.QueryMethod }
     * 
     */
    public QueryType.QueryMethod createQueryTypeQueryMethod() {
        return new QueryType.QueryMethod();
    }

    /**
     * Create an instance of {@link ClassFilterType }
     * 
     */
    public ClassFilterType createClassFilterType() {
        return new ClassFilterType();
    }

    /**
     * Create an instance of {@link EntityBeanType.Cache }
     * 
     */
    public EntityBeanType.Cache createEntityBeanTypeCache() {
        return new EntityBeanType.Cache();
    }

    /**
     * Create an instance of {@link XmlAttributeType }
     * 
     */
    public XmlAttributeType createXmlAttributeType() {
        return new XmlAttributeType();
    }

    /**
     * Create an instance of {@link EjbRelationType }
     * 
     */
    public EjbRelationType createEjbRelationType() {
        return new EjbRelationType();
    }

    /**
     * Create an instance of {@link GroupType }
     * 
     */
    public GroupType createGroupType() {
        return new GroupType();
    }

    /**
     * Create an instance of {@link SequenceTableType }
     * 
     */
    public SequenceTableType createSequenceTableType() {
        return new SequenceTableType();
    }

    /**
     * Create an instance of {@link CmpFieldGroupMappingType }
     * 
     */
    public CmpFieldGroupMappingType createCmpFieldGroupMappingType() {
        return new CmpFieldGroupMappingType();
    }

    /**
     * Create an instance of {@link EntityManagerFactoryRefType }
     * 
     */
    public EntityManagerFactoryRefType createEntityManagerFactoryRefType() {
        return new EntityManagerFactoryRefType();
    }

    /**
     * Create an instance of {@link SqlGeneratorType }
     * 
     */
    public SqlGeneratorType createSqlGeneratorType() {
        return new SqlGeneratorType();
    }

    /**
     * Create an instance of {@link EjbRelationshipRoleType.CmrField }
     * 
     */
    public EjbRelationshipRoleType.CmrField createEjbRelationshipRoleTypeCmrField() {
        return new EjbRelationshipRoleType.CmrField();
    }

    /**
     * Create an instance of {@link SessionBeanType }
     * 
     */
    public SessionBeanType createSessionBeanType() {
        return new SessionBeanType();
    }

    /**
     * Create an instance of {@link WebServiceSecurityType }
     * 
     */
    public WebServiceSecurityType createWebServiceSecurityType() {
        return new WebServiceSecurityType();
    }

    /**
     * Create an instance of {@link EjbRefType }
     * 
     */
    public EjbRefType createEjbRefType() {
        return new EjbRefType();
    }

    /**
     * Create an instance of {@link ResourceEnvRefType }
     * 
     */
    public ResourceEnvRefType createResourceEnvRefType() {
        return new ResourceEnvRefType();
    }

    /**
     * Create an instance of {@link EjbRelationshipRoleType }
     * 
     */
    public EjbRelationshipRoleType createEjbRelationshipRoleType() {
        return new EjbRelationshipRoleType();
    }

    /**
     * Create an instance of {@link GbeanRefType }
     * 
     */
    public GbeanRefType createGbeanRefType() {
        return new GbeanRefType();
    }

    /**
     * Create an instance of {@link EntityBeanType.PrefetchGroup }
     * 
     */
    public EntityBeanType.PrefetchGroup createEntityBeanTypePrefetchGroup() {
        return new EntityBeanType.PrefetchGroup();
    }

    /**
     * Create an instance of {@link GbeanLocatorType }
     * 
     */
    public GbeanLocatorType createGbeanLocatorType() {
        return new GbeanLocatorType();
    }

    /**
     * Create an instance of {@link ResourceRefType }
     * 
     */
    public ResourceRefType createResourceRefType() {
        return new ResourceRefType();
    }

    /**
     * Create an instance of {@link CustomGeneratorType }
     * 
     */
    public CustomGeneratorType createCustomGeneratorType() {
        return new CustomGeneratorType();
    }

    /**
     * Create an instance of {@link MessageDestinationType }
     * 
     */
    public MessageDestinationType createMessageDestinationType() {
        return new MessageDestinationType();
    }

    /**
     * Create an instance of {@link ApplicationType }
     * 
     */
    public ApplicationType createApplicationType() {
        return new ApplicationType();
    }

    /**
     * Create an instance of {@link EntityGroupMappingType }
     * 
     */
    public EntityGroupMappingType createEntityGroupMappingType() {
        return new EntityGroupMappingType();
    }

    /**
     * Create an instance of {@link ServiceRefType }
     * 
     */
    public ServiceRefType createServiceRefType() {
        return new ServiceRefType();
    }

    /**
     * Create an instance of {@link KeyGeneratorType }
     * 
     */
    public KeyGeneratorType createKeyGeneratorType() {
        return new KeyGeneratorType();
    }

    /**
     * Create an instance of {@link EjbLocalRefType }
     * 
     */
    public EjbLocalRefType createEjbLocalRefType() {
        return new EjbLocalRefType();
    }

    /**
     * Create an instance of {@link ServiceModuleType }
     * 
     */
    public ServiceModuleType createServiceModuleType() {
        return new ServiceModuleType();
    }


    /**
     * Create an instance of {@link ServiceCompletionType }
     * 
     */
    public ServiceCompletionType createServiceCompletionType() {
        return new ServiceCompletionType();
    }

    /**
     * Create an instance of {@link PatternType }
     * 
     */
    public PatternType createPatternType() {
        return new PatternType();
    }

    /**
     * Create an instance of {@link DependencyType }
     * 
     */
    public DependencyType createDependencyType() {
        return new DependencyType();
    }

    /**
     * Create an instance of {@link OpenejbJarType }
     * 
     */
    public OpenejbJarType createOpenejbJarType() {
        return new OpenejbJarType();
    }

    /**
     * Create an instance of {@link EntityBeanType }
     * 
     */
    public EntityBeanType createEntityBeanType() {
        return new EntityBeanType();
    }

    /**
     * Create an instance of {@link EjbRelationshipRoleType.RelationshipRoleSource }
     * 
     */
    public EjbRelationshipRoleType.RelationshipRoleSource createEjbRelationshipRoleTypeRelationshipRoleSource() {
        return new EjbRelationshipRoleType.RelationshipRoleSource();
    }

    /**
     * Create an instance of {@link ActivationConfigType }
     * 
     */
    public ActivationConfigType createActivationConfigType() {
        return new ActivationConfigType();
    }

    /**
     * Create an instance of {@link ArtifactType }
     * 
     */
    public ArtifactType createArtifactType() {
        return new ArtifactType();
    }

    /**
     * Create an instance of {@link ExtModuleType }
     * 
     */
    public ExtModuleType createExtModuleType() {
        return new ExtModuleType();
    }

    /**
     * Create an instance of {@link GbeanType }
     * 
     */
    public GbeanType createGbeanType() {
        return new GbeanType();
    }

    /**
     * Create an instance of {@link EmptyType }
     * 
     */
    public EmptyType createEmptyType() {
        return new EmptyType();
    }

    /**
     * Create an instance of {@link QueryType }
     * 
     */
    public QueryType createQueryType() {
        return new QueryType();
    }

    /**
     * Create an instance of {@link DatabaseGeneratedType }
     * 
     */
    public DatabaseGeneratedType createDatabaseGeneratedType() {
        return new DatabaseGeneratedType();
    }

    /**
     * Create an instance of {@link CmrFieldGroupMappingType }
     * 
     */
    public CmrFieldGroupMappingType createCmrFieldGroupMappingType() {
        return new CmrFieldGroupMappingType();
    }

    /**
     * Create an instance of {@link EjbRelationshipRoleType.RoleMapping.CmrFieldMapping }
     * 
     */
    public EjbRelationshipRoleType.RoleMapping.CmrFieldMapping createEjbRelationshipRoleTypeRoleMappingCmrFieldMapping() {
        return new EjbRelationshipRoleType.RoleMapping.CmrFieldMapping();
    }

    /**
     * Create an instance of {@link PortType }
     * 
     */
    public PortType createPortType() {
        return new PortType();
    }

    /**
     * Create an instance of {@link EjbRelationshipRoleType.RoleMapping }
     * 
     */
    public EjbRelationshipRoleType.RoleMapping createEjbRelationshipRoleTypeRoleMapping() {
        return new EjbRelationshipRoleType.RoleMapping();
    }

    /**
     * Create an instance of {@link EntityBeanType.CmpFieldMapping }
     * 
     */
    public EntityBeanType.CmpFieldMapping createEntityBeanTypeCmpFieldMapping() {
        return new EntityBeanType.CmpFieldMapping();
    }

    /**
     * Create an instance of {@link ModuleType }
     * 
     */
    public ModuleType createModuleType() {
        return new ModuleType();
    }

    /**
     * Create an instance of {@link MessageDrivenBeanType }
     * 
     */
    public MessageDrivenBeanType createMessageDrivenBeanType() {
        return new MessageDrivenBeanType();
    }

    /**
     * Create an instance of {@link PersistenceContextRefType }
     * 
     */
    public PersistenceContextRefType createPersistenceContextRefType() {
        return new PersistenceContextRefType();
    }

    /**
     * Create an instance of {@link PortCompletionType }
     * 
     */
    public PortCompletionType createPortCompletionType() {
        return new PortCompletionType();
    }

    /**
     * Create an instance of {@link ResourceLocatorType }
     * 
     */
    public ResourceLocatorType createResourceLocatorType() {
        return new ResourceLocatorType();
    }

    /**
     * Create an instance of {@link DependenciesType }
     * 
     */
    public DependenciesType createDependenciesType() {
        return new DependenciesType();
    }

    /**
     * Create an instance of {@link QueryType.QueryMethod.MethodParams }
     * 
     */
    public QueryType.QueryMethod.MethodParams createQueryTypeQueryMethodMethodParams() {
        return new QueryType.QueryMethod.MethodParams();
    }

    /**
     * Create an instance of {@link ReferenceType }
     * 
     */
    public ReferenceType createReferenceType() {
        return new ReferenceType();
    }

    /**
     * Create an instance of {@link PropertyType }
     * 
     */
    public PropertyType createPropertyType() {
        return new PropertyType();
    }

    /**
     * Create an instance of {@link AttributeType }
     * 
     */
    public AttributeType createAttributeType() {
        return new AttributeType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferencesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "references", scope = GbeanType.class)
    public JAXBElement<ReferencesType> createGbeanTypeReferences(ReferencesType value) {
        return new JAXBElement<ReferencesType>(_GbeanTypeReferences_QNAME, ReferencesType.class, GbeanType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XmlAttributeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "xml-reference", scope = GbeanType.class)
    public JAXBElement<XmlAttributeType> createGbeanTypeXmlReference(XmlAttributeType value) {
        return new JAXBElement<XmlAttributeType>(_GbeanTypeXmlReference_QNAME, XmlAttributeType.class, GbeanType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PatternType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "dependency", scope = GbeanType.class)
    public JAXBElement<PatternType> createGbeanTypeDependency(PatternType value) {
        return new JAXBElement<PatternType>(_GbeanTypeDependency_QNAME, PatternType.class, GbeanType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttributeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "attribute", scope = GbeanType.class)
    public JAXBElement<AttributeType> createGbeanTypeAttribute(AttributeType value) {
        return new JAXBElement<AttributeType>(_GbeanTypeAttribute_QNAME, AttributeType.class, GbeanType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "reference", scope = GbeanType.class)
    public JAXBElement<ReferenceType> createGbeanTypeReference(ReferenceType value) {
        return new JAXBElement<ReferenceType>(_GbeanTypeReference_QNAME, ReferenceType.class, GbeanType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XmlAttributeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "xml-attribute", scope = GbeanType.class)
    public JAXBElement<XmlAttributeType> createGbeanTypeXmlAttribute(XmlAttributeType value) {
        return new JAXBElement<XmlAttributeType>(_GbeanTypeXmlAttribute_QNAME, XmlAttributeType.class, GbeanType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceEnvRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "resource-env-ref")
    public JAXBElement<ResourceEnvRefType> createResourceEnvRef(ResourceEnvRefType value) {
        return new JAXBElement<ResourceEnvRefType>(_ResourceEnvRef_QNAME, ResourceEnvRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "resource-ref")
    public JAXBElement<ResourceRefType> createResourceRef(ResourceRefType value) {
        return new JAXBElement<ResourceRefType>(_ResourceRef_QNAME, ResourceRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PersistenceContextRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "persistence-context-ref", substitutionHeadNamespace = "http://geronimo.apache.org/xml/ns/naming-1.2", substitutionHeadName = "abstract-naming-entry")
    public JAXBElement<PersistenceContextRefType> createPersistenceContextRef(PersistenceContextRefType value) {
        return new JAXBElement<PersistenceContextRefType>(_PersistenceContextRef_QNAME, PersistenceContextRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EntityManagerFactoryRefType }{@code >}}
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

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DependenciesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "dependencies")
    public JAXBElement<DependenciesType> createDependencies(DependenciesType value) {
        return new JAXBElement<DependenciesType>(_Dependencies_QNAME, DependenciesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplicationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2", name = "application")
    public JAXBElement<ApplicationType> createApplication(ApplicationType value) {
        return new JAXBElement<ApplicationType>(_Application_QNAME, ApplicationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageDestinationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "message-destination")
    public JAXBElement<MessageDestinationType> createMessageDestination(MessageDestinationType value) {
        return new JAXBElement<MessageDestinationType>(_MessageDestination_QNAME, MessageDestinationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceModuleType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "module")
    public JAXBElement<ServiceModuleType> createModule(ServiceModuleType value) {
        return new JAXBElement<ServiceModuleType>(_Module_QNAME, ServiceModuleType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractClusteringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2", name = "clustering")
    public JAXBElement<AbstractClusteringType> createClustering(AbstractClusteringType value) {
        return new JAXBElement<AbstractClusteringType>(_Clustering_QNAME, AbstractClusteringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeyGeneratorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://openejb.apache.org/xml/ns/pkgen-2.1", name = "key-generator")
    public JAXBElement<KeyGeneratorType> createKeyGenerator(KeyGeneratorType value) {
        return new JAXBElement<KeyGeneratorType>(_KeyGenerator_QNAME, KeyGeneratorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvironmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "environment")
    public JAXBElement<EnvironmentType> createEnvironment(EnvironmentType value) {
        return new JAXBElement<EnvironmentType>(_Environment_QNAME, EnvironmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractSecurityType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2", name = "security")
    public JAXBElement<AbstractSecurityType> createSecurity(AbstractSecurityType value) {
        return new JAXBElement<AbstractSecurityType>(_Security_QNAME, AbstractSecurityType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceLocatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "resource-adapter")
    public JAXBElement<ResourceLocatorType> createResourceAdapter(ResourceLocatorType value) {
        return new JAXBElement<ResourceLocatorType>(_ResourceAdapter_QNAME, ResourceLocatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbLocalRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "ejb-local-ref")
    public JAXBElement<EjbLocalRefType> createEjbLocalRef(EjbLocalRefType value) {
        return new JAXBElement<EjbLocalRefType>(_EjbLocalRef_QNAME, EjbLocalRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvironmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "server-environment")
    public JAXBElement<EnvironmentType> createServerEnvironment(EnvironmentType value) {
        return new JAXBElement<EnvironmentType>(_ServerEnvironment_QNAME, EnvironmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractServiceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "service")
    public JAXBElement<AbstractServiceType> createService(AbstractServiceType value) {
        return new JAXBElement<AbstractServiceType>(_Service_QNAME, AbstractServiceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GbeanType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "gbean", substitutionHeadNamespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", substitutionHeadName = "service")
    public JAXBElement<GbeanType> createGbean(GbeanType value) {
        return new JAXBElement<GbeanType>(_Gbean_QNAME, GbeanType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GbeanLocatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "workmanager")
    public JAXBElement<GbeanLocatorType> createWorkmanager(GbeanLocatorType value) {
        return new JAXBElement<GbeanLocatorType>(_Workmanager_QNAME, GbeanLocatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "service-ref")
    public JAXBElement<ServiceRefType> createServiceRef(ServiceRefType value) {
        return new JAXBElement<ServiceRefType>(_ServiceRef_QNAME, ServiceRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceLocatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "cmp-connection-factory")
    public JAXBElement<ResourceLocatorType> createCmpConnectionFactory(ResourceLocatorType value) {
        return new JAXBElement<ResourceLocatorType>(_CmpConnectionFactory_QNAME, ResourceLocatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvironmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", name = "client-environment")
    public JAXBElement<EnvironmentType> createClientEnvironment(EnvironmentType value) {
        return new JAXBElement<EnvironmentType>(_ClientEnvironment_QNAME, EnvironmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GbeanRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "gbean-ref", substitutionHeadNamespace = "http://geronimo.apache.org/xml/ns/naming-1.2", substitutionHeadName = "abstract-naming-entry")
    public JAXBElement<GbeanRefType> createGbeanRef(GbeanRefType value) {
        return new JAXBElement<GbeanRefType>(_GbeanRef_QNAME, GbeanRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenejbJarType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2", name = "openejb-jar")
    public JAXBElement<OpenejbJarType> createOpenejbJar(OpenejbJarType value) {
        return new JAXBElement<OpenejbJarType>(_OpenejbJar_QNAME, OpenejbJarType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "ejb-ref")
    public JAXBElement<EjbRefType> createEjbRef(EjbRefType value) {
        return new JAXBElement<EjbRefType>(_EjbRef_QNAME, EjbRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GbeanLocatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", name = "web-container")
    public JAXBElement<GbeanLocatorType> createWebContainer(GbeanLocatorType value) {
        return new JAXBElement<GbeanLocatorType>(_WebContainer_QNAME, GbeanLocatorType.class, null, value);
    }

    private final static QName _EjbJar_QNAME = new QName("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", "ejb-jar");
    @XmlElementDecl(namespace = "http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", name = "ejb-jar")
    public JAXBElement<GeronimoEjbJarType> createEjbJar(GeronimoEjbJarType value) {
        return new JAXBElement<GeronimoEjbJarType>(_EjbJar_QNAME, GeronimoEjbJarType.class, null, value);
    }

}
