
package org.apache.openejb.jee.wls;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.apache.openejb.jee.wls package. 
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

    private final static QName _FieldGroupCmpField_QNAME = new QName("http://www.bea.com/ns/weblogic/90", "cmp-field");
    private final static QName _FieldGroupCmrField_QNAME = new QName("http://www.bea.com/ns/weblogic/90", "cmr-field");
    private final static QName _SecurityRoleAssignmentPrincipalName_QNAME = new QName("http://www.bea.com/ns/weblogic/90", "principal-name");
    private final static QName _WeblogicEjbJar_QNAME = new QName("http://www.bea.com/ns/weblogic/90", "weblogic-ejb-jar");
    private final static QName _WeblogicRdbmsRelationWeblogicRelationshipRole_QNAME = new QName("http://www.bea.com/ns/weblogic/90", "weblogic-relationship-role");
    private final static QName _WeblogicRdbmsRelationTableName_QNAME = new QName("http://www.bea.com/ns/weblogic/90", "table-name");
    private final static QName _WeblogicRdbmsRelationRelationName_QNAME = new QName("http://www.bea.com/ns/weblogic/90", "relation-name");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.openejb.jee.wls
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Description }
     * 
     */
    public Description createDescription() {
        return new Description();
    }

    /**
     * Create an instance of {@link StatefulSessionClustering }
     * 
     */
    public StatefulSessionClustering createStatefulSessionClustering() {
        return new StatefulSessionClustering();
    }

    /**
     * Create an instance of {@link InvalidationTarget }
     * 
     */
    public InvalidationTarget createInvalidationTarget() {
        return new InvalidationTarget();
    }

    /**
     * Create an instance of {@link ResponseTimeRequestClass }
     * 
     */
    public ResponseTimeRequestClass createResponseTimeRequestClass() {
        return new ResponseTimeRequestClass();
    }

    /**
     * Create an instance of {@link ResourceDescription }
     * 
     */
    public ResourceDescription createResourceDescription() {
        return new ResourceDescription();
    }

    /**
     * Create an instance of {@link RelationshipRoleMap }
     * 
     */
    public RelationshipRoleMap createRelationshipRoleMap() {
        return new RelationshipRoleMap();
    }

    /**
     * Create an instance of {@link WorkManagerShutdownTrigger }
     * 
     */
    public WorkManagerShutdownTrigger createWorkManagerShutdownTrigger() {
        return new WorkManagerShutdownTrigger();
    }

    /**
     * Create an instance of {@link EjbQlQuery }
     * 
     */
    public EjbQlQuery createEjbQlQuery() {
        return new EjbQlQuery();
    }

    /**
     * Create an instance of {@link ResourceEnvDescription }
     * 
     */
    public ResourceEnvDescription createResourceEnvDescription() {
        return new ResourceEnvDescription();
    }

    /**
     * Create an instance of {@link CachingName }
     * 
     */
    public CachingName createCachingName() {
        return new CachingName();
    }

    /**
     * Create an instance of {@link WeblogicEnterpriseBean }
     * 
     */
    public WeblogicEnterpriseBean createWeblogicEnterpriseBean() {
        return new WeblogicEnterpriseBean();
    }

    /**
     * Create an instance of {@link TransportRequirements }
     * 
     */
    public TransportRequirements createTransportRequirements() {
        return new TransportRequirements();
    }

    /**
     * Create an instance of {@link IiopSecurityDescriptor }
     * 
     */
    public IiopSecurityDescriptor createIiopSecurityDescriptor() {
        return new IiopSecurityDescriptor();
    }

    /**
     * Create an instance of {@link EjbReferenceDescription }
     * 
     */
    public EjbReferenceDescription createEjbReferenceDescription() {
        return new EjbReferenceDescription();
    }

    /**
     * Create an instance of {@link UnknownPrimaryKeyField }
     * 
     */
    public UnknownPrimaryKeyField createUnknownPrimaryKeyField() {
        return new UnknownPrimaryKeyField();
    }

    /**
     * Create an instance of {@link CachingElement }
     * 
     */
    public CachingElement createCachingElement() {
        return new CachingElement();
    }

    /**
     * Create an instance of {@link MessageDestinationDescriptor }
     * 
     */
    public MessageDestinationDescriptor createMessageDestinationDescriptor() {
        return new MessageDestinationDescriptor();
    }

    /**
     * Create an instance of {@link StatefulSessionCache }
     * 
     */
    public StatefulSessionCache createStatefulSessionCache() {
        return new StatefulSessionCache();
    }

    /**
     * Create an instance of {@link MessageDrivenDescriptor }
     * 
     */
    public MessageDrivenDescriptor createMessageDrivenDescriptor() {
        return new MessageDrivenDescriptor();
    }

    /**
     * Create an instance of {@link FairShareRequestClass }
     * 
     */
    public FairShareRequestClass createFairShareRequestClass() {
        return new FairShareRequestClass();
    }

    /**
     * Create an instance of {@link Compatibility }
     * 
     */
    public Compatibility createCompatibility() {
        return new Compatibility();
    }

    /**
     * Create an instance of {@link MinThreadsConstraint }
     * 
     */
    public MinThreadsConstraint createMinThreadsConstraint() {
        return new MinThreadsConstraint();
    }

    /**
     * Create an instance of {@link TimerDescriptor }
     * 
     */
    public TimerDescriptor createTimerDescriptor() {
        return new TimerDescriptor();
    }

    /**
     * Create an instance of {@link StatelessClustering }
     * 
     */
    public StatelessClustering createStatelessClustering() {
        return new StatelessClustering();
    }

    /**
     * Create an instance of {@link StatelessSessionDescriptor }
     * 
     */
    public StatelessSessionDescriptor createStatelessSessionDescriptor() {
        return new StatelessSessionDescriptor();
    }

    /**
     * Create an instance of {@link QueryMethod }
     * 
     */
    public QueryMethod createQueryMethod() {
        return new QueryMethod();
    }

    /**
     * Create an instance of {@link PortInfo }
     * 
     */
    public PortInfo createPortInfo() {
        return new PortInfo();
    }

    /**
     * Create an instance of {@link ApplicationAdminModeTrigger }
     * 
     */
    public ApplicationAdminModeTrigger createApplicationAdminModeTrigger() {
        return new ApplicationAdminModeTrigger();
    }

    /**
     * Create an instance of {@link FieldGroup }
     * 
     */
    public FieldGroup createFieldGroup() {
        return new FieldGroup();
    }

    /**
     * Create an instance of {@link WeblogicRdbmsBean }
     * 
     */
    public WeblogicRdbmsBean createWeblogicRdbmsBean() {
        return new WeblogicRdbmsBean();
    }

    /**
     * Create an instance of {@link DistributedDestinationConnection }
     * 
     */
    public DistributedDestinationConnection createDistributedDestinationConnection() {
        return new DistributedDestinationConnection();
    }

    /**
     * Create an instance of {@link TransactionDescriptor }
     * 
     */
    public TransactionDescriptor createTransactionDescriptor() {
        return new TransactionDescriptor();
    }

    /**
     * Create an instance of {@link WeblogicQuery }
     * 
     */
    public WeblogicQuery createWeblogicQuery() {
        return new WeblogicQuery();
    }

    /**
     * Create an instance of {@link WeblogicCompatibility }
     * 
     */
    public WeblogicCompatibility createWeblogicCompatibility() {
        return new WeblogicCompatibility();
    }

    /**
     * Create an instance of {@link PersistenceUse }
     * 
     */
    public PersistenceUse createPersistenceUse() {
        return new PersistenceUse();
    }

    /**
     * Create an instance of {@link WeblogicRdbmsRelation }
     * 
     */
    public WeblogicRdbmsRelation createWeblogicRdbmsRelation() {
        return new WeblogicRdbmsRelation();
    }

    /**
     * Create an instance of {@link ContextCase }
     * 
     */
    public ContextCase createContextCase() {
        return new ContextCase();
    }

    /**
     * Create an instance of {@link Capacity }
     * 
     */
    public Capacity createCapacity() {
        return new Capacity();
    }

    /**
     * Create an instance of {@link Logging }
     * 
     */
    public Logging createLogging() {
        return new Logging();
    }

    /**
     * Create an instance of {@link AutomaticKeyGeneration }
     * 
     */
    public AutomaticKeyGeneration createAutomaticKeyGeneration() {
        return new AutomaticKeyGeneration();
    }

    /**
     * Create an instance of {@link WeblogicEjbJar }
     * 
     */
    public WeblogicEjbJar createWeblogicEjbJar() {
        return new WeblogicEjbJar();
    }

    /**
     * Create an instance of {@link SecurityRoleAssignment }
     * 
     */
    public SecurityRoleAssignment createSecurityRoleAssignment() {
        return new SecurityRoleAssignment();
    }

    /**
     * Create an instance of {@link Persistence }
     * 
     */
    public Persistence createPersistence() {
        return new Persistence();
    }

    /**
     * Create an instance of {@link ConnectionPoolParams }
     * 
     */
    public ConnectionPoolParams createConnectionPoolParams() {
        return new ConnectionPoolParams();
    }

    /**
     * Create an instance of {@link RunAsRoleAssignment }
     * 
     */
    public RunAsRoleAssignment createRunAsRoleAssignment() {
        return new RunAsRoleAssignment();
    }

    /**
     * Create an instance of {@link Method }
     * 
     */
    public Method createMethod() {
        return new Method();
    }

    /**
     * Create an instance of {@link Pool }
     * 
     */
    public Pool createPool() {
        return new Pool();
    }

    /**
     * Create an instance of {@link EntityCacheRef }
     * 
     */
    public EntityCacheRef createEntityCacheRef() {
        return new EntityCacheRef();
    }

    /**
     * Create an instance of {@link WeblogicRelationshipRole }
     * 
     */
    public WeblogicRelationshipRole createWeblogicRelationshipRole() {
        return new WeblogicRelationshipRole();
    }

    /**
     * Create an instance of {@link MaxThreadsConstraint }
     * 
     */
    public MaxThreadsConstraint createMaxThreadsConstraint() {
        return new MaxThreadsConstraint();
    }

    /**
     * Create an instance of {@link FieldMap }
     * 
     */
    public FieldMap createFieldMap() {
        return new FieldMap();
    }

    /**
     * Create an instance of {@link TableMap }
     * 
     */
    public TableMap createTableMap() {
        return new TableMap();
    }

    /**
     * Create an instance of {@link PropertyNamevalue }
     * 
     */
    public PropertyNamevalue createPropertyNamevalue() {
        return new PropertyNamevalue();
    }

    /**
     * Create an instance of {@link Table }
     * 
     */
    public Table createTable() {
        return new Table();
    }

    /**
     * Create an instance of {@link WorkManager }
     * 
     */
    public WorkManager createWorkManager() {
        return new WorkManager();
    }

    /**
     * Create an instance of {@link RetryMethodsOnRollback }
     * 
     */
    public RetryMethodsOnRollback createRetryMethodsOnRollback() {
        return new RetryMethodsOnRollback();
    }

    /**
     * Create an instance of {@link IdempotentMethods }
     * 
     */
    public IdempotentMethods createIdempotentMethods() {
        return new IdempotentMethods();
    }

    /**
     * Create an instance of {@link EntityDescriptor }
     * 
     */
    public EntityDescriptor createEntityDescriptor() {
        return new EntityDescriptor();
    }

    /**
     * Create an instance of {@link MethodParams }
     * 
     */
    public MethodParams createMethodParams() {
        return new MethodParams();
    }

    /**
     * Create an instance of {@link ServiceReferenceDescription }
     * 
     */
    public ServiceReferenceDescription createServiceReferenceDescription() {
        return new ServiceReferenceDescription();
    }

    /**
     * Create an instance of {@link Empty }
     * 
     */
    public Empty createEmpty() {
        return new Empty();
    }

    /**
     * Create an instance of {@link EntityClustering }
     * 
     */
    public EntityClustering createEntityClustering() {
        return new EntityClustering();
    }

    /**
     * Create an instance of {@link SqlShape }
     * 
     */
    public SqlShape createSqlShape() {
        return new SqlShape();
    }

    /**
     * Create an instance of {@link EntityCache }
     * 
     */
    public EntityCache createEntityCache() {
        return new EntityCache();
    }

    /**
     * Create an instance of {@link SqlQuery }
     * 
     */
    public SqlQuery createSqlQuery() {
        return new SqlQuery();
    }

    /**
     * Create an instance of {@link StatefulSessionDescriptor }
     * 
     */
    public StatefulSessionDescriptor createStatefulSessionDescriptor() {
        return new StatefulSessionDescriptor();
    }

    /**
     * Create an instance of {@link RelationshipCaching }
     * 
     */
    public RelationshipCaching createRelationshipCaching() {
        return new RelationshipCaching();
    }

    /**
     * Create an instance of {@link WeblogicRdbmsJar }
     * 
     */
    public WeblogicRdbmsJar createWeblogicRdbmsJar() {
        return new WeblogicRdbmsJar();
    }

    /**
     * Create an instance of {@link ContextRequestClass }
     * 
     */
    public ContextRequestClass createContextRequestClass() {
        return new ContextRequestClass();
    }

    /**
     * Create an instance of {@link SecurityPermission }
     * 
     */
    public SecurityPermission createSecurityPermission() {
        return new SecurityPermission();
    }

    /**
     * Create an instance of {@link DatabaseSpecificSql }
     * 
     */
    public DatabaseSpecificSql createDatabaseSpecificSql() {
        return new DatabaseSpecificSql();
    }

    /**
     * Create an instance of {@link SecurityPlugin }
     * 
     */
    public SecurityPlugin createSecurityPlugin() {
        return new SecurityPlugin();
    }

    /**
     * Create an instance of {@link TransactionIsolation }
     * 
     */
    public TransactionIsolation createTransactionIsolation() {
        return new TransactionIsolation();
    }

    /**
     * Create an instance of {@link ColumnMap }
     * 
     */
    public ColumnMap createColumnMap() {
        return new ColumnMap();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.bea.com/ns/weblogic/90", name = "cmp-field", scope = FieldGroup.class)
    public JAXBElement<String> createFieldGroupCmpField(String value) {
        return new JAXBElement<String>(_FieldGroupCmpField_QNAME, String.class, FieldGroup.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.bea.com/ns/weblogic/90", name = "cmr-field", scope = FieldGroup.class)
    public JAXBElement<String> createFieldGroupCmrField(String value) {
        return new JAXBElement<String>(_FieldGroupCmrField_QNAME, String.class, FieldGroup.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.bea.com/ns/weblogic/90", name = "principal-name", scope = SecurityRoleAssignment.class)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createSecurityRoleAssignmentPrincipalName(String value) {
        return new JAXBElement<String>(_SecurityRoleAssignmentPrincipalName_QNAME, String.class, SecurityRoleAssignment.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WeblogicEjbJar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.bea.com/ns/weblogic/90", name = "weblogic-ejb-jar")
    public JAXBElement<WeblogicEjbJar> createWeblogicEjbJar(WeblogicEjbJar value) {
        return new JAXBElement<WeblogicEjbJar>(_WeblogicEjbJar_QNAME, WeblogicEjbJar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WeblogicRelationshipRole }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.bea.com/ns/weblogic/90", name = "weblogic-relationship-role", scope = WeblogicRdbmsRelation.class)
    public JAXBElement<WeblogicRelationshipRole> createWeblogicRdbmsRelationWeblogicRelationshipRole(WeblogicRelationshipRole value) {
        return new JAXBElement<WeblogicRelationshipRole>(_WeblogicRdbmsRelationWeblogicRelationshipRole_QNAME, WeblogicRelationshipRole.class, WeblogicRdbmsRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.bea.com/ns/weblogic/90", name = "table-name", scope = WeblogicRdbmsRelation.class)
    public JAXBElement<String> createWeblogicRdbmsRelationTableName(String value) {
        return new JAXBElement<String>(_WeblogicRdbmsRelationTableName_QNAME, String.class, WeblogicRdbmsRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.bea.com/ns/weblogic/90", name = "relation-name", scope = WeblogicRdbmsRelation.class)
    public JAXBElement<String> createWeblogicRdbmsRelationRelationName(String value) {
        return new JAXBElement<String>(_WeblogicRdbmsRelationRelationName_QNAME, String.class, WeblogicRdbmsRelation.class, value);
    }

}
