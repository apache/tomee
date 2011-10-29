[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] OpenEJB :: Web Examples :: Moviefun
[INFO] OpenEJB :: Web Examples :: Moviefun :: Interfaces
[INFO] OpenEJB :: Web Examples :: Moviefun :: App
[INFO] OpenEJB :: Web Examples :: Moviefun :: Monitor
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: Moviefun 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ moviefun ---
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ moviefun ---
[INFO] Installing /Users/dblevins/examples/webapps/moviefun/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/moviefun/1.0/moviefun-1.0.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: Moviefun :: Interfaces 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ moviefun-iface ---
[INFO] Deleting /Users/dblevins/examples/webapps/moviefun/iface/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ moviefun-iface ---
[WARNING] Using platform encoding (MacRoman actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/moviefun/iface/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ moviefun-iface ---
[WARNING] File encoding has not been set, using platform encoding MacRoman, i.e. build is platform dependent!
[INFO] Compiling 3 source files to /Users/dblevins/examples/webapps/moviefun/iface/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ moviefun-iface ---
[WARNING] Using platform encoding (MacRoman actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/moviefun/iface/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ moviefun-iface ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.2:test (default-test) @ moviefun-iface ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ moviefun-iface ---
[INFO] Building jar: /Users/dblevins/examples/webapps/moviefun/iface/target/moviefun-iface-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ moviefun-iface ---
[INFO] Installing /Users/dblevins/examples/webapps/moviefun/iface/target/moviefun-iface-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/moviefun-iface/1.0/moviefun-iface-1.0.jar
[INFO] Installing /Users/dblevins/examples/webapps/moviefun/iface/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/moviefun-iface/1.0/moviefun-iface-1.0.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: Moviefun :: App 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ moviefun-app ---
[INFO] Deleting /Users/dblevins/examples/webapps/moviefun/app/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ moviefun-app ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 3 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ moviefun-app ---
[INFO] Compiling 7 source files to /Users/dblevins/examples/webapps/moviefun/app/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ moviefun-app ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ moviefun-app ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/webapps/moviefun/app/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ moviefun-app ---
[INFO] Surefire report directory: /Users/dblevins/examples/webapps/moviefun/app/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.moviefun.MoviesTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/webapps/moviefun/app
INFO - openejb.base = /Users/dblevins/examples/webapps/moviefun/app
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/webapps/moviefun/app/target/classes
INFO - Found ClientModule in classpath: /Users/dblevins/examples/webapps/moviefun/app/target/test-classes
INFO - Beginning load: /Users/dblevins/examples/webapps/moviefun/app/target/classes
INFO - Beginning load: /Users/dblevins/examples/webapps/moviefun/app/target/test-classes
INFO - Configuring enterprise application: /Users/dblevins/examples/webapps/moviefun/app/classpath.ear
WARN - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean NotifierImpl: Container(type=STATELESS, id=Default Stateless Container)
INFO - Configuring Service(id=Default JMS Connection Factory, type=Resource, provider-id=Default JMS Connection Factory)
INFO - Auto-creating a Resource with id 'Default JMS Connection Factory' of type 'javax.jms.ConnectionFactory for 'NotifierImpl'.
INFO - Configuring Service(id=Default JMS Resource Adapter, type=Resource, provider-id=Default JMS Resource Adapter)
INFO - Auto-linking resource-ref 'java:comp/env/org.superbiz.moviefun.NotifierImpl/connectionFactory' in bean NotifierImpl to Resource(id=Default JMS Connection Factory)
INFO - Configuring Service(id=notifications, type=Resource, provider-id=Default Topic)
INFO - Auto-creating a Resource with id 'notifications' of type 'javax.jms.Topic for 'NotifierImpl'.
INFO - Auto-linking resource-env-ref 'java:comp/env/notifications' in bean NotifierImpl to Resource(id=notifications)
INFO - Configuring PersistenceUnit(name=movie-unit)
INFO - Auto-creating a Resource with id 'movieDatabaseNonJta' of type 'DataSource for 'movie-unit'.
INFO - Configuring Service(id=movieDatabaseNonJta, type=Resource, provider-id=movieDatabase)
INFO - Adjusting PersistenceUnit movie-unit <non-jta-data-source> to Resource ID 'movieDatabaseNonJta' from 'movieDatabaseUnmanaged'
INFO - Enterprise application "/Users/dblevins/examples/webapps/moviefun/app/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/webapps/moviefun/app/classpath.ear
INFO - PersistenceUnit(name=movie-unit, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 425ms
INFO - Jndi(name=NotifierImplLocal) --> Ejb(deployment-id=NotifierImpl)
INFO - Jndi(name=global/classpath.ear/app/NotifierImpl!org.superbiz.moviefun.Notifier) --> Ejb(deployment-id=NotifierImpl)
INFO - Jndi(name=global/classpath.ear/app/NotifierImpl) --> Ejb(deployment-id=NotifierImpl)
INFO - Jndi(name=MoviesLocalBean) --> Ejb(deployment-id=Movies)
INFO - Jndi(name=global/classpath.ear/app/Movies!org.superbiz.moviefun.MoviesImpl) --> Ejb(deployment-id=Movies)
INFO - Jndi(name=MoviesLocal) --> Ejb(deployment-id=Movies)
INFO - Jndi(name=global/classpath.ear/app/Movies!org.superbiz.moviefun.Movies) --> Ejb(deployment-id=Movies)
INFO - Jndi(name=MoviesRemote) --> Ejb(deployment-id=Movies)
INFO - Jndi(name=global/classpath.ear/app/Movies!org.superbiz.moviefun.MoviesRemote) --> Ejb(deployment-id=Movies)
INFO - Jndi(name=global/classpath.ear/app/Movies) --> Ejb(deployment-id=Movies)
INFO - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=NotifierImpl, ejb-name=NotifierImpl, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=NotifierImpl, ejb-name=NotifierImpl, container=Default Stateless Container)
INFO - LocalClient(class=org.superbiz.moviefun.MoviesTest, module=test-classes) 
INFO - Deployed Application(path=/Users/dblevins/examples/webapps/moviefun/app/classpath.ear)
WARN - Meta class "org.superbiz.moviefun.Movie_" for entity class org.superbiz.moviefun.Movie can not be registered with following exception "java.security.PrivilegedActionException: java.lang.ClassNotFoundException: org.superbiz.moviefun.Movie_"
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.368 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-war-plugin:2.1.1:war (default-war) @ moviefun-app ---
[INFO] Packaging webapp
[INFO] Assembling webapp [moviefun-app] in [/Users/dblevins/examples/webapps/moviefun/app/target/moviefun]
[INFO] Processing war project
[INFO] Copying webapp resources [/Users/dblevins/examples/webapps/moviefun/app/src/main/webapp]
[INFO] Webapp assembled in [88 msecs]
[INFO] Building war: /Users/dblevins/examples/webapps/moviefun/app/target/moviefun.war
[INFO] WEB-INF/web.xml already added, skipping
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ moviefun-app ---
[INFO] Installing /Users/dblevins/examples/webapps/moviefun/app/target/moviefun.war to /Users/dblevins/.m2/repository/org/superbiz/moviefun-app/1.0/moviefun-app-1.0.war
[INFO] Installing /Users/dblevins/examples/webapps/moviefun/app/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/moviefun-app/1.0/moviefun-app-1.0.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: Moviefun :: Monitor 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ moviefun-monitor ---
[INFO] Deleting /Users/dblevins/examples/webapps/moviefun/monitor/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ moviefun-monitor ---
[WARNING] Using platform encoding (MacRoman actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] Copying 3 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ moviefun-monitor ---
[WARNING] File encoding has not been set, using platform encoding MacRoman, i.e. build is platform dependent!
[INFO] Compiling 2 source files to /Users/dblevins/examples/webapps/moviefun/monitor/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ moviefun-monitor ---
[WARNING] Using platform encoding (MacRoman actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/moviefun/monitor/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ moviefun-monitor ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ moviefun-monitor ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/webapps/moviefun/monitor/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ moviefun-monitor ---
[INFO] Building jar: /Users/dblevins/examples/webapps/moviefun/monitor/target/moviefun-monitor-1.0.jar
[INFO] 
[INFO] --- maven-assembly-plugin:2.2-beta-5:attached (default) @ moviefun-monitor ---
[INFO] Reading assembly descriptor: src/main/assembly/client.xml
[WARNING] Artifact: org.superbiz:moviefun-monitor:jar:1.0 references the same file as the assembly destination file. Moving it to a temporary location for inclusion.
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] javax/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/dependencies.txt already added, skipping
[INFO] META-INF/dependencies.xml already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/management/ already added, skipping
[INFO] javax/xml/ already added, skipping
[INFO] javax/xml/ws/ already added, skipping
[INFO] javax/xml/ws/EndpointReference.class already added, skipping
[INFO] javax/xml/ws/WebServiceFeature.class already added, skipping
[INFO] javax/xml/ws/wsaddressing/ already added, skipping
[INFO] javax/xml/ws/wsaddressing/W3CEndpointReference.class already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/dependencies.txt already added, skipping
[INFO] META-INF/dependencies.xml already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.activemq/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/broker/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/http/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/http/activemq.apache.org/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/http/activemq.apache.org/schema/ already added, skipping
[INFO] META-INF/spring.handlers already added, skipping
[INFO] META-INF/spring.schemas already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/activemq/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.activemq/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/management/ already added, skipping
[INFO] javax/management/j2ee/ already added, skipping
[INFO] javax/management/j2ee/statistics/ already added, skipping
[INFO] javax/management/j2ee/ListenerRegistration.class already added, skipping
[INFO] javax/management/j2ee/Management.class already added, skipping
[INFO] javax/management/j2ee/ManagementHome.class already added, skipping
[INFO] javax/management/j2ee/statistics/BoundaryStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/BoundedRangeStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/CountStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/EJBStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/EntityBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JavaMailStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JCAConnectionPoolStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JCAConnectionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JCAStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JDBCConnectionPoolStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JDBCConnectionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JDBCStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSConnectionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSConsumerStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSEndpointStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSProducerStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSSessionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JTAStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JVMStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/MessageDrivenBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/RangeStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/ServletStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/SessionBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/StatefulSessionBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/StatelessSessionBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/Statistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/Stats.class already added, skipping
[INFO] javax/management/j2ee/statistics/TimeStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/URLStats.class already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-management_1.1_spec/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-management_1.1_spec/pom.xml already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-management_1.1_spec/pom.properties already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] org/apache/commons/logging/ already added, skipping
[INFO] org/apache/commons/logging/impl/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] org/apache/commons/logging/Log.class already added, skipping
[INFO] org/apache/commons/logging/LogConfigurationException.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$1.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$2.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$3.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$4.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$5.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory.class already added, skipping
[INFO] org/apache/commons/logging/LogSource.class already added, skipping
[INFO] org/apache/commons/logging/impl/Jdk14Logger.class already added, skipping
[INFO] org/apache/commons/logging/impl/LogFactoryImpl.class already added, skipping
[INFO] org/apache/commons/logging/impl/NoOpLog.class already added, skipping
[INFO] org/apache/commons/logging/impl/SimpleLog$1.class already added, skipping
[INFO] org/apache/commons/logging/impl/SimpleLog.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$1.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$2.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$Entry.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$Referenced.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$WeakKey.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/servlet/ already added, skipping
[INFO] javax/servlet/http/ already added, skipping
[INFO] javax/servlet/jsp/ already added, skipping
[INFO] javax/servlet/jsp/resources/ already added, skipping
[INFO] javax/servlet/jsp/tagext/ already added, skipping
[INFO] javax/servlet/resources/ already added, skipping
[INFO] javax/servlet/Filter.class already added, skipping
[INFO] javax/servlet/FilterChain.class already added, skipping
[INFO] javax/servlet/FilterConfig.class already added, skipping
[INFO] javax/servlet/GenericServlet.class already added, skipping
[INFO] javax/servlet/http/Cookie.class already added, skipping
[INFO] javax/servlet/http/HttpServlet.class already added, skipping
[INFO] javax/servlet/http/HttpServletRequest.class already added, skipping
[INFO] javax/servlet/http/HttpServletRequestWrapper.class already added, skipping
[INFO] javax/servlet/http/HttpServletResponse.class already added, skipping
[INFO] javax/servlet/http/HttpServletResponseWrapper.class already added, skipping
[INFO] javax/servlet/http/HttpSession.class already added, skipping
[INFO] javax/servlet/http/HttpSessionActivationListener.class already added, skipping
[INFO] javax/servlet/http/HttpSessionAttributeListener.class already added, skipping
[INFO] javax/servlet/http/HttpSessionBindingEvent.class already added, skipping
[INFO] javax/servlet/http/HttpSessionBindingListener.class already added, skipping
[INFO] javax/servlet/http/HttpSessionContext.class already added, skipping
[INFO] javax/servlet/http/HttpSessionEvent.class already added, skipping
[INFO] javax/servlet/http/HttpSessionListener.class already added, skipping
[INFO] javax/servlet/http/HttpUtils.class already added, skipping
[INFO] javax/servlet/http/LocalStrings.properties already added, skipping
[INFO] javax/servlet/http/LocalStrings_es.properties already added, skipping
[INFO] javax/servlet/http/LocalStrings_ja.properties already added, skipping
[INFO] javax/servlet/http/NoBodyOutputStream.class already added, skipping
[INFO] javax/servlet/http/NoBodyResponse.class already added, skipping
[INFO] javax/servlet/jsp/HttpJspPage.class already added, skipping
[INFO] javax/servlet/jsp/JspEngineInfo.class already added, skipping
[INFO] javax/servlet/jsp/JspException.class already added, skipping
[INFO] javax/servlet/jsp/JspFactory.class already added, skipping
[INFO] javax/servlet/jsp/JspPage.class already added, skipping
[INFO] javax/servlet/jsp/JspTagException.class already added, skipping
[INFO] javax/servlet/jsp/JspWriter.class already added, skipping
[INFO] javax/servlet/jsp/PageContext.class already added, skipping
[INFO] javax/servlet/jsp/resources/jspxml.dtd already added, skipping
[INFO] javax/servlet/jsp/resources/jspxml.xsd already added, skipping
[INFO] javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd already added, skipping
[INFO] javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd already added, skipping
[INFO] javax/servlet/jsp/tagext/BodyContent.class already added, skipping
[INFO] javax/servlet/jsp/tagext/BodyTag.class already added, skipping
[INFO] javax/servlet/jsp/tagext/BodyTagSupport.class already added, skipping
[INFO] javax/servlet/jsp/tagext/IterationTag.class already added, skipping
[INFO] javax/servlet/jsp/tagext/PageData.class already added, skipping
[INFO] javax/servlet/jsp/tagext/Tag.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagAttributeInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagData.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagExtraInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagLibraryInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagLibraryValidator.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagSupport.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagVariableInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TryCatchFinally.class already added, skipping
[INFO] javax/servlet/jsp/tagext/ValidationMessage.class already added, skipping
[INFO] javax/servlet/jsp/tagext/VariableInfo.class already added, skipping
[INFO] javax/servlet/LocalStrings.properties already added, skipping
[INFO] javax/servlet/LocalStrings_ja.properties already added, skipping
[INFO] javax/servlet/RequestDispatcher.class already added, skipping
[INFO] javax/servlet/resources/web-app_2_2.dtd already added, skipping
[INFO] javax/servlet/resources/web-app_2_3.dtd already added, skipping
[INFO] javax/servlet/Servlet.class already added, skipping
[INFO] javax/servlet/ServletConfig.class already added, skipping
[INFO] javax/servlet/ServletContext.class already added, skipping
[INFO] javax/servlet/ServletContextAttributeEvent.class already added, skipping
[INFO] javax/servlet/ServletContextAttributeListener.class already added, skipping
[INFO] javax/servlet/ServletContextEvent.class already added, skipping
[INFO] javax/servlet/ServletContextListener.class already added, skipping
[INFO] javax/servlet/ServletException.class already added, skipping
[INFO] javax/servlet/ServletInputStream.class already added, skipping
[INFO] javax/servlet/ServletOutputStream.class already added, skipping
[INFO] javax/servlet/ServletRequest.class already added, skipping
[INFO] javax/servlet/ServletRequestWrapper.class already added, skipping
[INFO] javax/servlet/ServletResponse.class already added, skipping
[INFO] javax/servlet/ServletResponseWrapper.class already added, skipping
[INFO] javax/servlet/SingleThreadModel.class already added, skipping
[INFO] javax/servlet/UnavailableException.class already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.activemq/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/transport/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/wireformat/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/activemq/ already added, skipping
[INFO] org/apache/activemq/protobuf/ already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] org/apache/activemq/protobuf/AsciiBuffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/BaseMessage.class already added, skipping
[INFO] org/apache/activemq/protobuf/Buffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/BufferInputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/BufferOutputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/CodedInputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/CodedOutputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$2.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$3.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$Closure.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/CommandLineSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/CompilerException.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/EnumDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/EnumFieldDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ExtensionsDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/FieldDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/IntrospectionSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$2.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$3.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$Closure.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/MessageDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/MethodDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/OptionDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ParseException.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser$JJCalls.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser$LookaheadSuccess.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParserConstants.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParserTokenManager.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/SimpleCharStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/Token.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/TokenMgrError.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ParserSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ProtoDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ProtoMojo$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ProtoMojo.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ServiceDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat$InvalidEscapeSequence.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat$ParseException.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat$Tokenizer.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TypeDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/DeferredDecodeMessage.class already added, skipping
[INFO] org/apache/activemq/protobuf/InvalidProtocolBufferException.class already added, skipping
[INFO] org/apache/activemq/protobuf/Message.class already added, skipping
[INFO] org/apache/activemq/protobuf/MessageBuffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/MessageBufferSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/PBMessage.class already added, skipping
[INFO] org/apache/activemq/protobuf/UninitializedMessageException.class already added, skipping
[INFO] org/apache/activemq/protobuf/UTF8Buffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/WireFormat.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/geronimo/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.components/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/geronimo/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-connector_1.6_spec/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-connector_1.6_spec/pom.properties already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-connector_1.6_spec/pom.xml already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/resource/ already added, skipping
[INFO] javax/resource/NotSupportedException.class already added, skipping
[INFO] javax/resource/Referenceable.class already added, skipping
[INFO] javax/resource/ResourceException.class already added, skipping
[INFO] javax/resource/cci/ already added, skipping
[INFO] javax/resource/cci/Connection.class already added, skipping
[INFO] javax/resource/cci/ConnectionFactory.class already added, skipping
[INFO] javax/resource/cci/ConnectionMetaData.class already added, skipping
[INFO] javax/resource/cci/ConnectionSpec.class already added, skipping
[INFO] javax/resource/cci/IndexedRecord.class already added, skipping
[INFO] javax/resource/cci/Interaction.class already added, skipping
[INFO] javax/resource/cci/InteractionSpec.class already added, skipping
[INFO] javax/resource/cci/LocalTransaction.class already added, skipping
[INFO] javax/resource/cci/MappedRecord.class already added, skipping
[INFO] javax/resource/cci/MessageListener.class already added, skipping
[INFO] javax/resource/cci/Record.class already added, skipping
[INFO] javax/resource/cci/RecordFactory.class already added, skipping
[INFO] javax/resource/cci/ResourceAdapterMetaData.class already added, skipping
[INFO] javax/resource/cci/ResourceWarning.class already added, skipping
[INFO] javax/resource/cci/ResultSet.class already added, skipping
[INFO] javax/resource/cci/ResultSetInfo.class already added, skipping
[INFO] javax/resource/cci/Streamable.class already added, skipping
[INFO] javax/resource/spi/ already added, skipping
[INFO] javax/resource/spi/Activation.class already added, skipping
[INFO] javax/resource/spi/ActivationSpec.class already added, skipping
[INFO] javax/resource/spi/AdministeredObject.class already added, skipping
[INFO] javax/resource/spi/ApplicationServerInternalException.class already added, skipping
[INFO] javax/resource/spi/AuthenticationMechanism$CredentialInterface.class already added, skipping
[INFO] javax/resource/spi/AuthenticationMechanism.class already added, skipping
[INFO] javax/resource/spi/BootstrapContext.class already added, skipping
[INFO] javax/resource/spi/CommException.class already added, skipping
[INFO] javax/resource/spi/ConfigProperty.class already added, skipping
[INFO] javax/resource/spi/ConnectionDefinition.class already added, skipping
[INFO] javax/resource/spi/ConnectionDefinitions.class already added, skipping
[INFO] javax/resource/spi/ConnectionEvent.class already added, skipping
[INFO] javax/resource/spi/ConnectionEventListener.class already added, skipping
[INFO] javax/resource/spi/ConnectionManager.class already added, skipping
[INFO] javax/resource/spi/ConnectionRequestInfo.class already added, skipping
[INFO] javax/resource/spi/Connector.class already added, skipping
[INFO] javax/resource/spi/DissociatableManagedConnection.class already added, skipping
[INFO] javax/resource/spi/EISSystemException.class already added, skipping
[INFO] javax/resource/spi/IllegalStateException.class already added, skipping
[INFO] javax/resource/spi/InvalidPropertyException.class already added, skipping
[INFO] javax/resource/spi/LazyAssociatableConnectionManager.class already added, skipping
[INFO] javax/resource/spi/LazyEnlistableConnectionManager.class already added, skipping
[INFO] javax/resource/spi/LazyEnlistableManagedConnection.class already added, skipping
[INFO] javax/resource/spi/LocalTransaction.class already added, skipping
[INFO] javax/resource/spi/LocalTransactionException.class already added, skipping
[INFO] javax/resource/spi/ManagedConnection.class already added, skipping
[INFO] javax/resource/spi/ManagedConnectionFactory.class already added, skipping
[INFO] javax/resource/spi/ManagedConnectionMetaData.class already added, skipping
[INFO] javax/resource/spi/ResourceAdapter.class already added, skipping
[INFO] javax/resource/spi/ResourceAdapterAssociation.class already added, skipping
[INFO] javax/resource/spi/ResourceAdapterInternalException.class already added, skipping
[INFO] javax/resource/spi/ResourceAllocationException.class already added, skipping
[INFO] javax/resource/spi/RetryableException.class already added, skipping
[INFO] javax/resource/spi/RetryableUnavailableException.class already added, skipping
[INFO] javax/resource/spi/SecurityException.class already added, skipping
[INFO] javax/resource/spi/SecurityPermission.class already added, skipping
[INFO] javax/resource/spi/SharingViolationException.class already added, skipping
[INFO] javax/resource/spi/TransactionSupport$TransactionSupportLevel.class already added, skipping
[INFO] javax/resource/spi/TransactionSupport.class already added, skipping
[INFO] javax/resource/spi/UnavailableException.class already added, skipping
[INFO] javax/resource/spi/ValidatingManagedConnectionFactory.class already added, skipping
[INFO] javax/resource/spi/XATerminator.class already added, skipping
[INFO] javax/resource/spi/endpoint/ already added, skipping
[INFO] javax/resource/spi/endpoint/MessageEndpoint.class already added, skipping
[INFO] javax/resource/spi/endpoint/MessageEndpointFactory.class already added, skipping
[INFO] javax/resource/spi/security/ already added, skipping
[INFO] javax/resource/spi/security/GenericCredential.class already added, skipping
[INFO] javax/resource/spi/security/PasswordCredential.class already added, skipping
[INFO] javax/resource/spi/work/ already added, skipping
[INFO] javax/resource/spi/work/DistributableWork.class already added, skipping
[INFO] javax/resource/spi/work/DistributableWorkManager.class already added, skipping
[INFO] javax/resource/spi/work/ExecutionContext.class already added, skipping
[INFO] javax/resource/spi/work/HintsContext.class already added, skipping
[INFO] javax/resource/spi/work/RetryableWorkRejectedException.class already added, skipping
[INFO] javax/resource/spi/work/SecurityContext.class already added, skipping
[INFO] javax/resource/spi/work/TransactionContext.class already added, skipping
[INFO] javax/resource/spi/work/Work.class already added, skipping
[INFO] javax/resource/spi/work/WorkAdapter.class already added, skipping
[INFO] javax/resource/spi/work/WorkCompletedException.class already added, skipping
[INFO] javax/resource/spi/work/WorkContext.class already added, skipping
[INFO] javax/resource/spi/work/WorkContextErrorCodes.class already added, skipping
[INFO] javax/resource/spi/work/WorkContextLifecycleListener.class already added, skipping
[INFO] javax/resource/spi/work/WorkContextProvider.class already added, skipping
[INFO] javax/resource/spi/work/WorkEvent.class already added, skipping
[INFO] javax/resource/spi/work/WorkException.class already added, skipping
[INFO] javax/resource/spi/work/WorkListener.class already added, skipping
[INFO] javax/resource/spi/work/WorkManager.class already added, skipping
[INFO] javax/resource/spi/work/WorkRejectedException.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] javax/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/geronimo/ already added, skipping
[INFO] org/apache/geronimo/osgi/ already added, skipping
[INFO] org/apache/geronimo/osgi/locator/ already added, skipping
[INFO] org/apache/geronimo/osgi/locator/Activator.class already added, skipping
[INFO] org/apache/geronimo/osgi/locator/ProviderLocator.class already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] javax/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/slf4j/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.slf4j/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/openwebbeans/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/openwebbeans/openwebbeans.properties already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/openwebbeans/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] org/apache/webbeans/ee/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/openwebbeans/openwebbeans.properties already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/openwebbeans/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/openwebbeans/openwebbeans.properties already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] org/apache/commons/collections/ already added, skipping
[INFO] org/apache/commons/collections/ArrayStack.class already added, skipping
[INFO] org/apache/commons/collections/Buffer.class already added, skipping
[INFO] org/apache/commons/collections/BufferUnderflowException.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$1.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$CollectionView$CollectionViewIterator.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$CollectionView.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$EntrySet.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$KeySet.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$Values.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.bval/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/bval/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/DISCLAIMER already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/ejb-jar.xml already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] Building jar: /Users/dblevins/examples/webapps/moviefun/monitor/target/moviefun-monitor-1.0.jar
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] javax/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/dependencies.txt already added, skipping
[INFO] META-INF/dependencies.xml already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/management/ already added, skipping
[INFO] javax/xml/ already added, skipping
[INFO] javax/xml/ws/ already added, skipping
[INFO] javax/xml/ws/EndpointReference.class already added, skipping
[INFO] javax/xml/ws/WebServiceFeature.class already added, skipping
[INFO] javax/xml/ws/wsaddressing/ already added, skipping
[INFO] javax/xml/ws/wsaddressing/W3CEndpointReference.class already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/dependencies.txt already added, skipping
[INFO] META-INF/dependencies.xml already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/openejb/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openejb/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.activemq/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/broker/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/http/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/http/activemq.apache.org/ already added, skipping
[INFO] META-INF/services/org/apache/xbean/spring/http/activemq.apache.org/schema/ already added, skipping
[INFO] META-INF/spring.handlers already added, skipping
[INFO] META-INF/spring.schemas already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/activemq/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.activemq/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/management/ already added, skipping
[INFO] javax/management/j2ee/ already added, skipping
[INFO] javax/management/j2ee/statistics/ already added, skipping
[INFO] javax/management/j2ee/ListenerRegistration.class already added, skipping
[INFO] javax/management/j2ee/Management.class already added, skipping
[INFO] javax/management/j2ee/ManagementHome.class already added, skipping
[INFO] javax/management/j2ee/statistics/BoundaryStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/BoundedRangeStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/CountStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/EJBStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/EntityBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JavaMailStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JCAConnectionPoolStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JCAConnectionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JCAStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JDBCConnectionPoolStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JDBCConnectionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JDBCStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSConnectionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSConsumerStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSEndpointStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSProducerStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSSessionStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JMSStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JTAStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/JVMStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/MessageDrivenBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/RangeStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/ServletStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/SessionBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/StatefulSessionBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/StatelessSessionBeanStats.class already added, skipping
[INFO] javax/management/j2ee/statistics/Statistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/Stats.class already added, skipping
[INFO] javax/management/j2ee/statistics/TimeStatistic.class already added, skipping
[INFO] javax/management/j2ee/statistics/URLStats.class already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-management_1.1_spec/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-management_1.1_spec/pom.xml already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-management_1.1_spec/pom.properties already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] org/apache/commons/logging/ already added, skipping
[INFO] org/apache/commons/logging/impl/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] org/apache/commons/logging/Log.class already added, skipping
[INFO] org/apache/commons/logging/LogConfigurationException.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$1.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$2.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$3.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$4.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory$5.class already added, skipping
[INFO] org/apache/commons/logging/LogFactory.class already added, skipping
[INFO] org/apache/commons/logging/LogSource.class already added, skipping
[INFO] org/apache/commons/logging/impl/Jdk14Logger.class already added, skipping
[INFO] org/apache/commons/logging/impl/LogFactoryImpl.class already added, skipping
[INFO] org/apache/commons/logging/impl/NoOpLog.class already added, skipping
[INFO] org/apache/commons/logging/impl/SimpleLog$1.class already added, skipping
[INFO] org/apache/commons/logging/impl/SimpleLog.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$1.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$2.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$Entry.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$Referenced.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable$WeakKey.class already added, skipping
[INFO] org/apache/commons/logging/impl/WeakHashtable.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/servlet/ already added, skipping
[INFO] javax/servlet/http/ already added, skipping
[INFO] javax/servlet/jsp/ already added, skipping
[INFO] javax/servlet/jsp/resources/ already added, skipping
[INFO] javax/servlet/jsp/tagext/ already added, skipping
[INFO] javax/servlet/resources/ already added, skipping
[INFO] javax/servlet/Filter.class already added, skipping
[INFO] javax/servlet/FilterChain.class already added, skipping
[INFO] javax/servlet/FilterConfig.class already added, skipping
[INFO] javax/servlet/GenericServlet.class already added, skipping
[INFO] javax/servlet/http/Cookie.class already added, skipping
[INFO] javax/servlet/http/HttpServlet.class already added, skipping
[INFO] javax/servlet/http/HttpServletRequest.class already added, skipping
[INFO] javax/servlet/http/HttpServletRequestWrapper.class already added, skipping
[INFO] javax/servlet/http/HttpServletResponse.class already added, skipping
[INFO] javax/servlet/http/HttpServletResponseWrapper.class already added, skipping
[INFO] javax/servlet/http/HttpSession.class already added, skipping
[INFO] javax/servlet/http/HttpSessionActivationListener.class already added, skipping
[INFO] javax/servlet/http/HttpSessionAttributeListener.class already added, skipping
[INFO] javax/servlet/http/HttpSessionBindingEvent.class already added, skipping
[INFO] javax/servlet/http/HttpSessionBindingListener.class already added, skipping
[INFO] javax/servlet/http/HttpSessionContext.class already added, skipping
[INFO] javax/servlet/http/HttpSessionEvent.class already added, skipping
[INFO] javax/servlet/http/HttpSessionListener.class already added, skipping
[INFO] javax/servlet/http/HttpUtils.class already added, skipping
[INFO] javax/servlet/http/LocalStrings.properties already added, skipping
[INFO] javax/servlet/http/LocalStrings_es.properties already added, skipping
[INFO] javax/servlet/http/LocalStrings_ja.properties already added, skipping
[INFO] javax/servlet/http/NoBodyOutputStream.class already added, skipping
[INFO] javax/servlet/http/NoBodyResponse.class already added, skipping
[INFO] javax/servlet/jsp/HttpJspPage.class already added, skipping
[INFO] javax/servlet/jsp/JspEngineInfo.class already added, skipping
[INFO] javax/servlet/jsp/JspException.class already added, skipping
[INFO] javax/servlet/jsp/JspFactory.class already added, skipping
[INFO] javax/servlet/jsp/JspPage.class already added, skipping
[INFO] javax/servlet/jsp/JspTagException.class already added, skipping
[INFO] javax/servlet/jsp/JspWriter.class already added, skipping
[INFO] javax/servlet/jsp/PageContext.class already added, skipping
[INFO] javax/servlet/jsp/resources/jspxml.dtd already added, skipping
[INFO] javax/servlet/jsp/resources/jspxml.xsd already added, skipping
[INFO] javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd already added, skipping
[INFO] javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd already added, skipping
[INFO] javax/servlet/jsp/tagext/BodyContent.class already added, skipping
[INFO] javax/servlet/jsp/tagext/BodyTag.class already added, skipping
[INFO] javax/servlet/jsp/tagext/BodyTagSupport.class already added, skipping
[INFO] javax/servlet/jsp/tagext/IterationTag.class already added, skipping
[INFO] javax/servlet/jsp/tagext/PageData.class already added, skipping
[INFO] javax/servlet/jsp/tagext/Tag.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagAttributeInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagData.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagExtraInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagLibraryInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagLibraryValidator.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagSupport.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TagVariableInfo.class already added, skipping
[INFO] javax/servlet/jsp/tagext/TryCatchFinally.class already added, skipping
[INFO] javax/servlet/jsp/tagext/ValidationMessage.class already added, skipping
[INFO] javax/servlet/jsp/tagext/VariableInfo.class already added, skipping
[INFO] javax/servlet/LocalStrings.properties already added, skipping
[INFO] javax/servlet/LocalStrings_ja.properties already added, skipping
[INFO] javax/servlet/RequestDispatcher.class already added, skipping
[INFO] javax/servlet/resources/web-app_2_2.dtd already added, skipping
[INFO] javax/servlet/resources/web-app_2_3.dtd already added, skipping
[INFO] javax/servlet/Servlet.class already added, skipping
[INFO] javax/servlet/ServletConfig.class already added, skipping
[INFO] javax/servlet/ServletContext.class already added, skipping
[INFO] javax/servlet/ServletContextAttributeEvent.class already added, skipping
[INFO] javax/servlet/ServletContextAttributeListener.class already added, skipping
[INFO] javax/servlet/ServletContextEvent.class already added, skipping
[INFO] javax/servlet/ServletContextListener.class already added, skipping
[INFO] javax/servlet/ServletException.class already added, skipping
[INFO] javax/servlet/ServletInputStream.class already added, skipping
[INFO] javax/servlet/ServletOutputStream.class already added, skipping
[INFO] javax/servlet/ServletRequest.class already added, skipping
[INFO] javax/servlet/ServletRequestWrapper.class already added, skipping
[INFO] javax/servlet/ServletResponse.class already added, skipping
[INFO] javax/servlet/ServletResponseWrapper.class already added, skipping
[INFO] javax/servlet/SingleThreadModel.class already added, skipping
[INFO] javax/servlet/UnavailableException.class already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.activemq/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/services/org/ already added, skipping
[INFO] META-INF/services/org/apache/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/transport/ already added, skipping
[INFO] META-INF/services/org/apache/activemq/wireformat/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/activemq/ already added, skipping
[INFO] org/apache/activemq/protobuf/ already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] org/apache/activemq/protobuf/AsciiBuffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/BaseMessage.class already added, skipping
[INFO] org/apache/activemq/protobuf/Buffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/BufferInputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/BufferOutputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/CodedInputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/CodedOutputStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$2.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$3.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator$Closure.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/AltJavaGenerator.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/CommandLineSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/CompilerException.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/EnumDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/EnumFieldDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ExtensionsDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/FieldDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/IntrospectionSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$2.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$3.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator$Closure.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/JavaGenerator.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/MessageDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/MethodDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/OptionDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ParseException.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser$JJCalls.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser$LookaheadSuccess.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParser.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParserConstants.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/ProtoParserTokenManager.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/SimpleCharStream.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/Token.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/parser/TokenMgrError.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ParserSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ProtoDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ProtoMojo$1.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ProtoMojo.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/ServiceDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat$InvalidEscapeSequence.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat$ParseException.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat$Tokenizer.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TextFormat.class already added, skipping
[INFO] org/apache/activemq/protobuf/compiler/TypeDescriptor.class already added, skipping
[INFO] org/apache/activemq/protobuf/DeferredDecodeMessage.class already added, skipping
[INFO] org/apache/activemq/protobuf/InvalidProtocolBufferException.class already added, skipping
[INFO] org/apache/activemq/protobuf/Message.class already added, skipping
[INFO] org/apache/activemq/protobuf/MessageBuffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/MessageBufferSupport.class already added, skipping
[INFO] org/apache/activemq/protobuf/PBMessage.class already added, skipping
[INFO] org/apache/activemq/protobuf/UninitializedMessageException.class already added, skipping
[INFO] org/apache/activemq/protobuf/UTF8Buffer.class already added, skipping
[INFO] org/apache/activemq/protobuf/WireFormat.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/geronimo/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.components/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/geronimo/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-connector_1.6_spec/ already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-connector_1.6_spec/pom.properties already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/geronimo-j2ee-connector_1.6_spec/pom.xml already added, skipping
[INFO] javax/ already added, skipping
[INFO] javax/resource/ already added, skipping
[INFO] javax/resource/NotSupportedException.class already added, skipping
[INFO] javax/resource/Referenceable.class already added, skipping
[INFO] javax/resource/ResourceException.class already added, skipping
[INFO] javax/resource/cci/ already added, skipping
[INFO] javax/resource/cci/Connection.class already added, skipping
[INFO] javax/resource/cci/ConnectionFactory.class already added, skipping
[INFO] javax/resource/cci/ConnectionMetaData.class already added, skipping
[INFO] javax/resource/cci/ConnectionSpec.class already added, skipping
[INFO] javax/resource/cci/IndexedRecord.class already added, skipping
[INFO] javax/resource/cci/Interaction.class already added, skipping
[INFO] javax/resource/cci/InteractionSpec.class already added, skipping
[INFO] javax/resource/cci/LocalTransaction.class already added, skipping
[INFO] javax/resource/cci/MappedRecord.class already added, skipping
[INFO] javax/resource/cci/MessageListener.class already added, skipping
[INFO] javax/resource/cci/Record.class already added, skipping
[INFO] javax/resource/cci/RecordFactory.class already added, skipping
[INFO] javax/resource/cci/ResourceAdapterMetaData.class already added, skipping
[INFO] javax/resource/cci/ResourceWarning.class already added, skipping
[INFO] javax/resource/cci/ResultSet.class already added, skipping
[INFO] javax/resource/cci/ResultSetInfo.class already added, skipping
[INFO] javax/resource/cci/Streamable.class already added, skipping
[INFO] javax/resource/spi/ already added, skipping
[INFO] javax/resource/spi/Activation.class already added, skipping
[INFO] javax/resource/spi/ActivationSpec.class already added, skipping
[INFO] javax/resource/spi/AdministeredObject.class already added, skipping
[INFO] javax/resource/spi/ApplicationServerInternalException.class already added, skipping
[INFO] javax/resource/spi/AuthenticationMechanism$CredentialInterface.class already added, skipping
[INFO] javax/resource/spi/AuthenticationMechanism.class already added, skipping
[INFO] javax/resource/spi/BootstrapContext.class already added, skipping
[INFO] javax/resource/spi/CommException.class already added, skipping
[INFO] javax/resource/spi/ConfigProperty.class already added, skipping
[INFO] javax/resource/spi/ConnectionDefinition.class already added, skipping
[INFO] javax/resource/spi/ConnectionDefinitions.class already added, skipping
[INFO] javax/resource/spi/ConnectionEvent.class already added, skipping
[INFO] javax/resource/spi/ConnectionEventListener.class already added, skipping
[INFO] javax/resource/spi/ConnectionManager.class already added, skipping
[INFO] javax/resource/spi/ConnectionRequestInfo.class already added, skipping
[INFO] javax/resource/spi/Connector.class already added, skipping
[INFO] javax/resource/spi/DissociatableManagedConnection.class already added, skipping
[INFO] javax/resource/spi/EISSystemException.class already added, skipping
[INFO] javax/resource/spi/IllegalStateException.class already added, skipping
[INFO] javax/resource/spi/InvalidPropertyException.class already added, skipping
[INFO] javax/resource/spi/LazyAssociatableConnectionManager.class already added, skipping
[INFO] javax/resource/spi/LazyEnlistableConnectionManager.class already added, skipping
[INFO] javax/resource/spi/LazyEnlistableManagedConnection.class already added, skipping
[INFO] javax/resource/spi/LocalTransaction.class already added, skipping
[INFO] javax/resource/spi/LocalTransactionException.class already added, skipping
[INFO] javax/resource/spi/ManagedConnection.class already added, skipping
[INFO] javax/resource/spi/ManagedConnectionFactory.class already added, skipping
[INFO] javax/resource/spi/ManagedConnectionMetaData.class already added, skipping
[INFO] javax/resource/spi/ResourceAdapter.class already added, skipping
[INFO] javax/resource/spi/ResourceAdapterAssociation.class already added, skipping
[INFO] javax/resource/spi/ResourceAdapterInternalException.class already added, skipping
[INFO] javax/resource/spi/ResourceAllocationException.class already added, skipping
[INFO] javax/resource/spi/RetryableException.class already added, skipping
[INFO] javax/resource/spi/RetryableUnavailableException.class already added, skipping
[INFO] javax/resource/spi/SecurityException.class already added, skipping
[INFO] javax/resource/spi/SecurityPermission.class already added, skipping
[INFO] javax/resource/spi/SharingViolationException.class already added, skipping
[INFO] javax/resource/spi/TransactionSupport$TransactionSupportLevel.class already added, skipping
[INFO] javax/resource/spi/TransactionSupport.class already added, skipping
[INFO] javax/resource/spi/UnavailableException.class already added, skipping
[INFO] javax/resource/spi/ValidatingManagedConnectionFactory.class already added, skipping
[INFO] javax/resource/spi/XATerminator.class already added, skipping
[INFO] javax/resource/spi/endpoint/ already added, skipping
[INFO] javax/resource/spi/endpoint/MessageEndpoint.class already added, skipping
[INFO] javax/resource/spi/endpoint/MessageEndpointFactory.class already added, skipping
[INFO] javax/resource/spi/security/ already added, skipping
[INFO] javax/resource/spi/security/GenericCredential.class already added, skipping
[INFO] javax/resource/spi/security/PasswordCredential.class already added, skipping
[INFO] javax/resource/spi/work/ already added, skipping
[INFO] javax/resource/spi/work/DistributableWork.class already added, skipping
[INFO] javax/resource/spi/work/DistributableWorkManager.class already added, skipping
[INFO] javax/resource/spi/work/ExecutionContext.class already added, skipping
[INFO] javax/resource/spi/work/HintsContext.class already added, skipping
[INFO] javax/resource/spi/work/RetryableWorkRejectedException.class already added, skipping
[INFO] javax/resource/spi/work/SecurityContext.class already added, skipping
[INFO] javax/resource/spi/work/TransactionContext.class already added, skipping
[INFO] javax/resource/spi/work/Work.class already added, skipping
[INFO] javax/resource/spi/work/WorkAdapter.class already added, skipping
[INFO] javax/resource/spi/work/WorkCompletedException.class already added, skipping
[INFO] javax/resource/spi/work/WorkContext.class already added, skipping
[INFO] javax/resource/spi/work/WorkContextErrorCodes.class already added, skipping
[INFO] javax/resource/spi/work/WorkContextLifecycleListener.class already added, skipping
[INFO] javax/resource/spi/work/WorkContextProvider.class already added, skipping
[INFO] javax/resource/spi/work/WorkEvent.class already added, skipping
[INFO] javax/resource/spi/work/WorkException.class already added, skipping
[INFO] javax/resource/spi/work/WorkListener.class already added, skipping
[INFO] javax/resource/spi/work/WorkManager.class already added, skipping
[INFO] javax/resource/spi/work/WorkRejectedException.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] javax/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/geronimo/ already added, skipping
[INFO] org/apache/geronimo/osgi/ already added, skipping
[INFO] org/apache/geronimo/osgi/locator/ already added, skipping
[INFO] org/apache/geronimo/osgi/locator/Activator.class already added, skipping
[INFO] org/apache/geronimo/osgi/locator/ProviderLocator.class already added, skipping
[INFO] META-INF/maven/org.apache.geronimo.specs/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.xbean/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/xbean/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] javax/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/slf4j/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.slf4j/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/openwebbeans/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/openwebbeans/openwebbeans.properties already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/openwebbeans/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] org/apache/webbeans/ee/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/openwebbeans/openwebbeans.properties already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/openwebbeans/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/webbeans/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/openwebbeans/openwebbeans.properties already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.openwebbeans/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/LICENSE.txt already added, skipping
[INFO] META-INF/NOTICE.txt already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/commons/ already added, skipping
[INFO] org/apache/commons/collections/ already added, skipping
[INFO] org/apache/commons/collections/ArrayStack.class already added, skipping
[INFO] org/apache/commons/collections/Buffer.class already added, skipping
[INFO] org/apache/commons/collections/BufferUnderflowException.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$1.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$CollectionView$CollectionViewIterator.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$CollectionView.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$EntrySet.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$KeySet.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap$Values.class already added, skipping
[INFO] org/apache/commons/collections/FastHashMap.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/org.apache.bval/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] org/ already added, skipping
[INFO] org/apache/ already added, skipping
[INFO] org/apache/bval/ already added, skipping
[INFO] META-INF/DEPENDENCIES already added, skipping
[INFO] META-INF/DISCLAIMER already added, skipping
[INFO] META-INF/LICENSE already added, skipping
[INFO] META-INF/NOTICE already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/ejb-jar.xml already added, skipping
[INFO] META-INF/maven/ already added, skipping
[WARNING] Configuration options: 'appendAssemblyId' is set to false, and 'classifier' is missing.
Instead of attaching the assembly file: /Users/dblevins/examples/webapps/moviefun/monitor/target/moviefun-monitor-1.0.jar, it will become the file for main project artifact.
NOTE: If multiple descriptors or descriptor-formats are provided for this project, the value of this file will be non-deterministic!
[WARNING] Replacing pre-existing project main-artifact file: /Users/dblevins/examples/webapps/moviefun/monitor/target/archive-tmp/moviefun-monitor-1.0.jar
with assembly file: /Users/dblevins/examples/webapps/moviefun/monitor/target/moviefun-monitor-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ moviefun-monitor ---
[INFO] Installing /Users/dblevins/examples/webapps/moviefun/monitor/target/moviefun-monitor-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/moviefun-monitor/1.0/moviefun-monitor-1.0.jar
[INFO] Installing /Users/dblevins/examples/webapps/moviefun/monitor/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/moviefun-monitor/1.0/moviefun-monitor-1.0.pom
[INFO] 
[INFO] --- maven-dependency-plugin:2.1:copy-dependencies (default) @ moviefun-monitor ---
[INFO] Copying avalon-framework-4.1.3.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/avalon-framework-4.1.3.jar
[INFO] Copying commons-beanutils-core-1.8.3.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-beanutils-core-1.8.3.jar
[INFO] Copying commons-cli-1.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-cli-1.2.jar
[INFO] Copying commons-collections-3.2.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-collections-3.2.1.jar
[INFO] Copying commons-dbcp-1.4.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-dbcp-1.4.jar
[INFO] Copying commons-lang-2.6.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-lang-2.6.jar
[INFO] Copying commons-logging-1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-logging-1.1.jar
[INFO] Copying commons-logging-api-1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-logging-api-1.1.jar
[INFO] Copying commons-net-2.0.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-net-2.0.jar
[INFO] Copying commons-pool-1.5.6.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/commons-pool-1.5.6.jar
[INFO] Copying hsqldb-1.8.0.10.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/hsqldb-1.8.0.10.jar
[INFO] Copying javassist-3.12.0.GA.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/javassist-3.12.0.GA.jar
[INFO] Copying servlet-api-2.3.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/servlet-api-2.3.jar
[INFO] Copying junit-4.8.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/junit-4.8.1.jar
[INFO] Copying log4j-1.2.16.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/log4j-1.2.16.jar
[INFO] Copying logkit-1.0.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/logkit-1.0.1.jar
[INFO] Copying scannotation-1.0.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/scannotation-1.0.2.jar
[INFO] Copying serp-1.13.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/serp-1.13.1.jar
[INFO] Copying activeio-core-3.1.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/activeio-core-3.1.2.jar
[INFO] Copying activemq-core-5.4.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/activemq-core-5.4.2.jar
[INFO] Copying activemq-ra-5.4.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/activemq-ra-5.4.2.jar
[INFO] Copying kahadb-5.4.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/kahadb-5.4.2.jar
[INFO] Copying activemq-protobuf-1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/activemq-protobuf-1.1.jar
[INFO] Copying bval-core-0.3-incubating.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/bval-core-0.3-incubating.jar
[INFO] Copying geronimo-connector-3.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/geronimo-connector-3.1.1.jar
[INFO] Copying geronimo-transaction-3.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/geronimo-transaction-3.1.1.jar
[INFO] Copying geronimo-javamail_1.4_mail-1.8.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/geronimo-javamail_1.4_mail-1.8.2.jar
[INFO] Copying geronimo-j2ee-connector_1.6_spec-1.0.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/geronimo-j2ee-connector_1.6_spec-1.0.jar
[INFO] Copying geronimo-j2ee-management_1.1_spec-1.0.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/geronimo-j2ee-management_1.1_spec-1.0.1.jar
[INFO] Copying javaee-api-6.0-2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/javaee-api-6.0-2.jar
[INFO] Copying mbean-annotation-api-4.0.0-beta-1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/mbean-annotation-api-4.0.0-beta-1.jar
[INFO] Copying openejb-api-4.0.0-beta-1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openejb-api-4.0.0-beta-1.jar
[INFO] Copying openejb-core-4.0.0-beta-1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openejb-core-4.0.0-beta-1.jar
[INFO] Copying openejb-javaagent-4.0.0-beta-1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openejb-javaagent-4.0.0-beta-1.jar
[INFO] Copying openejb-jee-4.0.0-beta-1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openejb-jee-4.0.0-beta-1.jar
[INFO] Copying openejb-loader-4.0.0-beta-1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openejb-loader-4.0.0-beta-1.jar
[INFO] Copying openejb-bval-0.3.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openejb-bval-0.3.jar
[INFO] Copying openjpa-2.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openjpa-2.1.1.jar
[INFO] Copying openwebbeans-ee-1.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openwebbeans-ee-1.1.1.jar
[INFO] Copying openwebbeans-ee-common-1.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openwebbeans-ee-common-1.1.1.jar
[INFO] Copying openwebbeans-ejb-1.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openwebbeans-ejb-1.1.1.jar
[INFO] Copying openwebbeans-impl-1.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openwebbeans-impl-1.1.1.jar
[INFO] Copying openwebbeans-spi-1.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openwebbeans-spi-1.1.1.jar
[INFO] Copying openwebbeans-web-1.1.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/openwebbeans-web-1.1.1.jar
[INFO] Copying xbean-asm-shaded-3.8.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/xbean-asm-shaded-3.8.jar
[INFO] Copying xbean-bundleutils-3.8.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/xbean-bundleutils-3.8.jar
[INFO] Copying xbean-finder-shaded-3.8.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/xbean-finder-shaded-3.8.jar
[INFO] Copying xbean-naming-3.8.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/xbean-naming-3.8.jar
[INFO] Copying xbean-reflect-3.8.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/xbean-reflect-3.8.jar
[INFO] Copying swizzle-stream-1.0.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/swizzle-stream-1.0.2.jar
[INFO] Copying howl-1.0.1-1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/howl-1.0.1-1.jar
[INFO] Copying quartz-1.8.5.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/quartz-1.8.5.jar
[INFO] Copying slf4j-api-1.6.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/slf4j-api-1.6.1.jar
[INFO] Copying slf4j-log4j12-1.6.1.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/slf4j-log4j12-1.6.1.jar
[INFO] Copying wsdl4j-1.6.2.jar to /Users/dblevins/examples/webapps/moviefun/monitor/target/lib/wsdl4j-1.6.2.jar
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] OpenEJB :: Web Examples :: Moviefun ............... SUCCESS [0.633s]
[INFO] OpenEJB :: Web Examples :: Moviefun :: Interfaces . SUCCESS [1.301s]
[INFO] OpenEJB :: Web Examples :: Moviefun :: App ........ SUCCESS [7.076s]
[INFO] OpenEJB :: Web Examples :: Moviefun :: Monitor .... SUCCESS [11.191s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 20.354s
[INFO] Finished at: Fri Oct 28 17:04:42 PDT 2011
[INFO] Final Memory: 20M/81M
[INFO] ------------------------------------------------------------------------
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.EJB;
    import javax.servlet.ServletException;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import javax.servlet.http.HttpSession;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.ListIterator;
    
    /**
     * @version $Revision$ $Date$
     */
    public class ActionServlet extends HttpServlet {
    
        @EJB(name = "movies")
        private Movies moviesBean;
    
    	@Override
    	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		process(request, response);
    	}
    
    	@Override
    	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		process(request, response);
    	}
    
    	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		HttpSession session = request.getSession();
    		
    	    List<Movie> movies = null;
    	    ListIterator<Movie> listIterator = null;
    	    int display = 5;
    	    
    	    String action = request.getParameter("action");
    
    	    
    		if ("Add".equals(action)) {
    
    	        String title = request.getParameter("title");
    	        String director = request.getParameter("director");
    	        String genre = request.getParameter("genre");
    	        int rating = Integer.parseInt(request.getParameter("rating"));
    	        int year = Integer.parseInt(request.getParameter("year"));
    
    	        Movie movie = new Movie(title, director, genre, rating, year);
    
    	        moviesBean.addMovie(movie);
    
    	    } else if ("Remove".equals(action)) {
    
    	        String[] ids = request.getParameterValues("id");
    	        for (String id : ids) {
    	            moviesBean.deleteMovieId(new Long(id));
    	        }
    
    	    } else if (">>".equals(action)) {
    
    	        movies = (List) session.getAttribute("movies.collection");
    	        listIterator = (ListIterator) session.getAttribute("movies.iterator");
    
    	    } else if ("<<".equals(action)) {
    
    	        movies = (List) session.getAttribute("movies.collection");
    	        listIterator = (ListIterator) session.getAttribute("movies.iterator");
    	        for (int i = display * 2; i > 0 && listIterator.hasPrevious(); i--) {
    	            listIterator.previous(); // backup
    	        }
    
    	    } else if ("findByTitle".equals(action)) {
    
    	        movies = moviesBean.findByTitle(request.getParameter("key"));
    
    	    } else if ("findByDirector".equals(action)) {
    
    	        movies = moviesBean.findByDirector(request.getParameter("key"));
    
    	    } else if ("findByGenre".equals(action)) {
    
    	        movies = moviesBean.findByGenre(request.getParameter("key"));
    	    }
    
    	    if (movies == null) {
    	        try {
    	            movies = moviesBean.getMovies();
    	        } catch (Throwable e) {
    	            // We must not have run setup yet
    	            response.sendRedirect("setup.jsp");
    	            return;
    	        }
    	    }
    
    	    if (listIterator == null) {
    	        listIterator = movies.listIterator();
    	    }
    
    	    session.setAttribute("movies.collection", movies);
    	    session.setAttribute("movies.iterator", listIterator);
    	    
    	    List<Movie> moviesToShow = new ArrayList<Movie>();
    	    
    	    boolean hasPrevious = listIterator.hasPrevious();
    	    
    	    int start = listIterator.nextIndex();
            
    	    for (int i=display; i > 0 && listIterator.hasNext(); i-- ) {
    	    	    Movie movie = (Movie) listIterator.next();
    	    	    moviesToShow.add(movie);
    	    }
            
    	    boolean hasNext = listIterator.hasNext();
    			
    	    int end = listIterator.nextIndex();
    	    request.setAttribute("movies", moviesToShow);
    	    request.setAttribute("start", start);
    	    request.setAttribute("end", end);
    	    request.setAttribute("total", movies.size());
    	    request.setAttribute("display", display);
    	    request.setAttribute("hasNext", hasNext);
    	    request.setAttribute("hasPrev", hasPrevious);
    		
    	    request.getRequestDispatcher("WEB-INF/moviefun.jsp").forward(request, response);
    	}
    
    }
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import org.superbiz.moviefun.util.JsfUtil;
    import org.superbiz.moviefun.util.PaginationHelper;
    
    import javax.ejb.EJB;
    import javax.faces.bean.ManagedBean;
    import javax.faces.bean.SessionScoped;
    import javax.faces.component.UIComponent;
    import javax.faces.context.FacesContext;
    import javax.faces.convert.Converter;
    import javax.faces.convert.FacesConverter;
    import javax.faces.model.DataModel;
    import javax.faces.model.ListDataModel;
    import javax.faces.model.SelectItem;
    import java.io.Serializable;
    import java.util.ResourceBundle;
    
    @ManagedBean(name = "movieController")
    @SessionScoped
    public class MovieController implements Serializable {
    
    
        private Movie current;
        private DataModel items = null;
        @EJB
        private MoviesImpl ejbFacade;
        private PaginationHelper pagination;
        private int selectedItemIndex;
    
        public MovieController() {
        }
    
        public Movie getSelected() {
            if (current == null) {
                current = new Movie();
                selectedItemIndex = -1;
            }
            return current;
        }
    
        private MoviesImpl getFacade() {
            return ejbFacade;
        }
    
        public PaginationHelper getPagination() {
            if (pagination == null) {
                pagination = new PaginationHelper(10) {
    
                    @Override
                    public int getItemsCount() {
                        return getFacade().count();
                    }
    
                    @Override
                    public DataModel createPageDataModel() {
                        return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                    }
                };
            }
            return pagination;
        }
    
        public String prepareList() {
            recreateModel();
            return "List";
        }
    
        public String prepareView() {
            current = (Movie) getItems().getRowData();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
            return "View";
        }
    
        public String prepareCreate() {
            current = new Movie();
            selectedItemIndex = -1;
            return "Create";
        }
    
        public String create() {
            try {
                getFacade().addMovie(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MovieCreated"));
                return prepareCreate();
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                return null;
            }
        }
    
        public String prepareEdit() {
            current = (Movie) getItems().getRowData();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
            return "Edit";
        }
    
        public String update() {
            try {
                getFacade().editMovie(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MovieUpdated"));
                return "View";
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                return null;
            }
        }
    
        public String destroy() {
            current = (Movie) getItems().getRowData();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
            performDestroy();
            recreateModel();
            return "List";
        }
    
        public String destroyAndView() {
            performDestroy();
            recreateModel();
            updateCurrentItem();
            if (selectedItemIndex >= 0) {
                return "View";
            } else {
                // all items were removed - go back to list
                recreateModel();
                return "List";
            }
        }
    
        private void performDestroy() {
            try {
                getFacade().deleteMovieId(current.getId());
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MovieDeleted"));
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    
        private void updateCurrentItem() {
            int count = getFacade().count();
            if (selectedItemIndex >= count) {
                // selected index cannot be bigger than number of items:
                selectedItemIndex = count - 1;
                // go to previous page if last page disappeared:
                if (pagination.getPageFirstItem() >= count) {
                    pagination.previousPage();
                }
            }
            if (selectedItemIndex >= 0) {
                current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
            }
        }
    
        public DataModel getItems() {
            if (items == null) {
                items = getPagination().createPageDataModel();
            }
            return items;
        }
    
        private void recreateModel() {
            items = null;
        }
    
        public String next() {
            getPagination().nextPage();
            recreateModel();
            return "List";
        }
    
        public String previous() {
            getPagination().previousPage();
            recreateModel();
            return "List";
        }
    
        public SelectItem[] getItemsAvailableSelectMany() {
            return JsfUtil.getSelectItems(ejbFacade.getMovies(), false);
        }
    
        public SelectItem[] getItemsAvailableSelectOne() {
            return JsfUtil.getSelectItems(ejbFacade.getMovies(), true);
        }
    
        @FacesConverter(forClass = Movie.class)
        public static class MovieControllerConverter implements Converter {
    
            public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
                if (value == null || value.length() == 0) {
                    return null;
                }
                MovieController controller = (MovieController) facesContext.getApplication().getELResolver().
                        getValue(facesContext.getELContext(), null, "movieController");
                return controller.ejbFacade.find(getKey(value));
            }
    
            long getKey(String value) {
                long key;
                key = Long.parseLong(value);
                return key;
            }
    
            String getStringKey(long value) {
                StringBuffer sb = new StringBuffer();
                sb.append(value);
                return sb.toString();
            }
    
            public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
                if (object == null) {
                    return null;
                }
                if (object instanceof Movie) {
                    Movie o = (Movie) object;
                    return getStringKey(o.getId());
                } else {
                    throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + MovieController.class.getName());
                }
            }
    
        }
    
    }
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.EJB;
    import javax.ejb.LocalBean;
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.TypedQuery;
    import javax.persistence.criteria.CriteriaBuilder;
    import javax.persistence.criteria.CriteriaQuery;
    import javax.persistence.criteria.Path;
    import javax.persistence.criteria.Predicate;
    import javax.persistence.criteria.Root;
    import javax.persistence.metamodel.EntityType;
    import java.util.List;
    
    @LocalBean
    @Stateless(name = "Movies")
    @WebService(portName = "MoviesPort",
            serviceName = "MoviesWebService",
            targetNamespace = "http://superbiz.org/wsdl")
    public class MoviesImpl implements Movies, MoviesRemote {
    
        @EJB
        private Notifier notifier;
    
        @PersistenceContext(unitName = "movie-unit")
        private EntityManager entityManager;
    
        @Override
        public Movie find(Long id) {
            return entityManager.find(Movie.class, id);
        }
    
        @Override
        public void addMovie(Movie movie) {
            entityManager.persist(movie);
        }
    
        @Override
        public void editMovie(Movie movie) {
            entityManager.merge(movie);
        }
    
        @Override
        public void deleteMovie(Movie movie) {
            entityManager.remove(movie);
            notifier.notify("Deleted Movie \"" + movie.getTitle() + "\" (" + movie.getYear() + ")");
        }
    
        @Override
        public void deleteMovieId(long id) {
            Movie movie = entityManager.find(Movie.class, id);
            deleteMovie(movie);
        }
    
        @Override
        public List<Movie> getMovies() {
            CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
            cq.select(cq.from(Movie.class));
            return entityManager.createQuery(cq).getResultList();
        }
    
        @Override
        public List<Movie> findByTitle(String title) {
            return findByStringField("title", title);
        }
    
        @Override
        public List<Movie> findByGenre(String genre) {
            return findByStringField("genre", genre);
        }
    
        @Override
        public List<Movie> findByDirector(String director) {
            return findByStringField("director", director);
        }
    
        private List<Movie> findByStringField(String fieldname, String param) {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
            Root<Movie> root = query.from(Movie.class);
            EntityType<Movie> type = entityManager.getMetamodel().entity(Movie.class);
    
            Path<String> path = root.get(type.getDeclaredSingularAttribute(fieldname, String.class));
            Predicate condition = builder.like(path, "%" + param + "%");
    
            query.where(condition);
    
            return entityManager.createQuery(query).getResultList();
        }
    
        @Override
        public List<Movie> findRange(int[] range) {
            CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
            cq.select(cq.from(Movie.class));
            TypedQuery<Movie> q = entityManager.createQuery(cq);
            q.setMaxResults(range[1] - range[0]);
            q.setFirstResult(range[0]);
            return q.getResultList();
        }
    
        @Override
        public int count() {
            CriteriaQuery<Long> cq = entityManager.getCriteriaBuilder().createQuery(Long.class);
            Root<Movie> rt = cq.from(Movie.class);
            cq.select(entityManager.getCriteriaBuilder().count(rt));
            TypedQuery<Long> q = entityManager.createQuery(cq);
            return (q.getSingleResult()).intValue();
        }
    
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    /**
     * @version $Revision$ $Date$
     */
    public interface Notifier {
        void notify(String message);
    }
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.annotation.Resource;
    import javax.ejb.Stateless;
    import javax.jms.Connection;
    import javax.jms.ConnectionFactory;
    import javax.jms.DeliveryMode;
    import javax.jms.JMSException;
    import javax.jms.MessageProducer;
    import javax.jms.Session;
    import javax.jms.TextMessage;
    import javax.jms.Topic;
    
    @Stateless
    public class NotifierImpl implements Notifier {
    
        @Resource
        private ConnectionFactory connectionFactory;
    
        @Resource(name = "notifications")
        private Topic notificationsTopic;
    
        public void notify(String message) {
            try {
                Connection connection = null;
                Session session = null;
    
                try {
                    connection = connectionFactory.createConnection();
                    connection.start();
    
                    // Create a Session
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
                    // Create a MessageProducer from the Session to the Topic or Queue
                    MessageProducer producer = session.createProducer(notificationsTopic);
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    
                    // Create a message
                    TextMessage textMessage = session.createTextMessage(message);
    
                    // Tell the producer to send the message
                    producer.send(textMessage);
                } finally {
                    // Clean up
                    if (session != null) session.close();
                    if (connection != null) connection.close();
                }
            } catch (JMSException e) {
                throw new IllegalStateException(e);
            }
    
        }
    
    }
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun.util;
    
    import javax.faces.application.FacesMessage;
    import javax.faces.component.UIComponent;
    import javax.faces.context.FacesContext;
    import javax.faces.convert.Converter;
    import javax.faces.model.SelectItem;
    import java.util.List;
    
    public class JsfUtil {
    
        public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
            int size = selectOne ? entities.size() + 1 : entities.size();
            SelectItem[] items = new SelectItem[size];
            int i = 0;
            if (selectOne) {
                items[0] = new SelectItem("", "---");
                i++;
            }
            for (Object x : entities) {
                items[i++] = new SelectItem(x, x.toString());
            }
            return items;
        }
    
        public static void addErrorMessage(Exception ex, String defaultMsg) {
            String msg = ex.getLocalizedMessage();
            if (msg != null && msg.length() > 0) {
                addErrorMessage(msg);
            } else {
                addErrorMessage(defaultMsg);
            }
        }
    
        public static void addErrorMessages(List<String> messages) {
            for (String message : messages) {
                addErrorMessage(message);
            }
        }
    
        public static void addErrorMessage(String msg) {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
            FacesContext.getCurrentInstance().addMessage(null, facesMsg);
        }
    
        public static void addSuccessMessage(String msg) {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
            FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
        }
    
        public static String getRequestParameter(String key) {
            return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
        }
    
        public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
            String theId = JsfUtil.getRequestParameter(requestParameterName);
            return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
        }
    
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun.util;
    
    import javax.faces.model.DataModel;
    
    public abstract class PaginationHelper {
    
        private int pageSize;
        private int page;
    
        public PaginationHelper(int pageSize) {
            this.pageSize = pageSize;
        }
    
        public abstract int getItemsCount();
    
        public abstract DataModel createPageDataModel();
    
        public int getPageFirstItem() {
            return page * pageSize;
        }
    
        public int getPageLastItem() {
            int i = getPageFirstItem() + pageSize - 1;
            int count = getItemsCount() - 1;
            if (i > count) {
                i = count;
            }
            if (i < 0) {
                i = 0;
            }
            return i;
        }
    
        public boolean isHasNextPage() {
            return (page + 1) * pageSize + 1 <= getItemsCount();
        }
    
        public void nextPage() {
            if (isHasNextPage()) {
                page++;
            }
        }
    
        public boolean isHasPreviousPage() {
            return page > 0;
        }
    
        public void previousPage() {
            if (isHasPreviousPage()) {
                page--;
            }
        }
    
        public int getPageSize() {
            return pageSize;
        }
    
    }
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import com.gargoylesoftware.htmlunit.WebClient;
    import com.gargoylesoftware.htmlunit.html.DomNodeList;
    import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
    import com.gargoylesoftware.htmlunit.html.HtmlElement;
    import com.gargoylesoftware.htmlunit.html.HtmlPage;
    import org.junit.Test;
    
    import java.util.Iterator;
    
    import static org.junit.Assert.assertTrue;
    
    public class MoviesIT {
    
        @Test
        public void testShouldMakeSureWebappIsWorking() throws Exception {
            WebClient webClient = new WebClient();
            HtmlPage page = webClient.getPage("http://localhost:9999/moviefun/setup.jsp");
    
            assertMoviesPresent(page);
    
            page = webClient.getPage("http://localhost:9999/moviefun/faces/movie/List.xhtml");
    
            assertMoviesPresent(page);
            webClient.closeAllWindows();
        }
    
        private void assertMoviesPresent(HtmlPage page) {
            String pageAsText = page.asText();
            assertTrue(pageAsText.contains("Wedding Crashers"));
            assertTrue(pageAsText.contains("Starsky & Hutch"));
            assertTrue(pageAsText.contains("Shanghai Knights"));
            assertTrue(pageAsText.contains("I-Spy"));
            assertTrue(pageAsText.contains("The Royal Tenenbaums"));
            assertTrue(pageAsText.contains("Zoolander"));
            assertTrue(pageAsText.contains("Shanghai Noon"));
        }
    
        private void clickOnLink(HtmlPage page, String lookFor) throws Exception {
            DomNodeList<HtmlElement> links = page.getElementsByTagName("a");
            Iterator<HtmlElement> iterator = links.iterator();
            while (iterator.hasNext()) {
                HtmlAnchor anchor = (HtmlAnchor) iterator.next();
    
                if (lookFor.equals(anchor.getTextContent())) {
                    anchor.click();
                    break;
                }
            }
        }
    
    }
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import junit.framework.TestCase;
    import org.apache.openejb.api.LocalClient;
    
    import javax.annotation.Resource;
    import javax.ejb.EJB;
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.transaction.UserTransaction;
    import java.util.List;
    import java.util.Properties;
    
    @LocalClient
    public class MoviesTest extends TestCase {
    
        @EJB
        private Movies movies;
    
        @Resource
        private UserTransaction userTransaction;
    
        @PersistenceContext
        private EntityManager entityManager;
    
        public void setUp() throws Exception {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
    
            InitialContext initialContext = new InitialContext(p);
    
            // Here's the fun part
            initialContext.bind("inject", this);
        }
    
        public void test() throws Exception {
    
            userTransaction.begin();
    
            try {
                entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
                entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));
    
                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());
    
                for (Movie movie : list) {
                    movies.deleteMovie(movie);
                }
    
                assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
    
            } finally {
                userTransaction.commit();
            }
        }
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;
    import java.io.Serializable;
    
    @Entity
    public class Movie implements Serializable {
    
        private static final long serialVersionUID = 1L;
    
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;
    
        private String director;
        private String title;
        private int year;
        private String genre;
        private int rating;
    
    
        public Movie() {
        }
    
        public Movie(String title, String director, String genre, int rating, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
            this.genre = genre;
            this.rating = rating;
        }
    
        public Movie(String director, String title, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
        }
    
        public long getId() {
            return id;
        }
    
        public void setId(long id) {
            this.id = id;
        }
    
        public String getDirector() {
            return director;
        }
    
        public void setDirector(String director) {
            this.director = director;
        }
    
        public String getTitle() {
            return title;
        }
    
        public void setTitle(String title) {
            this.title = title;
        }
    
        public int getYear() {
            return year;
        }
    
        public void setYear(int year) {
            this.year = year;
        }
    
        public String getGenre() {
            return genre;
        }
    
        public void setGenre(String genre) {
            this.genre = genre;
        }
    
        public int getRating() {
            return rating;
        }
    
        public void setRating(int rating) {
            this.rating = rating;
        }
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.Local;
    import java.util.List;
    
    @Local
    public interface Movies {
        public int count();
    
        public List<Movie> findRange(int[] range);
    
        public List<Movie> findByDirector(String director);
    
        public List<Movie> findByGenre(String genre);
    
        public List<Movie> findByTitle(String title);
    
        public List<Movie> getMovies();
    
        public void deleteMovieId(long id);
    
        public void deleteMovie(Movie movie);
    
        public void editMovie(Movie movie);
    
        public void addMovie(Movie movie);
    
        public Movie find(Long id);
    }
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.Remote;
    import java.util.List;
    
    @Remote
    public interface MoviesRemote {
        public int count();
    
        public List<Movie> findRange(int[] range);
    
        public List<Movie> findByDirector(String director);
    
        public List<Movie> findByGenre(String genre);
    
        public List<Movie> findByTitle(String title);
    
        public List<Movie> getMovies();
    
        public void deleteMovieId(long id);
    
        public void deleteMovie(Movie movie);
    
        public void editMovie(Movie movie);
    
        public void addMovie(Movie movie);
    
        public Movie find(Long id);
    }
    /*
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
    package org.superbiz.moviefun;
    
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.net.MalformedURLException;
    import java.net.URL;
    
    public class NotificationMonitor {
        private static TrayIcon trayIcon;
    
        public static void main(String[] args) throws NamingException, InterruptedException, AWTException, MalformedURLException {
            addSystemTrayIcon();
    
            // Boot the embedded EJB Container 
            new InitialContext();
    
            System.out.println("Starting monitor...");
        }
    
        private static void addSystemTrayIcon() throws AWTException, MalformedURLException {
            SystemTray tray = SystemTray.getSystemTray();
    
            URL moviepng = NotificationMonitor.class.getClassLoader().getResource("movie.png");
            Image image = Toolkit.getDefaultToolkit().getImage(moviepng);
    
            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting monitor...");
                    System.exit(0);
                }
            };
    
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
    
            trayIcon = new TrayIcon(image, "Notification Monitor", popup);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
    
    
        }
    
        public static void showAlert(String message) {
            synchronized (trayIcon) {
                trayIcon.displayMessage("Alert received", message, TrayIcon.MessageType.WARNING);
            }
        }
    }/*
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
    package org.superbiz.moviefun;
    
    import javax.ejb.ActivationConfigProperty;
    import javax.ejb.MessageDriven;
    import javax.jms.JMSException;
    import javax.jms.Message;
    import javax.jms.MessageListener;
    import javax.jms.TextMessage;
    
    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "notifications")})
    public class NotificationsBean implements MessageListener {
    
        public void onMessage(Message message) {
            try {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
    
                System.out.println("");
                System.out.println("====================================");
                System.out.println("Notification received: " + text);
                System.out.println("====================================");
                System.out.println("");
    
                NotificationMonitor.showAlert(text);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }