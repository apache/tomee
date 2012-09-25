-------------------------------
Apache OpenWebBeans M3
-------------------------------
This readme file contains an information regarding to the "M3" version of the
Apache OpenWebBeans project.

--------------------------------
What is an OpenWebBeans?
--------------------------------
OpenWebBeans is an ASL-License implementation of the JSR-299, Contexts and Dependency Injection for the Java EE platform.

Project web page could be found at the URL : 
"http://incubator.apache.org/projects/openwebbeans.html"

--------------------------------
OpenWebBeans M3 Release Features
--------------------------------

- M3 Release Supports the followings
-----------------------------------
* Managed Beans Support
* Session Beans Support (via Embeddable OpenEJB container in Tomcat)
* Producer Method Support
* Producer Field Support
* Java EE Resource Injection Support
* JMS OpenWebBeans Support(OWB Specific)
* Inheritance, Stereotype Inheritances
* Specialization Support
* Event Support
* Decorator and Interceptor Support
* Lookup and Dependency Injection Support
* Java EE Plugin Support (via ServetContextListener interface)
* Portable Integration Support

- M3 Release Does not Supports the followings
--------------------------------------------
* Injection into other Java EE 6 non-contextual components
* Passivation Capability of Beans
* Using @Alternative instead of @DeploymentType
* Does not fully integrated with Geronimo 
 (You have to configure your application to use OWB functionality, 
  e.g configure web.xml, adding interceptor to EJBs etc.)

-------------------------------------------
Release Notes - OpenWebBeans - Version M3
-------------------------------------------
Sub-task

    * [OWB-74] - support @PersistenceContext JPA injection

Bug

    * [OWB-59] - Review the handling of ConversationScope timeouts
    * [OWB-95] - rename exceptions to fit specification
    * [OWB-121] - remove JMS specific namings from OpenWebBeansPlugin
    * [OWB-123] - remove StereoType restrictions
    * [OWB-124] - ClassCastExceptoin during deployment
    * [OWB-132] - atinject license header formatting breaks build

Improvement

    * [OWB-65] - Java EE Namespace Support
    * [OWB-92] - facor out all EJB specific parts into an own maven module
    * [OWB-96] - EntityManager WebBeans Component
    * [OWB-105] - Improve Injection Resolver for Generic Classes
    * [OWB-108] - Update InjectionPoint interface
    * [OWB-109] - Update Instance Interface
    * [OWB-110] - Update Creational Context Interface
    * [OWB-111] - Update Contextual Interface
    * [OWB-112] - Check Usage of Creational Context
    * [OWB-113] - Dependent Context Active State
    * [OWB-114] - Check Contextual Reference Object Return Type
    * [OWB-117] - Updates Event For Last Draft

New Feature

    * [OWB-5] - EJB Web Beans
    * [OWB-43] - Constructor Parameter Injection For primitive/wrappers
    * [OWB-130] - implement JSR-330 annotations for OWB
    * [OWB-131] - integrate new JSR-330 annotations within OWB

Task

    * [OWB-2] - XML Configuration
    * [OWB-8] - Common Annotations
    * [OWB-12] - Update WebBeans Lifecycle for EJB Beans
    * [OWB-13] - Update WebBeans Lifecycle for JMS Beans
    * [OWB-47] - Java EE Resource Injection Support
    * [OWB-64] - Asynhronous Event Support via JMS
    * [OWB-66] - Schema Validation
    * [OWB-68] - Support for Child Managers
    * [OWB-115] - Contextual Reference Validity
    * [OWB-119] - Cover Chapter11 SPI Implementations
    * [OWB-120] - Implement Chapter12 Initialization Events

----------------------------------------------
Required Platform
----------------------------------------------
Java Version : Java SE >= 6.0
Java EE Must : Java EE >= 5.0

---------------------------------------------
How to Configure The OpenWebBeans
---------------------------------------------

This section explains a content of the distribution bundle, OWB plugins and its
dependent libraries. 

