[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Troubleshooting 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ troubleshooting ---
[INFO] Deleting /Users/dblevins/examples/troubleshooting/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ troubleshooting ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ troubleshooting ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/troubleshooting/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ troubleshooting ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/troubleshooting/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ troubleshooting ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/troubleshooting/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ troubleshooting ---
[INFO] Surefire report directory: /Users/dblevins/examples/troubleshooting/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.troubleshooting.MoviesTest
2011-10-28 16:59:47,149 - DEBUG - Using default 'openejb.nobanner=true'
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
2011-10-28 16:59:47,150 - INFO  - openejb.home = /Users/dblevins/examples/troubleshooting
2011-10-28 16:59:47,150 - INFO  - openejb.base = /Users/dblevins/examples/troubleshooting
2011-10-28 16:59:47,150 - DEBUG - Using default 'openejb.assembler=org.apache.openejb.assembler.classic.Assembler'
2011-10-28 16:59:47,151 - DEBUG - Instantiating assembler class org.apache.openejb.assembler.classic.Assembler
2011-10-28 16:59:47,187 - DEBUG - Using default 'openejb.jndiname.failoncollision=true'
2011-10-28 16:59:47,187 - INFO  - Using 'javax.ejb.embeddable.EJBContainer=true'
2011-10-28 16:59:47,191 - DEBUG - Using default 'openejb.configurator=org.apache.openejb.config.ConfigurationFactory'
2011-10-28 16:59:47,260 - DEBUG - Using default 'openejb.validation.skip=false'
2011-10-28 16:59:47,261 - DEBUG - Using default 'openejb.deploymentId.format={ejbName}'
2011-10-28 16:59:47,261 - DEBUG - Using default 'openejb.debuggable-vm-hackery=false'
2011-10-28 16:59:47,261 - DEBUG - Using default 'openejb.webservices.enabled=true'
2011-10-28 16:59:47,265 - DEBUG - Using default 'openejb.vendor.config=ALL'  Possible values are: geronimo, glassfish, jboss, weblogic or NONE or ALL
2011-10-28 16:59:47,283 - DEBUG - Using default 'openejb.provider.default=org.apache.openejb.embedded'
2011-10-28 16:59:47,328 - INFO  - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
2011-10-28 16:59:47,332 - INFO  - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
2011-10-28 16:59:47,335 - INFO  - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
2011-10-28 16:59:47,335 - DEBUG - Override [JdbcDriver=org.hsqldb.jdbcDriver]
2011-10-28 16:59:47,336 - DEBUG - Using default 'openejb.deployments.classpath=false'
2011-10-28 16:59:47,337 - INFO  - Creating TransactionManager(id=Default Transaction Manager)
2011-10-28 16:59:47,347 - DEBUG - defaultTransactionTimeoutSeconds=600
2011-10-28 16:59:47,347 - DEBUG - TxRecovery=false
2011-10-28 16:59:47,347 - DEBUG - bufferSizeKb=32
2011-10-28 16:59:47,347 - DEBUG - checksumEnabled=true
2011-10-28 16:59:47,347 - DEBUG - adler32Checksum=true
2011-10-28 16:59:47,347 - DEBUG - flushSleepTimeMilliseconds=50
2011-10-28 16:59:47,347 - DEBUG - logFileDir=txlog
2011-10-28 16:59:47,347 - DEBUG - logFileExt=log
2011-10-28 16:59:47,347 - DEBUG - logFileName=howl
2011-10-28 16:59:47,347 - DEBUG - maxBlocksPerFile=-1
2011-10-28 16:59:47,347 - DEBUG - maxBuffers=0
2011-10-28 16:59:47,347 - DEBUG - maxLogFiles=2
2011-10-28 16:59:47,347 - DEBUG - minBuffers=4
2011-10-28 16:59:47,348 - DEBUG - threadsWaitingForceThreshold=-1
2011-10-28 16:59:47,397 - DEBUG - createService.success
2011-10-28 16:59:47,397 - INFO  - Creating SecurityService(id=Default Security Service)
2011-10-28 16:59:47,397 - DEBUG - DefaultUser=guest
2011-10-28 16:59:47,426 - DEBUG - createService.success
2011-10-28 16:59:47,426 - INFO  - Creating Resource(id=movieDatabase)
2011-10-28 16:59:47,426 - DEBUG - Definition=
2011-10-28 16:59:47,426 - DEBUG - JtaManaged=true
2011-10-28 16:59:47,426 - DEBUG - JdbcDriver=org.hsqldb.jdbcDriver
2011-10-28 16:59:47,426 - DEBUG - JdbcUrl=jdbc:hsqldb:mem:hsqldb
2011-10-28 16:59:47,426 - DEBUG - UserName=sa
2011-10-28 16:59:47,426 - DEBUG - Password=
2011-10-28 16:59:47,426 - DEBUG - PasswordCipher=PlainText
2011-10-28 16:59:47,426 - DEBUG - ConnectionProperties=
2011-10-28 16:59:47,426 - DEBUG - DefaultAutoCommit=true
2011-10-28 16:59:47,426 - DEBUG - InitialSize=0
2011-10-28 16:59:47,426 - DEBUG - MaxActive=20
2011-10-28 16:59:47,426 - DEBUG - MaxIdle=20
2011-10-28 16:59:47,426 - DEBUG - MinIdle=0
2011-10-28 16:59:47,426 - DEBUG - MaxWait=-1
2011-10-28 16:59:47,426 - DEBUG - TestOnBorrow=true
2011-10-28 16:59:47,426 - DEBUG - TestOnReturn=false
2011-10-28 16:59:47,426 - DEBUG - TestWhileIdle=false
2011-10-28 16:59:47,426 - DEBUG - TimeBetweenEvictionRunsMillis=-1
2011-10-28 16:59:47,426 - DEBUG - NumTestsPerEvictionRun=3
2011-10-28 16:59:47,427 - DEBUG - MinEvictableIdleTimeMillis=1800000
2011-10-28 16:59:47,427 - DEBUG - PoolPreparedStatements=false
2011-10-28 16:59:47,427 - DEBUG - MaxOpenPreparedStatements=0
2011-10-28 16:59:47,427 - DEBUG - AccessToUnderlyingConnectionAllowed=false
2011-10-28 16:59:47,451 - DEBUG - createService.success
2011-10-28 16:59:47,452 - DEBUG - Containers        : 0
2011-10-28 16:59:47,455 - DEBUG - Deployments       : 0
2011-10-28 16:59:47,455 - DEBUG - SecurityService   : org.apache.openejb.core.security.SecurityServiceImpl
2011-10-28 16:59:47,455 - DEBUG - TransactionManager: org.apache.geronimo.transaction.manager.GeronimoTransactionManager
2011-10-28 16:59:47,455 - DEBUG - OpenEJB Container System ready.
2011-10-28 16:59:47,455 - DEBUG - Using default 'openejb.validation.skip=false'
2011-10-28 16:59:47,455 - DEBUG - Using default 'openejb.deploymentId.format={ejbName}'
2011-10-28 16:59:47,455 - DEBUG - Using default 'openejb.debuggable-vm-hackery=false'
2011-10-28 16:59:47,455 - DEBUG - Using default 'openejb.webservices.enabled=true'
2011-10-28 16:59:47,455 - DEBUG - Using default 'openejb.vendor.config=ALL'  Possible values are: geronimo, glassfish, jboss, weblogic or NONE or ALL
2011-10-28 16:59:47,457 - DEBUG - Using default 'openejb.deployments.classpath.include=.*'
2011-10-28 16:59:47,457 - DEBUG - Using default 'openejb.deployments.classpath.exclude='
2011-10-28 16:59:47,457 - DEBUG - Using default 'openejb.deployments.classpath.require.descriptor=client'  Possible values are: ejb, client or NONE or ALL
2011-10-28 16:59:47,457 - DEBUG - Using default 'openejb.deployments.classpath.filter.descriptors=false'
2011-10-28 16:59:47,457 - DEBUG - Using default 'openejb.deployments.classpath.filter.systemapps=true'
2011-10-28 16:59:47,482 - DEBUG - Inspecting classpath for applications: 5 urls.
2011-10-28 16:59:47,492 - INFO  - Found EjbModule in classpath: /Users/dblevins/examples/troubleshooting/target/classes
2011-10-28 16:59:47,615 - DEBUG - URLs after filtering: 55
2011-10-28 16:59:47,615 - DEBUG - Annotations path: file:/Users/dblevins/examples/troubleshooting/target/classes/
2011-10-28 16:59:47,615 - DEBUG - Annotations path: jar:file:/Users/dblevins/.m2/repository/org/apache/maven/surefire/surefire-api/2.7.2/surefire-api-2.7.2.jar!/
2011-10-28 16:59:47,615 - DEBUG - Annotations path: jar:file:/Users/dblevins/.m2/repository/org/apache/openejb/mbean-annotation-api/4.0.0-beta-1/mbean-annotation-api-4.0.0-beta-1.jar!/
2011-10-28 16:59:47,615 - DEBUG - Annotations path: jar:file:/Users/dblevins/.m2/repository/org/apache/maven/surefire/surefire-booter/2.7.2/surefire-booter-2.7.2.jar!/
2011-10-28 16:59:47,615 - DEBUG - Annotations path: file:/Users/dblevins/examples/troubleshooting/target/test-classes/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/bval/bval-core/0.3-incubating/bval-core-0.3-incubating.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/activemq-core/5.4.2/activemq-core-5.4.2.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-bundleutils/3.8/xbean-bundleutils-3.8.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/junit/junit/4.8.1/junit-4.8.1.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/net/sf/scannotation/scannotation/1.0.2/scannotation-1.0.2.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openejb/javaee-api/6.0-2/javaee-api-6.0-2.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-beanutils/commons-beanutils-core/1.8.3/commons-beanutils-core-1.8.3.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/avalon-framework/avalon-framework/4.1.3/avalon-framework-4.1.3.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-web/1.1.1/openwebbeans-web-1.1.1.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/wsdl4j/wsdl4j/1.6.2/wsdl4j-1.6.2.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/logkit/logkit/1.0.1/logkit-1.0.1.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/com/ibm/icu/icu4j/4.0.1/icu4j-4.0.1.jar!/
2011-10-28 16:59:47,616 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-asm-shaded/3.8/xbean-asm-shaded-3.8.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-ee-common/1.1.1/openwebbeans-ee-common-1.1.1.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-pool/commons-pool/1.5.6/commons-pool-1.5.6.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-logging/commons-logging-api/1.1/commons-logging-api-1.1.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-impl/1.1.1/openwebbeans-impl-1.1.1.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-finder-shaded/3.8/xbean-finder-shaded-3.8.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/specs/geronimo-j2ee-connector_1.6_spec/1.0/geronimo-j2ee-connector_1.6_spec-1.0.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/kahadb/5.4.2/kahadb-5.4.2.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/hsqldb/hsqldb/1.8.0.10/hsqldb-1.8.0.10.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/log4j/log4j/1.2.16/log4j-1.2.16.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/components/geronimo-connector/3.1.1/geronimo-connector-3.1.1.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/activemq-ra/5.4.2/activemq-ra-5.4.2.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/net/sourceforge/serp/serp/1.13.1/serp-1.13.1.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/javax/servlet/servlet-api/2.3/servlet-api-2.3.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/activeio-core/3.1.2/activeio-core-3.1.2.jar!/
2011-10-28 16:59:47,617 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/quartz-scheduler/quartz/1.8.5/quartz-1.8.5.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-ee/1.1.1/openwebbeans-ee-1.1.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-spi/1.1.1/openwebbeans-spi-1.1.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/codehaus/swizzle/swizzle-stream/1.0.2/swizzle-stream-1.0.2.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openjpa/openjpa/2.1.1/openjpa-2.1.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-naming/3.8/xbean-naming-3.8.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/components/geronimo-transaction/3.1.1/geronimo-transaction-3.1.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/javassist/javassist/3.12.0.GA/javassist-3.12.0.GA.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/objectweb/howl/howl/1.0.1-1/howl-1.0.1-1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-reflect/3.8/xbean-reflect-3.8.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-ejb/1.1.1/openwebbeans-ejb-1.1.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-logging/commons-logging/1.1/commons-logging-1.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-net/commons-net/2.0/commons-net-2.0.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/protobuf/activemq-protobuf/1.1/activemq-protobuf-1.1.jar!/
2011-10-28 16:59:47,618 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-dbcp/commons-dbcp/1.4/commons-dbcp-1.4.jar!/
2011-10-28 16:59:47,619 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/javamail/geronimo-javamail_1.4_mail/1.8.2/geronimo-javamail_1.4_mail-1.8.2.jar!/
2011-10-28 16:59:47,619 - DEBUG - Searched 5 classpath urls in 63 milliseconds.  Average 12 milliseconds per url.
2011-10-28 16:59:47,623 - INFO  - Beginning load: /Users/dblevins/examples/troubleshooting/target/classes
2011-10-28 16:59:47,626 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
2011-10-28 16:59:47,628 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
2011-10-28 16:59:47,689 - INFO  - Configuring enterprise application: /Users/dblevins/examples/troubleshooting
2011-10-28 16:59:47,690 - DEBUG - No ejb-jar.xml found assuming annotated beans present: /Users/dblevins/examples/troubleshooting, module: troubleshooting
2011-10-28 16:59:47,794 - DEBUG - Searching for annotated application exceptions (see OPENEJB-980)
2011-10-28 16:59:47,795 - DEBUG - Searching for annotated application exceptions (see OPENEJB-980)
2011-10-28 16:59:47,827 - WARN  - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
2011-10-28 16:59:47,828 - DEBUG - looking for annotated MBeans in 
2011-10-28 16:59:47,828 - DEBUG - registered 0 annotated MBeans in 
2011-10-28 16:59:47,858 - INFO  - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
2011-10-28 16:59:47,858 - INFO  - Auto-creating a container for bean Movies: Container(type=STATELESS, id=Default Stateless Container)
2011-10-28 16:59:47,858 - INFO  - Creating Container(id=Default Stateless Container)
2011-10-28 16:59:47,859 - DEBUG - AccessTimeout=30 seconds
2011-10-28 16:59:47,859 - DEBUG - MaxSize=10
2011-10-28 16:59:47,859 - DEBUG - MinSize=0
2011-10-28 16:59:47,859 - DEBUG - StrictPooling=true
2011-10-28 16:59:47,859 - DEBUG - MaxAge=0 hours
2011-10-28 16:59:47,859 - DEBUG - ReplaceAged=true
2011-10-28 16:59:47,859 - DEBUG - ReplaceFlushed=false
2011-10-28 16:59:47,859 - DEBUG - MaxAgeOffset=-1
2011-10-28 16:59:47,859 - DEBUG - IdleTimeout=0 minutes
2011-10-28 16:59:47,859 - DEBUG - GarbageCollection=false
2011-10-28 16:59:47,859 - DEBUG - SweepInterval=5 minutes
2011-10-28 16:59:47,859 - DEBUG - CallbackThreads=5
2011-10-28 16:59:47,859 - DEBUG - CloseTimeout=5 minutes
2011-10-28 16:59:47,875 - DEBUG - createService.success
2011-10-28 16:59:47,876 - INFO  - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
2011-10-28 16:59:47,876 - INFO  - Auto-creating a container for bean org.superbiz.troubleshooting.MoviesTest: Container(type=MANAGED, id=Default Managed Container)
2011-10-28 16:59:47,876 - INFO  - Creating Container(id=Default Managed Container)
2011-10-28 16:59:47,889 - DEBUG - createService.success
2011-10-28 16:59:47,889 - INFO  - Configuring PersistenceUnit(name=movie-unit)
2011-10-28 16:59:47,889 - DEBUG - raw <jta-data-source>movieDatabase</jta-datasource>
2011-10-28 16:59:47,889 - DEBUG - raw <non-jta-data-source>movieDatabaseUnmanaged</non-jta-datasource>
2011-10-28 16:59:47,889 - DEBUG - normalized <jta-data-source>movieDatabase</jta-datasource>
2011-10-28 16:59:47,889 - DEBUG - normalized <non-jta-data-source>movieDatabaseUnmanaged</non-jta-datasource>
2011-10-28 16:59:47,889 - DEBUG - Available DataSources
2011-10-28 16:59:47,889 - DEBUG - DataSource(name=movieDatabase, JtaManaged=true)
2011-10-28 16:59:47,890 - INFO  - Auto-creating a Resource with id 'movieDatabaseNonJta' of type 'DataSource for 'movie-unit'.
2011-10-28 16:59:47,890 - INFO  - Configuring Service(id=movieDatabaseNonJta, type=Resource, provider-id=movieDatabase)
2011-10-28 16:59:47,890 - INFO  - Creating Resource(id=movieDatabaseNonJta)
2011-10-28 16:59:47,890 - DEBUG - Definition=
2011-10-28 16:59:47,890 - DEBUG - JtaManaged=false
2011-10-28 16:59:47,890 - DEBUG - JdbcDriver=org.hsqldb.jdbcDriver
2011-10-28 16:59:47,890 - DEBUG - JdbcUrl=jdbc:hsqldb:mem:hsqldb
2011-10-28 16:59:47,890 - DEBUG - UserName=sa
2011-10-28 16:59:47,890 - DEBUG - Password=
2011-10-28 16:59:47,891 - DEBUG - PasswordCipher=PlainText
2011-10-28 16:59:47,891 - DEBUG - ConnectionProperties=
2011-10-28 16:59:47,891 - DEBUG - DefaultAutoCommit=true
2011-10-28 16:59:47,891 - DEBUG - InitialSize=0
2011-10-28 16:59:47,891 - DEBUG - MaxActive=20
2011-10-28 16:59:47,891 - DEBUG - MaxIdle=20
2011-10-28 16:59:47,891 - DEBUG - MinIdle=0
2011-10-28 16:59:47,891 - DEBUG - MaxWait=-1
2011-10-28 16:59:47,891 - DEBUG - TestOnBorrow=true
2011-10-28 16:59:47,891 - DEBUG - TestOnReturn=false
2011-10-28 16:59:47,891 - DEBUG - TestWhileIdle=false
2011-10-28 16:59:47,891 - DEBUG - TimeBetweenEvictionRunsMillis=-1
2011-10-28 16:59:47,891 - DEBUG - NumTestsPerEvictionRun=3
2011-10-28 16:59:47,891 - DEBUG - MinEvictableIdleTimeMillis=1800000
2011-10-28 16:59:47,891 - DEBUG - PoolPreparedStatements=false
2011-10-28 16:59:47,891 - DEBUG - MaxOpenPreparedStatements=0
2011-10-28 16:59:47,891 - DEBUG - AccessToUnderlyingConnectionAllowed=false
2011-10-28 16:59:47,895 - DEBUG - createService.success
2011-10-28 16:59:47,895 - INFO  - Adjusting PersistenceUnit movie-unit <non-jta-data-source> to Resource ID 'movieDatabaseNonJta' from 'movieDatabaseUnmanaged'
2011-10-28 16:59:47,895 - INFO  - Using 'openejb.descriptors.output=true'
2011-10-28 16:59:47,895 - INFO  - Using 'openejb.descriptors.output=true'
2011-10-28 16:59:48,215 - INFO  - Dumping Generated ejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/ejb-jar-4215130538870199197troubleshooting.xml
2011-10-28 16:59:48,230 - INFO  - Dumping Generated openejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/openejb-jar-3824744580112510968troubleshooting.xml
2011-10-28 16:59:48,230 - INFO  - Using 'openejb.descriptors.output=true'
2011-10-28 16:59:48,231 - INFO  - Dumping Generated ejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/ejb-jar-4814931753146039600EjbModule1519652738.xml
2011-10-28 16:59:48,232 - INFO  - Dumping Generated openejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/openejb-jar-741205343809155888EjbModule1519652738.xml
2011-10-28 16:59:48,238 - DEBUG - Adding persistence-unit movie-unit property openjpa.Log=log4j
2011-10-28 16:59:48,238 - DEBUG - Adjusting PersistenceUnit(name=movie-unit) property to openjpa.RuntimeUnenhancedClasses=supported
2011-10-28 16:59:48,247 - INFO  - Using 'openejb.validation.output.level=VERBOSE'
2011-10-28 16:59:48,247 - INFO  - Enterprise application "/Users/dblevins/examples/troubleshooting" loaded.
2011-10-28 16:59:48,247 - INFO  - Assembling app: /Users/dblevins/examples/troubleshooting
2011-10-28 16:59:48,251 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
2011-10-28 16:59:48,331 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
2011-10-28 16:59:48,717 - INFO  - PersistenceUnit(name=movie-unit, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 415ms
2011-10-28 16:59:48,718 - DEBUG - openjpa.jdbc.SynchronizeMappings=buildSchema(ForeignKeys=true)
2011-10-28 16:59:48,718 - DEBUG - openjpa.Log=log4j
2011-10-28 16:59:48,718 - DEBUG - openjpa.RuntimeUnenhancedClasses=supported
2011-10-28 16:59:48,846 - DEBUG - Using default 'openejb.jndiname.strategy.class=org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy'
2011-10-28 16:59:48,846 - DEBUG - Using default 'openejb.jndiname.format={deploymentId}{interfaceType.annotationName}'
2011-10-28 16:59:48,851 - DEBUG - Using default 'openejb.localcopy=true'
2011-10-28 16:59:48,854 - DEBUG - bound ejb at name: openejb/Deployment/Movies/org.superbiz.troubleshooting.Movies!LocalBean, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@34f34071
2011-10-28 16:59:48,854 - DEBUG - bound ejb at name: openejb/Deployment/Movies/org.superbiz.troubleshooting.Movies!LocalBeanHome, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@34f34071
2011-10-28 16:59:48,856 - INFO  - Jndi(name="java:global/troubleshooting/Movies!org.superbiz.troubleshooting.Movies")
2011-10-28 16:59:48,856 - INFO  - Jndi(name="java:global/troubleshooting/Movies")
2011-10-28 16:59:48,861 - DEBUG - Using default 'openejb.jndiname.strategy.class=org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy'
2011-10-28 16:59:48,861 - DEBUG - Using default 'openejb.jndiname.format={deploymentId}{interfaceType.annotationName}'
2011-10-28 16:59:48,861 - DEBUG - bound ejb at name: openejb/Deployment/org.superbiz.troubleshooting.MoviesTest/org.superbiz.troubleshooting.MoviesTest!LocalBean, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@3b0e2558
2011-10-28 16:59:48,861 - DEBUG - bound ejb at name: openejb/Deployment/org.superbiz.troubleshooting.MoviesTest/org.superbiz.troubleshooting.MoviesTest!LocalBeanHome, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@3b0e2558
2011-10-28 16:59:48,861 - INFO  - Jndi(name="java:global/EjbModule1519652738/org.superbiz.troubleshooting.MoviesTest!org.superbiz.troubleshooting.MoviesTest")
2011-10-28 16:59:48,861 - INFO  - Jndi(name="java:global/EjbModule1519652738/org.superbiz.troubleshooting.MoviesTest")
2011-10-28 16:59:48,875 - DEBUG - CDI Service not installed: org.apache.webbeans.spi.ConversationService
2011-10-28 16:59:48,985 - INFO  - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateless Container)
2011-10-28 16:59:49,013 - INFO  - Created Ejb(deployment-id=org.superbiz.troubleshooting.MoviesTest, ejb-name=org.superbiz.troubleshooting.MoviesTest, container=Default Managed Container)
2011-10-28 16:59:49,047 - INFO  - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateless Container)
2011-10-28 16:59:49,047 - INFO  - Started Ejb(deployment-id=org.superbiz.troubleshooting.MoviesTest, ejb-name=org.superbiz.troubleshooting.MoviesTest, container=Default Managed Container)
2011-10-28 16:59:49,047 - INFO  - Deployed Application(path=/Users/dblevins/examples/troubleshooting)
2011-10-28 16:59:49,317 - WARN  - The class "org.superbiz.testinjection.MoviesTest.Movie" listed in the openjpa.MetaDataFactory configuration property could not be loaded by sun.misc.Launcher$AppClassLoader@39172e08; ignoring.
2011-10-28 16:59:49,425 - WARN  - The class "org.superbiz.testinjection.MoviesTest.Movie" listed in the openjpa.MetaDataFactory configuration property could not be loaded by sun.misc.Launcher$AppClassLoader@39172e08; ignoring.
2011-10-28 16:59:49,437 - WARN  - The class "org.superbiz.testinjection.MoviesTest.Movie" listed in the openjpa.MetaDataFactory configuration property could not be loaded by sun.misc.Launcher$AppClassLoader@39172e08; ignoring.
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.567 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ troubleshooting ---
[INFO] Building jar: /Users/dblevins/examples/troubleshooting/target/troubleshooting-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ troubleshooting ---
[INFO] Installing /Users/dblevins/examples/troubleshooting/target/troubleshooting-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/troubleshooting/1.0/troubleshooting-1.0.jar
[INFO] Installing /Users/dblevins/examples/troubleshooting/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/troubleshooting/1.0/troubleshooting-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.421s
[INFO] Finished at: Fri Oct 28 16:59:49 PDT 2011
[INFO] Final Memory: 14M/81M
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
    package org.superbiz.troubleshooting;
    
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;
    
    @Entity(name = "Movie")
    public class Movie {
    
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;
    
        private String director;
        private String title;
        private int year;
    
        public Movie() {
        }
    
        public Movie(String director, String title, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
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
    package org.superbiz.troubleshooting;
    
    import javax.ejb.Stateless;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import java.util.List;
    
    //START SNIPPET: code
    @Stateless
    public class Movies {
    
        @PersistenceContext(unitName = "movie-unit", type = PersistenceContextType.TRANSACTION)
        private EntityManager entityManager;
    
        public void addMovie(Movie movie) throws Exception {
            entityManager.persist(movie);
        }
    
        public void deleteMovie(Movie movie) throws Exception {
            entityManager.remove(movie);
        }
    
        public List<Movie> getMovies() throws Exception {
            Query query = entityManager.createQuery("SELECT m from Movie as m");
            return query.getResultList();
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
    package org.superbiz.troubleshooting;
    
    import junit.framework.TestCase;
    
    import javax.annotation.Resource;
    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.transaction.UserTransaction;
    import java.util.List;
    import java.util.Properties;
    
    //START SNIPPET: code
    public class MoviesTest extends TestCase {
    
        @EJB
        private Movies movies;
    
        @Resource
        private UserTransaction userTransaction;
    
        @PersistenceContext
        private EntityManager entityManager;
    
        public void setUp() throws Exception {
            Properties p = new Properties();
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
    
            // These two debug levels will get you the basic log information
            // on the deployment of applications. Good first step in troubleshooting.
            p.put("log4j.category.OpenEJB.startup", "debug");
            p.put("log4j.category.OpenEJB.startup.config", "debug");
    
            // This log category is a good way to see what "openejb.foo" options
            // and flags are available and what their default values are
            p.put("log4j.category.OpenEJB.options", "debug");
    
            // This will output the full configuration of all containers
            // resources and other openejb.xml configurable items.  A good
            // way to see what the final configuration looks like after all
            // overriding has been applied.
            p.put("log4j.category.OpenEJB.startup.service", "debug");
    
            // Want timestamps in the log output?
            p.put("log4j.appender.C.layout", "org.apache.log4j.PatternLayout");
            p.put("log4j.appender.C.layout.ConversionPattern", "%d - %-5p - %m%n");
    
            // Will output a generated ejb-jar.xml file that represents
            // 100% of the annotations used in the code.  This is a great
            // way to figure out how to do something in xml for overriding
            // or just to "see" all your application meta-data in one place.
            // Look for log lines like this "Dumping Generated ejb-jar.xml to"
            p.put("openejb.descriptors.output", "true");
    
            // Setting the validation output level to verbose results in
            // validation messages that attempt to provide explanations
            // and information on what steps can be taken to remedy failures.
            // A great tool for those learning EJB.
            p.put("openejb.validation.output.level", "verbose");
    
            EJBContainer.createEJBContainer(p).getContext().bind("inject", this);
        }
    
        public void test() throws Exception {
    
            userTransaction.begin();
    
            try {
                entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
                entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));
    
                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());
    
            } finally {
                userTransaction.commit();
            }
    
            // Transaction was committed
            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());
    
        }
    
    }
    //END SNIPPET: code
