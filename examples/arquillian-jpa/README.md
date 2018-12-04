index-group=Unrevised
type=page
status=published
title=Arquillian Persistence Extension
~~~~~~

A sample showing how to use TomEE, Arquillian and its Persistence Extension.

Note that it doesn't work with embedded containers (openejb, tomee-embedded)
if you don't use workarounds like https://github.com/rmannibucau/persistence-with-openejb-and-arquillian
(see src/test/resources folder).

# Running (output)

    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.arquillian.test.persistence.PersistenceTest
    oct. 01, 2014 6:30:23 PM org.apache.openejb.arquillian.common.Setup findHome
    INFOS: Unable to find home in: /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/apache-tomee-remote
    oct. 01, 2014 6:30:23 PM org.apache.openejb.arquillian.common.MavenCache getArtifact
    INFOS: Downloading org.apache.openejb:apache-tomee:7.0.0-SNAPSHOT:zip:webprofile please wait...
    oct. 01, 2014 6:30:23 PM org.apache.openejb.arquillian.common.Zips unzip
    INFOS: Extracting '/home/rmannibucau/.m2/repository/org/apache/openejb/apache-tomee/7.0.0-SNAPSHOT/apache-tomee-7.0.0-SNAPSHOT-webprofile.zip' to '/home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/apache-tomee-remote'
    oct. 01, 2014 6:30:24 PM org.apache.tomee.arquillian.remote.RemoteTomEEContainer configure
    INFOS: Downloaded container to: /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/apache-tomee-remote/apache-tomee-webprofile-7.0.0-SNAPSHOT
    INFOS - Server version: Apache Tomcat/8.0.14
    INFOS - Server built:   Sep 24 2014 09:01:51
    INFOS - Server number:  8.0.14.0
    INFOS - OS Name:        Linux
    INFOS - OS Version:     3.13.0-35-generic
    INFOS - Architecture:   amd64
    INFOS - JVM Version:    1.7.0_67-b01
    INFOS - JVM Vendor:     Oracle Corporation
    INFOS - The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: /usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
    INFOS - Initializing ProtocolHandler ["http-nio-52256"]
    INFOS - Using a shared selector for servlet write/read
    INFOS - Initializing ProtocolHandler ["ajp-nio-40071"]
    INFOS - Using a shared selector for servlet write/read
    INFOS - Using 'openejb.jdbc.datasource-creator=org.apache.tomee.jdbc.TomEEDataSourceCreator'
    INFOS - ********************************************************************************
    INFOS - OpenEJB http://tomee.apache.org/
    INFOS - Startup: Wed Oct 01 18:30:26 CEST 2014
    INFOS - Copyright 1999-2013 (C) Apache OpenEJB Project, All Rights Reserved.
    INFOS - Version: 7.0.0-SNAPSHOT
    INFOS - Build date: 20141001
    INFOS - Build time: 04:53
    INFOS - ********************************************************************************
    INFOS - openejb.home = /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/apache-tomee-remote/apache-tomee-webprofile-7.0.0-SNAPSHOT
    INFOS - openejb.base = /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/apache-tomee-remote/apache-tomee-webprofile-7.0.0-SNAPSHOT
    INFOS - Created new singletonService org.apache.openejb.cdi.ThreadSingletonServiceImpl@13158bbd
    INFOS - Succeeded in installing singleton service
    INFOS - openejb configuration file is '/home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/apache-tomee-remote/apache-tomee-webprofile-7.0.0-SNAPSHOT/conf/tomee.xml'
    INFOS - Configuring Service(id=Tomcat Security Service, type=SecurityService, provider-id=Tomcat Security Service)
    INFOS - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFOS - Configuring Service(id=demoDataSource, type=Resource, provider-id=Default JDBC Database)
    INFOS - Using 'openejb.system.apps=true'
    INFOS - Configuring enterprise application: openejb
    INFOS - Using openejb.deploymentId.format '{ejbName}'
    INFOS - Auto-deploying ejb openejb/Deployer: EjbDeployment(deployment-id=openejb/Deployer)
    INFOS - Auto-deploying ejb openejb/ConfigurationInfo: EjbDeployment(deployment-id=openejb/ConfigurationInfo)
    INFOS - Auto-deploying ejb MEJB: EjbDeployment(deployment-id=MEJB)
    INFOS - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFOS - Auto-creating a container for bean openejb/Deployer: Container(type=STATELESS, id=Default Stateless Container)
    INFOS - Enterprise application "openejb" loaded.
    INFOS - Creating TransactionManager(id=Default Transaction Manager)
    INFOS - Creating SecurityService(id=Tomcat Security Service)
    INFOS - Creating Resource(id=demoDataSource)
    INFOS - Disabling testOnBorrow since no validation query is provided
    INFOS - Creating Container(id=Default Stateless Container)
    INFOS - Not creating another application classloader for openejb
    INFOS - Assembling app: openejb
    INFOS - Using 'openejb.jndiname.format={deploymentId}{interfaceType.openejbLegacyName}'
    INFOS - Jndi(name=openejb/DeployerBusinessRemote) --> Ejb(deployment-id=openejb/Deployer)
    INFOS - Jndi(name=global/openejb/openejb/Deployer!org.apache.openejb.assembler.Deployer) --> Ejb(deployment-id=openejb/Deployer)
    INFOS - Jndi(name=global/openejb/openejb/Deployer) --> Ejb(deployment-id=openejb/Deployer)
    INFOS - Jndi(name=openejb/ConfigurationInfoBusinessRemote) --> Ejb(deployment-id=openejb/ConfigurationInfo)
    INFOS - Jndi(name=global/openejb/openejb/ConfigurationInfo!org.apache.openejb.assembler.classic.cmd.ConfigurationInfo) --> Ejb(deployment-id=openejb/ConfigurationInfo)
    INFOS - Jndi(name=global/openejb/openejb/ConfigurationInfo) --> Ejb(deployment-id=openejb/ConfigurationInfo)
    INFOS - Jndi(name=MEJB) --> Ejb(deployment-id=MEJB)
    INFOS - Jndi(name=global/openejb/MEJB!javax.management.j2ee.ManagementHome) --> Ejb(deployment-id=MEJB)
    INFOS - Jndi(name=global/openejb/MEJB) --> Ejb(deployment-id=MEJB)
    INFOS - Created Ejb(deployment-id=openejb/Deployer, ejb-name=openejb/Deployer, container=Default Stateless Container)
    INFOS - Created Ejb(deployment-id=MEJB, ejb-name=MEJB, container=Default Stateless Container)
    INFOS - Created Ejb(deployment-id=openejb/ConfigurationInfo, ejb-name=openejb/ConfigurationInfo, container=Default Stateless Container)
    INFOS - Started Ejb(deployment-id=openejb/Deployer, ejb-name=openejb/Deployer, container=Default Stateless Container)
    INFOS - Started Ejb(deployment-id=MEJB, ejb-name=MEJB, container=Default Stateless Container)
    INFOS - Started Ejb(deployment-id=openejb/ConfigurationInfo, ejb-name=openejb/ConfigurationInfo, container=Default Stateless Container)
    INFOS - Deployed MBean(openejb.user.mbeans:application=openejb,group=org.apache.openejb.assembler.monitoring,name=JMXDeployer)
    INFOS - Deployed Application(path=openejb)
    INFOS - Creating ServerService(id=cxf-rs)
    INFOS -   ** Bound Services **
    INFOS -   NAME                 IP              PORT  
    INFOS - -------
    INFOS - Ready!
    INFOS - Initialization processed in 2589 ms
    INFOS - Importing a Tomcat Resource with id 'UserDatabase' of type 'org.apache.catalina.UserDatabase'.
    INFOS - Creating Resource(id=UserDatabase)
    INFOS - Démarrage du service Catalina
    INFOS - Starting Servlet Engine: Apache Tomcat (TomEE)/8.0.14 (7.0.0-SNAPSHOT)
    INFOS - Starting ProtocolHandler ["http-nio-52256"]
    INFOS - Starting ProtocolHandler ["ajp-nio-40071"]
    INFOS - Server startup in 140 ms
    oct. 01, 2014 6:30:30 PM org.apache.openejb.client.EventLogger log
    INFOS: RemoteInitialContextCreated{providerUri=http://localhost:52256/tomee/ejb}
    INFOS - Extracting jar: /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0/UserPersistenceTest.war
    INFOS - Extracted path: /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0/UserPersistenceTest
    INFOS - using default host: localhost
    INFOS - ------------------------- localhost -> /UserPersistenceTest
    INFOS - Using 'openejb.session.manager=org.apache.tomee.catalina.session.QuickSessionManager'
    INFOS - Configuring enterprise application: /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0/UserPersistenceTest
    INFOS - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFOS - Auto-creating a container for bean UserPersistenceTest_org.superbiz.arquillian.test.persistence.PersistenceTest: Container(type=MANAGED, id=Default Managed Container)
    INFOS - Creating Container(id=Default Managed Container)
    INFOS - Using directory /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/apache-tomee-remote/apache-tomee-webprofile-7.0.0-SNAPSHOT/temp for stateful session passivation
    INFOS - Configuring PersistenceUnit(name=demoApplicationPU)
    INFOS - Auto-creating a Resource with id 'demoDataSourceNonJta' of type 'DataSource for 'demoApplicationPU'.
    INFOS - Configuring Service(id=demoDataSourceNonJta, type=Resource, provider-id=demoDataSource)
    INFOS - Creating Resource(id=demoDataSourceNonJta)
    INFOS - Disabling testOnBorrow since no validation query is provided
    INFOS - Adjusting PersistenceUnit demoApplicationPU <non-jta-data-source> to Resource ID 'demoDataSourceNonJta' from 'null'
    INFOS - Enterprise application "/home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0/UserPersistenceTest" loaded.
    INFOS - Assembling app: /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0/UserPersistenceTest
    INFOS - OpenJPA dynamically loaded a validation provider.
    INFOS - Starting OpenJPA 2.4.0-nonfinal-1598334
    INFOS - Using dictionary class "org.apache.openjpa.jdbc.sql.HSQLDictionary" (HSQL Database Engine 2.3.2 ,HSQL Database Engine Driver 2.3.2).
    INFOS - Connected to HSQL Database Engine version 2.2 using JDBC driver HSQL Database Engine Driver version 2.3.2. 
    INFOS - SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES --> 0ms
    INFOS - CREATE TABLE User (id BIGINT NOT NULL, name VARCHAR(255), PRIMARY KEY (id)) --> 0ms
    INFOS - PersistenceUnit(name=demoApplicationPU, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 1075ms
    INFOS - Existing thread singleton service in SystemInstance(): org.apache.openejb.cdi.ThreadSingletonServiceImpl@13158bbd
    INFOS - OpenWebBeans Container is starting...
    INFOS - Adding OpenWebBeansPlugin : [CdiPlugin]
    INFOS - All injection points were validated successfully.
    INFOS - OpenWebBeans Container has started, it took 224 ms.
    INFOS - Deployed Application(path=/home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0/UserPersistenceTest)
    INFOS - At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
    AVERTISSEMENT - Potential problem found: The configured data type factory 'class org.dbunit.dataset.datatype.DefaultDataTypeFactory' might cause problems with the current database 'HSQL Database Engine' (e.g. some datatypes may not be supported properly). In rare cases you might see this message because the list of supported database products is incomplete (list=[derby]). If so please request a java-class update via the forums.If you are using your own IDataTypeFactory extending DefaultDataTypeFactory, ensure that you override getValidDbProducts() to specify the supported database products.
    INFOS - insert into USER (ID, NAME) values (1, TomEE) --> 1ms
    INFOS - insert into USER (ID, NAME) values (1, 2)TomEE,Old) --> 0ms
    INFOS - SELECT COUNT(t0.id) FROM User t0 --> 0ms
    INFOS - SELECT t0.name FROM User t0 WHERE t0.id = 2 --> 0ms
    INFOS - UPDATE User SET name = OpenEJB WHERE id = 2 --> 1ms
    INFOS - select ID, NAME from USER order by ID --> 0ms
    INFOS - select ID, NAME from USER order by ID --> 0ms
    INFOS - select ID, NAME from USER order by ID --> 0ms
    INFOS - select ID, NAME from USER order by ID --> 0ms
    INFOS - delete from USER --> 0ms
    oct. 01, 2014 6:30:34 PM org.apache.openejb.client.EventLogger log
    INFOS: RemoteInitialContextCreated{providerUri=http://localhost:52256/tomee/ejb}
    INFOS - Undeploying app: /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0/UserPersistenceTest
    oct. 01, 2014 6:30:34 PM org.apache.openejb.arquillian.common.TomEEContainer undeploy
    INFOS: cleaning /home/rmannibucau/dev/Apache/tomee-trunk/examples/arquillian-jpa/target/arquillian-test-working-dir/0
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 11.242 sec
    INFOS - A valid shutdown command was received via the shutdown port. Stopping the Server instance.
    INFOS - Pausing ProtocolHandler ["http-nio-52256"]
    INFOS - Pausing ProtocolHandler ["ajp-nio-40071"]
    INFOS - Arrêt du service Catalina
    INFOS - Stopping ProtocolHandler ["http-nio-52256"]
    INFOS - Stopping ProtocolHandler ["ajp-nio-40071"]
    INFOS - Stopping server services
    INFOS - Undeploying app: openejb
    INFOS - Closing DataSource: demoDataSource
    INFOS - Closing DataSource: demoDataSourceNonJta
    INFOS - Destroying ProtocolHandler ["http-nio-52256"]
    INFOS - Destroying ProtocolHandler ["ajp-nio-40071"]
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