---------------------------------------------
M3 Bundle Content
---------------------------------------------
There are several jars in the OpenWebBeans M3 distribution;

 - atinject-api-1.0.0-incubating-M3.jar          --> Includes JSR-330 API
 - openwebbeans-api-1.0.0-incubating-M3.jar      --> Includes JSR-299 API
 - openwebbeans-impl-1.0.0-incubating-M3.jar     --> Includes Core Dependency Injection Service
 - openwebbeans-ejb-1.0.0-incubating-M3.jar      --> EJB Plugin(Supports EJBs in OpenEJB embedded in Tomcat)
 - openwebbeans-jms-1.0.0-incubating-M3.jar      --> JMS Plugin(Supports injection of JMS related artifacts)
 - openwebbeans-jpa-1.0.0-incubating-M3.jar      --> JPA Plugin(Non JTA Entity Manager Support)This is @Deprecated,use Resource Plugin. 
 - openwebbeans-jsf-1.0.0-incubating-M3.jar      --> JSF Plugin(JSF Conversation Scoped Support)
 - openwebbeans-resource-1.0.0-incubating-M3.jar --> Java EE Resource Injection Plugin
 - openwebbeans-geronimo-incubating-M3.jar       --> Geronimo Integration(In early stage,includes supports for OpenEJB resource injections)


------------------------------------------
How OWB Plugins Work
------------------------------------------

OpenWebBeans has been developing as a plugin way. Core dependency injection service
is provided with the JSR-299 API and Core mplementation jars. If you need further service functionality, 
you have to add respective plugin jars into the application classpath. Plugin functionality uses Java SE 6.0 
"java.util.ServiceLoader" utility.

------------------------------------------
Dependent Libraries
------------------------------------------

Third Party jars:
They are necessary at runtime in the Core Implementation.

log4j: Version 1.2.14 
dom4j: Version 1.6.1
javassist : Version 3.8.0.GA
scannotation : Version 1.0.2

Java EE APIs jars :
Generally full Java EE servers provides these jars. But web containers like Tomcat or Jetty
does not contain some of them, such as JPA, JSF api etc. So, if you do not want to bundle
these libraries within your application classpath, you have to put these libraries into your
server common classpath if it does not contain as built-in.

servlet-2.5 --> Core implementation
ejb-3.0     --> EJB Plugin
el-1.0      --> JSR-299 API, Core Implementation
jsf-1.2     --> JSF Plugin
jsr-250     --> Core implementation
interceptor-3.0 --> JSR-299 API, Core implementation
jta-1.1     --> Core implementation
jsp-2.1     --> Core implementation
jpa-3.0     --> Resource Plugin
jaxws-2.1   --> Resource Plugin
jms-1.1     --> JMS Plugin
 
------------------------------------------
Library Configuration
------------------------------------------
To run openwebbeans applications in the Java EE based application server, 
you could add JSR-299 API and JSR-330 API into the server common classpath, and
implementation, plugins and dependent jars into your "WEB-INF/lib" directory 
of the Java EE Web Application.

In this release, we can not support the OpenWebBeans as an integrated
functionality of the Java EE Application Servers. So, you have to manage the
configuration of the OpenWebBeans within your application's "web.xml" file. A sample "web.xml"
file can be found in the "config" directory. To use EJB functionality, you also have to
add OWB specific interceptor into your EJB beans. Look at EJB section for further details.

---------------------------------------------
OpenWebBeans Properties File
---------------------------------------------
OpenWebBeans uses a default configuration file to configure some of its
properties. Default configuration file is embedded into the OWB implementation
jar file. Instead of opening this jar file and changing configuration properties, simply add
"openwebbeans.properties" file into a "META-INF/" folder of your application
classpath. This will override the default configuration.

Belows are all configuration properties of the OpenWebBeans:

- "org.apache.webbeans.spi.JNDIService" : Configures JNDI provider implementation.
  Values : org.apache.webbeans.spi.ee.JNDIServiceEnterpriseImpl, org.apache.webbeans.spi.se.JNDIServiceStaticImpl
  Default: org.apache.webbeans.spi.se.JNDIServiceStaticImpl

- "org.apache.webbeans.spi.JNDIService.jmsConnectionFactoryJndi" : Configures JMS ConnectionFactory object jndi name
  Values : Server specific JNDI name
  Default: ConnectionFactory

- "org.apache.webbeans.conversation.Conversation.periodicDelay" : Conversation removing thread periodic delay
  Values : Configured in millisecond
  Default: 150000 ms

- "org.apache.webbeans.spi.TransactionService" : Transaction Service implementation for getting transaction manager and transaction
  Values : org.apache.webbeans.spi.ee.TransactionServiceJndiImpl, org.apache.webbeans.spi.se.TransactionServiceNonJTA
  Default: org.apache.webbeans.spi.ee.TransactionServiceJndiImpl

