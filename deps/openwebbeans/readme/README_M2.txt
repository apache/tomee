-------------------------------
Apache OpenWebBeans M2
-------------------------------
This readme file contains the information regarding to the M2 of the
Apache OpenWebBeans project.

--------------------------------
What is OpenWebBeans?
--------------------------------
OpenWebBeans is an ASL-License implementation of the JSR-299, Java Context and Dependency Injection Specification.

Project web page could be found at the URL : 
http://incubator.apache.org/projects/openwebbeans.html

--------------------------------
OpenWebBeans M2 Release Content
--------------------------------

- M2 Release Supports the followings
-----------------------------------
* Simple WebBeans Support
* Producer Method Support
* Producer Field Support
* JMS WebBeans Support
* Inheritance, Stereotype Inheritances and Realization Support
* Specialization Support
* Event Support
* Decorator and Interceptor Support
* Experimental XML Configuration Support
* Lookup and Dependency Injection Support
* Java EE Plugin Support (via ServetContextListener interface)
* JPA Persistence Context injection support.(Currently does not support JTA based persistence context.)

- M2 Release Does not Supports the followings
--------------------------------------------
* Enterprise WebBeans Support
* Servlet Injection Support
* Full Common Annotations Support
* Passivation Scope and Serialization Operations

-------------------------------------------
Release Notes - OpenWebBeans - Version M2
-------------------------------------------
Sub-task

    * [OWB-26] - Inline webbeans decleration
    * [OWB-48] - Simple Beans Stereotyping
    * [OWB-49] - Producer Method Stereotyping
    * [OWB-50] - Implementation of the Contextual Interface
    * [OWB-51] - Implementation of the CreationalContext
    * [OWB-67] - Producer Field Implementation via XML
    * [OWB-71] - support @PersistenceUnit JPA injection

Bug

    * [OWB-42] - Dependent Context Inifinite Recursion
    * [OWB-78] - exception when running OpenWebBeans + MyFaces apps in jetty
    * [OWB-79] - wrong Exception about already defined WebBeansXMLConfigurator
    * [OWB-80] - according to the spec: Manager#getContext(scope) must throw a ContextNotActiveException if no active context exists.
    * [OWB-81] - acc 2 Spec: JNDI name has to be java:app/Manager
    * [OWB-82] - acc 2 Spec: Manager.addContext() has to throw IllegalArgumentException if there is more then 1active context
    * [OWB-83] - allow passivation of scopes @SessionScoped and @ConversationScoped
    * [OWB-84] - defining a bean @Serializable will case javaassist exceptions
    * [OWB-85] - WebBeansELResolver sets the "propertyResolved" flag to true regardless of the resolvation success
    * [OWB-90] - cannot find beans when working locally with jetty:run
    * [OWB-94] - Spec requires CreationalContext<?> instead of <T> in Manager#getInstanceToInject

Improvement

    * [OWB-45] - EL Name Resolution
    * [OWB-55] - Update Context API Contract and implementation
    * [OWB-76] - change dependency scopes to fit J2EE _and_ standalone concerns
    * [OWB-89] - make MetaDataDiscoveryService independent from scannotation
    * [OWB-91] - facor out all JPA specific parts into an own maven module
    * [OWB-93] - OWB TCKs should switch to released jsr-299 TCK artifacts

New Feature

    * [OWB-75] - create SPI integration support

Task

    * [OWB-3] - Stereotype Inheritance
    * [OWB-4] - JMS Components
    * [OWB-10] - Injection Point MetaData
    * [OWB-11] - Update WebBeans Lifecycle for SimpleWebBeans
    * [OWB-24] - Producer Field Implementation
    * [OWB-25] - Chapter-4 Inheritance and Realization
    * [OWB-28] - 5.4.1. Unproxyable API types
    * [OWB-29] - 5.9. Dynamic lookup, Instance and Obtains Support
    * [OWB-52] - Support for @Obtains via Instance interface
    * [OWB-69] - Add Manager#parse Implementation
    * [OWB-87] - move all non-JSF specific parts of Conversations handling to an own general package
    * [OWB-88] - facor out all JSF specific parts into an own maven module

