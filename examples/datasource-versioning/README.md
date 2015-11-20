Title: DataSource Versioning

This example shows you how to use versioned DataSources of the same provider using the classpath attribute.

# Configuration

The DataSource configuration can be made several ways and here we layout two common methods in the form of unit tests.
Before we start, if you take a peek in the project pom.xml and look for the maven-dependency-plugin usage you will see that we pull in
two completely different driver files for this example.

# AlternateDataSourceTest.java
This test utilizes the Arquillian testing framework. See [here](http://tomee.apache.org/arquillian-available-adapters.html) for more details.

The example uses src/test/resources/arquillian.xml and src/test/conf/tomee.xml to define the DataSources.
Note the differing driver version paths, yet still using the same provider (org.apache.derby.jdbc.EmbeddedDriver):

    <tomee>

      <Resource id="DatabaseOne" type="DataSource" classpath="${catalina.base}/../../drivers/derby-10.10.1.1.jar">
        JdbcDriver org.apache.derby.jdbc.EmbeddedDriver
        JdbcUrl jdbc:derby:databaseOne;create=true
        UserName SA
      </Resource>

      <Resource id="DatabaseTwo" type="DataSource" classpath="${catalina.base}/../../drivers/derby-10.9.1.0.jar">
        JdbcDriver org.apache.derby.jdbc.EmbeddedDriver
        JdbcUrl jdbc:derby:databaseTwo;create=true
        UserName SA
      </Resource>

    </tomee>
	
# Developer Information
When testing within a Maven environment it is also possible to use direct maven coordinates rather than a file link, like so:

    ....
	<Resource id="DatabaseOne" type="DataSource" classpath="mvn:org.apache.derby:derby:10.10.1.1">
	....
	

# AlternateDriverJarTest.java

This test takes an embedded approach and as you can see the driver paths are specified as a DataSource parameter.
Both examples demonstrate the same, in that two driver versions can be loaded and used within the same application.

    @Configuration
    public Properties config() {

        final File drivers = new File(new File("target"), "drivers").getAbsoluteFile();

        final Properties p = new Properties();
        p.put("openejb.jdbc.datasource-creator", "dbcp-alternative");

        File file = new File(drivers, "derby-10.10.1.1.jar");
        Assert.assertTrue("Failed to find: " + file, file.exists());

        p.put("JdbcOne", "new://Resource?type=DataSource&classpath="
                + file.getAbsolutePath().replace("\\", "/"));
        p.put("JdbcOne.JdbcDriver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("JdbcOne.JdbcUrl", "jdbc:derby:memory:JdbcOne;create=true");
        p.put("JdbcOne.UserName", USER);
        p.put("JdbcOne.Password", PASSWORD);
        p.put("JdbcOne.JtaManaged", "false");

        file = new File(drivers, "derby-10.9.1.0.jar");
        Assert.assertTrue("Failed to find: " + file, file.exists());

        p.put("JdbcTwo", "new://Resource?type=DataSource&classpath="
                + file.getAbsolutePath().replace("\\", "/"));
        p.put("JdbcTwo.JdbcDriver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("JdbcTwo.JdbcUrl", "jdbc:derby:memory:JdbcTwo;create=true");
        p.put("JdbcTwo.UserName", USER);
        p.put("JdbcTwo.Password", PASSWORD);
        p.put("JdbcTwo.JtaManaged", "false");
        return p;
    }

# Full Test Source for AlternateDataSourceTest.java

    package org.superbiz;

    import org.jboss.arquillian.container.test.api.Deployment;
    import org.jboss.arquillian.junit.Arquillian;
    import org.jboss.shrinkwrap.api.ShrinkWrap;
    import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
    import org.jboss.shrinkwrap.api.spec.WebArchive;
    import org.junit.Assert;
    import org.junit.Test;
    import org.junit.runner.RunWith;

    import javax.annotation.Resource;
    import javax.ejb.EJB;
    import javax.ejb.Stateless;
    import javax.sql.DataSource;
    import java.sql.Connection;
    import java.sql.DatabaseMetaData;
    import java.sql.SQLException;

    @RunWith(Arquillian.class)
    public class AlternateDataSourceTest {

        @Deployment
        public static WebArchive createDeployment() {

            return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(DataSourceTester.class)
                .addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml"), "META-INF/ejb-jar.xml");
            //We are using src/test/conf/tomee.xml, but this also works - .addAsResource(new ClassLoaderAsset("META-INF/resources.xml"), "META-INF/resources.xml");
            //Or even using a persistence context - .addAsResource(new ClassLoaderAsset("META-INF/persistence.xml"), "META-INF/persistence.xml");
        }

        @EJB
        private DataSourceTester tester;

        @Test
        public void testDataSourceOne() throws Exception {
            Assert.assertEquals("Should be using 10.10.1.1 - (1458268)", "10.10.1.1 - (1458268)", tester.getOne());
        }

        @Test
        public void testDataSourceTwo() throws Exception {
            Assert.assertEquals("Should be using 10.9.1.0 - (1344872)", "10.9.1.0 - (1344872)", tester.getTwo());
        }

        @Test
        public void testDataSourceBoth() throws Exception {
            Assert.assertEquals("Should be using 10.10.1.1 - (1458268)|10.9.1.0 - (1344872)", "10.10.1.1 - (1458268)|10.9.1.0 - (1344872)", tester.getBoth());
        }

        @Stateless
        public static class DataSourceTester {

            @Resource(name = "DatabaseOne")
            DataSource dataSourceOne;

            @Resource(name = "DatabaseTwo")
            DataSource dataSourceTwo;

            public String getOne() throws Exception {
                return getVersion(dataSourceOne);
            }

            public String getTwo() throws Exception {
                return getVersion(dataSourceTwo);
            }

            public String getBoth() throws Exception {
                return getOne() + "|" + getTwo();
            }

            private static String getVersion(final DataSource ds) throws SQLException {
                Connection con = null;
                try {
                    con = ds.getConnection();
                    final DatabaseMetaData md = con.getMetaData();
                    return md.getDriverVersion();
                } finally {
                    if (con != null) {
                        con.close();
                    }
                }
            }
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.AlternateDataSourceTest
    Apr 17, 2014 2:19:45 PM org.apache.openejb.arquillian.common.Setup findHome
    INFO: Unable to find home in: C:\dev\svn\tomee\examples\datasource-versioning\target\apache-tomee-remote
    Apr 17, 2014 2:19:45 PM org.apache.openejb.arquillian.common.MavenCache getArtifact
    INFO: Downloading org.apache.openejb:apache-tomee:1.6.1-SNAPSHOT:zip:webprofile please wait...
    Apr 17, 2014 2:19:45 PM org.apache.openejb.arquillian.common.Zips unzip
    INFO: Extracting 'C:\Users\Andy\.m2\repository\org\apache\openejb\apache-tomee\1.6.1-SNAPSHOT\apache-tomee-1.6.1-SNAPSHOT-webprofile.zip' to 'C:\dev\svn\tomee\examples\datasource-versioning\target\apache-tomee-remote'
    Apr 17, 2014 2:19:47 PM org.apache.tomee.arquillian.remote.RemoteTomEEContainer configure
    INFO: Downloaded container to: C:\dev\svn\tomee\examples\datasource-versioning\target\apache-tomee-remote\apache-tomee-webprofile-1.6.1-SNAPSHOT
    INFO - The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: C:\Program Files\Java\jdk1.7.0_45\jre\bin;C:\WINDOWS\Sun\Java\bin;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\Program Files (x86)\Windows Live\Shared;C:\Program Files (x86)\ATI Technologies\ATI.ACE\Core-Static;C:\Program Files\SlikSvn\bin;C:\dev\apache-maven-3.2.1\bin;C:\dev\apache-ant-1.9.3\bin;C:\Program Files (x86)\Git\cmd;C:\Program Files (x86)\Git\bin;C:\Program Files\TortoiseGit\bin;C:\Program Files\TortoiseSVN\bin;.
    INFO - Initializing ProtocolHandler ["http-bio-55243"]
    INFO - Initializing ProtocolHandler ["ajp-bio-55245"]
    INFO - Using 'openejb.jdbc.datasource-creator=org.apache.tomee.jdbc.TomEEDataSourceCreator'
    INFO - Optional service not installed: org.apache.tomee.webservices.TomeeJaxRsService
    INFO - Optional service not installed: org.apache.tomee.webservices.TomeeJaxWsService
    INFO - ********************************************************************************
    INFO - OpenEJB http://tomee.apache.org/
    INFO - Startup: Thu Apr 17 14:19:55 CEST 2014
    INFO - Copyright 1999-2013 (C) Apache OpenEJB Project, All Rights Reserved.
    INFO - Version: 7.0.0-SNAPSHOT
    INFO - Build date: 20140417
    INFO - Build time: 01:37
    INFO - ********************************************************************************
    INFO - openejb.home = C:\dev\svn\tomee\examples\datasource-versioning\target\apache-tomee-remote\apache-tomee-webprofile-1.6.1-SNAPSHOT
    INFO - openejb.base = C:\dev\svn\tomee\examples\datasource-versioning\target\apache-tomee-remote\apache-tomee-webprofile-1.6.1-SNAPSHOT
    INFO - Created new singletonService org.apache.openejb.cdi.ThreadSingletonServiceImpl@22c2e2dd
    INFO - Succeeded in installing singleton service
    INFO - openejb configuration file is 'C:\dev\svn\tomee\examples\datasource-versioning\target\apache-tomee-remote\apache-tomee-webprofile-1.6.1-SNAPSHOT\conf\tomee.xml'
    INFO - Configuring Service(id=Tomcat Security Service, type=SecurityService, provider-id=Tomcat Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Configuring Service(id=DatabaseOne, type=Resource, provider-id=Default JDBC Database)
    INFO - Configuring Service(id=DatabaseTwo, type=Resource, provider-id=Default JDBC Database)
    INFO - Using 'openejb.system.apps=true'
    INFO - Configuring enterprise application: openejb
    INFO - Using openejb.deploymentId.format '{ejbName}'
    INFO - Auto-deploying ejb openejb/Deployer: EjbDeployment(deployment-id=openejb/Deployer)
    INFO - Auto-deploying ejb openejb/ConfigurationInfo: EjbDeployment(deployment-id=openejb/ConfigurationInfo)
    INFO - Auto-deploying ejb MEJB: EjbDeployment(deployment-id=MEJB)
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean openejb/Deployer: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Enterprise application "openejb" loaded.
    INFO - Creating TransactionManager(id=Default Transaction Manager)
    INFO - Creating SecurityService(id=Tomcat Security Service)
    INFO - Creating Resource(id=DatabaseOne)
    INFO - Disabling testOnBorrow since no validation query is provided
    INFO - Creating Resource(id=DatabaseTwo)
    INFO - Disabling testOnBorrow since no validation query is provided
    INFO - Creating Container(id=Default Stateless Container)
    INFO - Assembling app: openejb
    INFO - Using 'openejb.jndiname.format={deploymentId}{interfaceType.openejbLegacyName}'
    INFO - Jndi(name=openejb/DeployerBusinessRemote) --> Ejb(deployment-id=openejb/Deployer)
    INFO - Jndi(name=global/openejb/openejb/Deployer!org.apache.openejb.assembler.Deployer) --> Ejb(deployment-id=openejb/Deployer)
    INFO - Jndi(name=global/openejb/openejb/Deployer) --> Ejb(deployment-id=openejb/Deployer)
    INFO - Jndi(name=openejb/ConfigurationInfoBusinessRemote) --> Ejb(deployment-id=openejb/ConfigurationInfo)
    INFO - Jndi(name=global/openejb/openejb/ConfigurationInfo!org.apache.openejb.assembler.classic.cmd.ConfigurationInfo) --> Ejb(deployment-id=openejb/ConfigurationInfo)
    INFO - Jndi(name=global/openejb/openejb/ConfigurationInfo) --> Ejb(deployment-id=openejb/ConfigurationInfo)
    INFO - Jndi(name=MEJB) --> Ejb(deployment-id=MEJB)
    INFO - Jndi(name=global/openejb/MEJB!javax.management.j2ee.ManagementHome) --> Ejb(deployment-id=MEJB)
    INFO - Jndi(name=global/openejb/MEJB) --> Ejb(deployment-id=MEJB)
    INFO - Created Ejb(deployment-id=openejb/Deployer, ejb-name=openejb/Deployer, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=MEJB, ejb-name=MEJB, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=openejb/ConfigurationInfo, ejb-name=openejb/ConfigurationInfo, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=openejb/Deployer, ejb-name=openejb/Deployer, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=MEJB, ejb-name=MEJB, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=openejb/ConfigurationInfo, ejb-name=openejb/ConfigurationInfo, container=Default Stateless Container)
    INFO - Deployed MBean(openejb.user.mbeans:application=openejb,group=org.apache.openejb.assembler.monitoring,name=JMXDeployer)
    INFO - Deployed Application(path=openejb)
    INFO -   ** Bound Services **
    INFO -   NAME                 IP              PORT
    INFO - -------
    INFO - Ready!
    INFO - Initialization processed in 7959 ms
    INFO - Importing a Tomcat Resource with id 'UserDatabase' of type 'org.apache.catalina.UserDatabase'.
    INFO - Creating Resource(id=UserDatabase)
    INFO - Starting service Catalina
    INFO - Starting Servlet Engine: Apache Tomcat (TomEE)/7.0.53 (1.6.1-SNAPSHOT)
    INFO - Starting ProtocolHandler ["http-bio-55243"]
    INFO - Starting ProtocolHandler ["ajp-bio-55245"]
    INFO - Server startup in 288 ms
    WARNING - StandardServer.await: Invalid command '' received
    Apr 17, 2014 2:20:04 PM org.apache.openejb.client.EventLogger log
    INFO: RemoteInitialContextCreated{providerUri=http://localhost:55243/tomee/ejb}
    INFO - Extracting jar: C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0\test.war
    INFO - Extracted path: C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0\test
    INFO - using default host: localhost
    INFO - ------------------------- localhost -> /test
    INFO - Using 'openejb.session.manager=org.apache.tomee.catalina.session.QuickSessionManager'
    INFO - Configuring enterprise application: C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0\test
    INFO - Auto-deploying ejb DataSourceTester: EjbDeployment(deployment-id=DataSourceTester)
    INFO - Auto-linking resource-ref 'java:comp/env/DatabaseTwo' in bean DataSourceTester to Resource(id=DatabaseTwo)
    INFO - Auto-linking resource-ref 'java:comp/env/DatabaseOne' in bean DataSourceTester to Resource(id=DatabaseOne)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.AlternateDataSourceTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Creating Container(id=Default Managed Container)
    INFO - Using directory C:\dev\svn\tomee\examples\datasource-versioning\target\apache-tomee-remote\apache-tomee-webprofile-1.6.1-SNAPSHOT\temp for stateful session passivation
    INFO - Enterprise application "C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0\test" loaded.
    INFO - Assembling app: C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0\test
    INFO - Jndi(name=DataSourceTesterLocalBean) --> Ejb(deployment-id=DataSourceTester)
    INFO - Jndi(name=global/test/DataSourceTester!org.superbiz.AlternateDataSourceTest$DataSourceTester) --> Ejb(deployment-id=DataSourceTester)
    INFO - Jndi(name=global/test/DataSourceTester) --> Ejb(deployment-id=DataSourceTester)
    INFO - Existing thread singleton service in SystemInstance(): org.apache.openejb.cdi.ThreadSingletonServiceImpl@22c2e2dd
    INFO - OpenWebBeans Container is starting...
    INFO - Adding OpenWebBeansPlugin : [CdiPlugin]
    INFO - Adding OpenWebBeansPlugin : [OpenWebBeansJsfPlugin]
    INFO - All injection points were validated successfully.
    INFO - OpenWebBeans Container has started, it took 203 ms.
    INFO - Created Ejb(deployment-id=DataSourceTester, ejb-name=DataSourceTester, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=DataSourceTester, ejb-name=DataSourceTester, container=Default Stateless Container)
    INFO - Deployed Application(path=C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0\test)
    Apr 17, 2014 2:20:11 PM org.apache.openejb.client.EventLogger log
    INFO: RemoteInitialContextCreated{providerUri=http://localhost:55243/tomee/ejb}
    INFO - Undeploying app: C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0\test
    Apr 17, 2014 2:20:13 PM org.apache.openejb.arquillian.common.TomEEContainer undeploy
    INFO: cleaning C:\dev\svn\tomee\examples\datasource-versioning\target\arquillian-test-working-dir\0
    Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 30.155 sec
    Running org.superbiz.AlternateDriverJarTest
    Apr 17, 2014 2:20:13 PM org.apache.openejb.config.ConfigUtils searchForConfiguration
    INFO: Cannot find the configuration file [conf/openejb.xml].  Will attempt to create one for the beans deployed.
    Apr 17, 2014 2:20:13 PM org.apache.openejb.config.ConfigurationFactory configureService
    INFO: Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    Apr 17, 2014 2:20:13 PM org.apache.openejb.config.ConfigurationFactory configureService
    INFO: Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    Apr 17, 2014 2:20:13 PM org.apache.openejb.config.ConfigurationFactory configureService
    INFO: Configuring Service(id=JdbcTwo, type=Resource, provider-id=Default JDBC Database)
    Apr 17, 2014 2:20:13 PM org.apache.openejb.config.ConfigurationFactory configureService
    INFO: Configuring Service(id=JdbcOne, type=Resource, provider-id=Default JDBC Database)
    Apr 17, 2014 2:20:13 PM org.apache.openejb.assembler.classic.Assembler createRecipe
    INFO: Creating TransactionManager(id=Default Transaction Manager)
    Apr 17, 2014 2:20:14 PM org.apache.openejb.assembler.classic.Assembler createRecipe
    INFO: Creating SecurityService(id=Default Security Service)
    Apr 17, 2014 2:20:14 PM org.apache.openejb.assembler.classic.Assembler createRecipe
    INFO: Creating Resource(id=JdbcTwo)
    Apr 17, 2014 2:20:15 PM org.apache.openejb.assembler.classic.Assembler createRecipe
    INFO: Creating Resource(id=JdbcOne)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.ConfigurationFactory configureApplication
    INFO: Configuring enterprise application: C:\dev\svn\tomee\examples\datasource-versioning\AlternateDriverJarTest
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.ConfigurationFactory configureService
    INFO: Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.AutoConfig createContainer
    INFO: Auto-creating a container for bean org.superbiz.AlternateDriverJarTest: Container(type=MANAGED, id=Default Managed Container)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.Assembler createRecipe
    INFO: Creating Container(id=Default Managed Container)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.core.managed.SimplePassivater init
    INFO: Using directory C:\Users\Andy\AppData\Local\Temp for stateful session passivation
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.ConfigurationFactory configureService
    INFO: Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.AutoConfig createContainer
    INFO: Auto-creating a container for bean JdbcOne: Container(type=SINGLETON, id=Default Singleton Container)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.Assembler createRecipe
    INFO: Creating Container(id=Default Singleton Container)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.AutoConfig processResourceRef
    INFO: Auto-linking resource-ref 'java:comp/env/JdbcOne' in bean JdbcOne to Resource(id=JdbcOne)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.AutoConfig processResourceRef
    INFO: Auto-linking resource-ref 'java:comp/env/JdbcTwo' in bean JdbcTwo to Resource(id=JdbcTwo)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.config.AppInfoBuilder build
    INFO: Enterprise application "C:\dev\svn\tomee\examples\datasource-versioning\AlternateDriverJarTest" loaded.
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.Assembler createApplication
    INFO: Assembling app: C:\dev\svn\tomee\examples\datasource-versioning\AlternateDriverJarTest
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.JndiBuilder bind
    INFO: Jndi(name=JdbcOneLocalBean) --> Ejb(deployment-id=JdbcOne)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.JndiBuilder bind
    INFO: Jndi(name=global/AlternateDriverJarTest/app/JdbcOne!org.superbiz.AlternateDriverJarTest$JdbcOne) --> Ejb(deployment-id=JdbcOne)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.JndiBuilder bind
    INFO: Jndi(name=global/AlternateDriverJarTest/app/JdbcOne) --> Ejb(deployment-id=JdbcOne)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.JndiBuilder bind
    INFO: Jndi(name=JdbcTwoLocalBean) --> Ejb(deployment-id=JdbcTwo)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.JndiBuilder bind
    INFO: Jndi(name=global/AlternateDriverJarTest/app/JdbcTwo!org.superbiz.AlternateDriverJarTest$JdbcTwo) --> Ejb(deployment-id=JdbcTwo)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.assembler.classic.JndiBuilder bind
    INFO: Jndi(name=global/AlternateDriverJarTest/app/JdbcTwo) --> Ejb(deployment-id=JdbcTwo)
    Apr 17, 2014 2:20:16 PM org.apache.openejb.cdi.CdiBuilder initializeOWB
    INFO: Created new singletonService org.apache.openejb.cdi.ThreadSingletonServiceImpl@5ddd4e70
    Apr 17, 2014 2:20:16 PM org.apache.openejb.cdi.CdiBuilder initializeOWB
    INFO: Succeeded in installing singleton service
    Apr 17, 2014 2:20:17 PM org.apache.openejb.cdi.OpenEJBLifecycle startApplication
    INFO: OpenWebBeans Container is starting...
    Apr 17, 2014 2:20:17 PM org.apache.webbeans.plugins.PluginLoader startUp
    INFO: Adding OpenWebBeansPlugin : [CdiPlugin]
    Apr 17, 2014 2:20:17 PM org.apache.webbeans.config.BeansDeployer validateInjectionPoints
    INFO: All injection points were validated successfully.
    Apr 17, 2014 2:20:17 PM org.apache.openejb.cdi.OpenEJBLifecycle startApplication
    INFO: OpenWebBeans Container has started, it took 223 ms.
    Apr 17, 2014 2:20:17 PM org.apache.openejb.assembler.classic.Assembler startEjbs
    INFO: Created Ejb(deployment-id=JdbcTwo, ejb-name=JdbcTwo, container=Default Singleton Container)
    Apr 17, 2014 2:20:17 PM org.apache.openejb.assembler.classic.Assembler startEjbs
    INFO: Created Ejb(deployment-id=JdbcOne, ejb-name=JdbcOne, container=Default Singleton Container)
    Apr 17, 2014 2:20:17 PM org.apache.openejb.assembler.classic.Assembler startEjbs
    INFO: Started Ejb(deployment-id=JdbcTwo, ejb-name=JdbcTwo, container=Default Singleton Container)
    Apr 17, 2014 2:20:17 PM org.apache.openejb.assembler.classic.Assembler startEjbs
    INFO: Started Ejb(deployment-id=JdbcOne, ejb-name=JdbcOne, container=Default Singleton Container)
    Apr 17, 2014 2:20:17 PM org.apache.openejb.assembler.classic.Assembler createApplication
    INFO: Deployed Application(path=C:\dev\svn\tomee\examples\datasource-versioning\AlternateDriverJarTest)
    Apr 17, 2014 2:20:20 PM org.apache.openejb.assembler.classic.Assembler destroyApplication
    INFO: Undeploying app: C:\dev\svn\tomee\examples\datasource-versioning\AlternateDriverJarTest
    Apr 17, 2014 2:20:20 PM org.apache.openejb.assembler.classic.Assembler destroyResource
    INFO: Closing DataSource: JdbcTwo
    Apr 17, 2014 2:20:20 PM org.apache.openejb.assembler.classic.Assembler destroyResource
    INFO: Closing DataSource: JdbcOne
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.857 sec
    INFO - A valid shutdown command was received via the shutdown port. Stopping the Server instance.
    INFO - Pausing ProtocolHandler ["http-bio-55243"]
    INFO - Pausing ProtocolHandler ["ajp-bio-55245"]
    INFO - Stopping service Catalina
    INFO - Stopping ProtocolHandler ["http-bio-55243"]
    INFO - Stopping ProtocolHandler ["ajp-bio-55245"]
    INFO - Stopping server services
    INFO - Undeploying app: openejb
    INFO - Closing DataSource: DatabaseOne
    INFO - Closing DataSource: DatabaseTwo
    INFO - Destroying ProtocolHandler ["http-bio-55243"]
    INFO - Destroying ProtocolHandler ["ajp-bio-55245"]

    Results :

    Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