- "org.apache.webbeans.resource.spi.ResourceService" : Java EE Resource Injection Service
  Values : org.apache.webbeans.resource.spi.se.ResourceServiceImpl, org.apache.webbeans.spi.ee.openejb.resource.OpenEjbResourceServiceImpl
  Default: org.apache.webbeans.resource.spi.se.ResourceServiceImpl

- "org.apache.webbeans.spi.deployer.MetaDataDiscoveryService" : Discovers annotations and configuration files
  Values : Implementation of org.apache.webbeans.spi.deployer.MetaDataDiscoveryService SPI
  Default: org.apache.webbeans.spi.ee.deployer.WarMetaDataDiscoveryImpl for war and Collapsed ear in OpenEJB in Tomcat.

- "org.apache.webbeans.spi.deployer.UseEjbMetaDataDiscoveryService" : Use EJB functionality or not. If use OpenEJB configures to true
  Values : false, true
  Default: false

- "org.apache.webbeans.useOwbSpecificXmlConfig" : Use OWB specific XML configuration. OWB also supports its own XML configuration. 
  In future, you can configure beans via OWB specific XML configuration. Default is ok for the time being.
  Values : false, true
  Default: false

- "org.apache.webbeans.fieldInjection.useOwbSpecificInjection" : Use @Inject on injection fields or not. If this set to true
  you can inject object into beans without @Inject.
  Values : false, true
  Default: false

- "org.apache.webbeans.spi.conversation.ConversationService": Implementation of conversation.
  Values : Any custom implementation of org.apache.webbeans.spi.conversation.ConversationService
  Default: org.apache.webbeans.spi.conversation.jsf.JSFConversationServiceImpl

---------------------------------------------
EJB Support via Embeddable OpenEJB Container in Tomcat 6.X
---------------------------------------------

Configuration Steps:
--------------------------------------------
1* Download Tomcat 6.X version
2* Configure OpenEJB. Look at URL http://openejb.apache.org/tomcat.html for installation.
3* Copy "atinject-api-1.0.0-incubating-M3.jar" to Tomcat /lib folder.
4* Copy "openwebbeans-api-1.0.0-incubating-M3.jar"
5* Look at ejb-sample.war for "WEB-INF/lib" libraries to develop custom application.
   You can also look at a source of the project.

To use EJB functionality, you will use OpenEJB collapse-ear support. In this configuration,
your EJB beans live within your "war" bundle.

How to Develop EJB Applications
---------------------------------------------
1* Add "META-INF/openwebbeans.properties" into your application classpath.
2* Add "org.apache.webbeans.spi.deployer.UseEjbMetaDataDiscoveryService=true" to use EJB functionality.
   So OWB container looks for EJBs.
3* Add "org.apache.webbeans.resource.spi.ResourceService=org.apache.webbeans.spi.ee.openejb.resource.OpenEjbResourceServiceImpl" to
use OpenEJB Resource injections.
4* Add "openwebbeans-ejb", "openwebbeans-resource" and "openwebbeans-geronimo" plugins into your web application classpath. 
It adds EJB, Resource and Open EJB Resource plugins into your application.
5* If you want to use other plugins, add respective plugins into your application classpath. For example, if you wish to use
JSF framework, you add "openwebbeans-jsf" plugin.
6* Add OWB related interceptor into your EJB Beans. This is called "org.apache.webbeans.ejb.interceptor.OpenWebBeansEjbInterceptor"
This is needed for OWB injections.
7* Update your application's "web.xml" to add OWB specific configuration.

---------------------------------------------
How to Run Samples
---------------------------------------------

In this release, there are four sample applications located in the "/samples" directory 
of the distribution. "Guess","Reservation" and "Ejb Sample" applications contains pure
sources. You have to execute mvn builds to run them. "Jms Sample" contains war file.
You can directly deploy it into the container.

1) "Guess Application" : Simple usage of the OWB + JSF. 
It can be run in the jetty web container via maven jetty plugin from source. 
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section.

2) "Hotel Reservation Application" : Show usage of JSF + JPA + OWB  
It can be run in the jetty web container via maven jetty plugin from source. 
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section.

3) "EJB Sample Application" : Shows the usage of EJBs with embeddable OpenEJB in Tomcat. Firstly
configure OpenEJB with Tomcat as explained above.
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section