Test

    * [OWB-15] - Test Interceptors
    * [OWB-16] - Test Decorators
    * [OWB-18] - Test Conversation Context


---------------------------------------------
How to Configure The OpenWebBeans
---------------------------------------------

There are several jars for OpenWebBeans distribution;

 - openwebbeans-api-1.0.0-incubating-M2.jar  --> Includes JSR-299 API
 - openwebbeans-impl-1.0.0-incubating-M2.jar --> Includes Core Dependency Injection Service
 - openwebbeans-ejb-1.0.0-incubating-M2.jar  --> EJB Plugin(In early stage)
 - openwebbeans-jms-1.0.0-incubating-M2.jar  --> JMS Plugin
 - openwebbeans-jpa-1.0.0-incubating-M2.jar  --> JPA Plugin(Non JTA Entity Manager Support)
 - openwebbeans-jsf-1.0.0-incubating-M2.jar  --> JSF Plugin(JSF Conversation Scoped Support)
 - openwebbeans-geronimo-incubating-M2.jar   --> Geronimo Integration(In early stage JTA Entity Manager Support via OpenEJB)


OpenWebBeans has been developing as a plugin way. Core dependency injection service
is provided with the API and IMPL jars. If you need the other functionality, you have to add
respective plugin jars into the application classpath.

There are also third party dependent libraries. These dependent library jars
are located in the directory "/lib/thirdparty" in the distribution.

Java EE APIs jars that are used by the project are located in the directory
"lib/javaee" directory in the distribution. You could put the necessary Java EE 
jars into the  server classpath if the server is not contains these jars already. 

To run openwebbeans applications in the Java EE based application server, 
you could add OpenWebBeans API, Implementation and dependent jars into 
the common classpath of the Java EE Application Server or your "WEB-INF/lib"
directory of the Java EE Web Application.

In this release, we can not support the OpenWebBeans as an integrated
functionality of the Java EE Application Servers. So, you have to manage the
configuration of the OpenWebBeans within your "web.xml" file. A sample "web.xml"
file can be found in the "config" directory.

---------------------------------------------
How to Run The Samples
---------------------------------------------

In this release, there are two sample applications located in the "/samples" directory 
of the distribution.

1) Guess Application : Name of the binary file is the "samples/guess.war". 
Source is included in the "source" distribution of the OpenWebBeans.

2) Hotel Reservation Application : Full blown web application that is developed with JPA,
JSF and OpenWebBeans. Name of the war file is "samples/reservation.war". Source is
included in the "source" distribution of the OpenWebBeans.

Configuring and Running the Applications:
--------------------------------------------
Simple put the "war" file into the any compatible web container.

Library Dependencies
--------------------------------------------
"Third party" jars are included within the WAR deployment(In WEB-INF/lib).
But it still requires the "lib/javaee" Java EE API jars for running sucessfully.
If your server does not include any of them, simply take the necessary jar from the "lib/javaee" and
put it into your server classpath.


Guess Example URL : Hit the url : http://localhost:8080/guess 
Hotel Reservation Example URL : Hit the url http://localhost:8080/reservation

--------------------------------------------
Maven Install and Package From the Source
--------------------------------------------

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
Compile and Run Samples via Jetty Plugin
-------------------------------------------
You can compile and run samples via maven Jetty plugin.
Go to the source bundle "samples/" directory. In the "guess/" or "reservation/" directory, run
the following maven commands. It will start up maven Jetty container. It bundles all of the
required jars into the WEB-INF/lib folder. You are not required to add any jar to the classpath.

> mvn clean install -Pjetty
> mvn jetty:run -Pjetty

Hit the above URLs to run the samples in the web browser.

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