4) "JMS Injection Sample" : Show JMS injections. JMS injection currently uses
   ConnectionFactory as JMS connection factory jndi name. You can change this
   via configuration file. Look above explanation for how to configure JMS jndi. Also,
   JMS injection requires to use of a JMS provider. Generally Java EE servers contains
   default JMS provider. It can be run on JBoss and Geronimo. Simple drops "samples/jms-sample.war"
   into Java EE server deploy folder. Also it uses Queue with jndi_name = "queue/A". So you have to
   create a queue destination in your JMS provider with name "queue/A" to run example. If you
   want to change queue jndi name, then look at source and change it from "WEB-INF/beans.xml" file.

Configuring and Running the Applications:
--------------------------------------------
See section Compile and Run Samples via Jetty&Tomcat Plugin.

Library Dependencies
--------------------------------------------
"Third party" jars are included within the WAR deployment(In WEB-INF/lib).
But it still requires the "lib/javaee" Java EE API jars for running sucessfully.
If your server does not include any of them, simply take the necessary jar from the "lib/javaee" and
put it into your server classpath.

--------------------------------------------
Maven Install and Package From the Source
--------------------------------------------

Maven Version : Apache Maven 2.2.1

Firstly you have to download the "source/all" version of the OpenWebBeans project that
contains the all source codes of the OpenWebBeans.

To install the Maven artifacts of the project from the source, Maven must be installed
in your runtime. After Maven installation, just run the following command in the top level
directory that contains the main "pom.xml" : 

> mvn clean install

This command will install all the Maven artifacts into your local Maven repository.

If you wish to package all artifacts of the project, just run the following command
in in the top level directory that contains the main "pom.xml" : 

> mvn clean package

This command will package the project artifacts from the source and put these artifacts into the each modules
respective "target" directory.

-------------------------------------------
Compile and Run Samples via Tomcat&Jetty Plugin
-------------------------------------------

Samples Run within Jetty Plugin
------------------------------------------
You can compile and run "guess" and "reservation "samples via maven Jetty plugin.
Go to the source bundle "samples/" directory. In the "guess/" or "reservation/" directory, run
the following maven commands. It will start up maven Jetty container. It bundles all of the
required jars into the WEB-INF/lib folder. You are not required to add any jar to the classpath.

> mvn clean install -Pjetty
> mvn jetty:run -Pjetty

Guess URL : http://localhost:8080/guess
Reservation URL : http://localhost:8080/reservation

Samples Run within Tomcat Plugin
------------------------------------------
You can also compile and run "ejb-sample" example via Tomcat plugin. Tomcat
plugin uses http://localhost:8080/manager application to deploy war file
into your embeddable EJB Tomcat container. There must be an tomcat-users.xml
file in the "conf" directory of the server that contains manager role and username.

>Start Tomcat server if not started
>mvn tomcat:deploy

Ejb Sample URL : http://localhost:8080/ejb-sample

Example tomcat-users.xml file
------------------------------------------
<tomcat-users>
<role rolename="manager"/>
<user username="admin" password="" roles="manager"/>
</tomcat-users>

-----------------------------------------
Deploy JMS Sample
-----------------------------------------
Simple drops jms-sample.war file into your application deploy location.

JMS Sample Example URL        : Hit the url http://localhost:8080/jms-sample/sender.jsf for sending JMS messages
                                Hit the url http://localhost:8080/jms-sample/receiver.jsf for receiving JMS messages

-----------------------------------------------
OpenWebBeans User and Development Mailing Lists
-----------------------------------------------
Please mail to the user mailing list about any questions or advice
about the OpenWebBeans.

User Mailing List : [openwebbeans-users@incubator.apache.org]

You can also join the discussions happening in the dev list

Dev Mailing List : [opwnwebbeans-dev@incubator.apache.org]

-------------------------------------------
OpenWebBeans JIRA Page
-------------------------------------------
Please logs bugs into the "https://issues.apache.org/jira/browse/OWB".

------------------------------------------
OpenWebBeans Wiki and Blog Page
-----------------------------------------
Wiki: http://cwiki.apache.org/OWB/
Introduction to OpenWebBeans : http://cwiki.apache.org/OWB/introduction-to-openwebbeans.html
Blog : http://blogs.apache.org/owb

-----------------------------------------
OpenWebBeans Web Page
----------------------------------------
You can reach the OpenWebBeans web page at
http://incubator.apache.org/openwebbeans.
