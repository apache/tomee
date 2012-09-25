--------------------------------
What is OpenWebBeans?
--------------------------------
OpenWebBeans is an ASL-License implementation of the JSR-299, Java Context and Dependency Injection Specification.

Project web page could be found at the URL : 
http://incubator.apache.org/projects/openwebbeans.html

--------------------------------
OpenWebBeans M1 Release Content
--------------------------------

- M1 Release Supports the followings
-----------------------------------
* Simple WebBeans Support
* Producer Method Support
* Event Support
* Decorator and Interceptor Support
* Experimental XML Configuration Support
* Lookup and Dependency Injection Support
* Java EE Plugin Support (via ServetContextListener interface)
* Experimental JPA injection support.

- M1 Release Does not Supports the followings
--------------------------------------------
* Enterprise WebBeans Support
* JMS WebBeans Support
* Producer Field Support
* Servlet Injection Support
* Inheritance, Stereotype Inheritance and Realization Support
* Full Common Annotations Support
* Passivation Scope and Serialization Operations
* Full Support for XML Configuration
* Java EE Container Integration Support (SPI)

---------------------------------------------
Release Notes - OpenWebBeans - Version M1
Bug

    * [OWB-33] - bug in EJBInterceptComponentTest#testMultipleInterceptedComponent
    * [OWB-34] - bug in ExceptionComponentTest
    * [OWB-38] - Review all usages of catch(Throwable)
    * [OWB-54] - Update Bean.getBindings Contract
    * [OWB-61] - Refactor the annotations to match the new package schema.
    * [OWB-62] - Refactor web-beans.xml to beans.xml

Improvement

    * [OWB-31] - various XML definition improvements
    * [OWB-35] - cut back overly exception handling in ClassUtil
    * [OWB-41] - M1-Release Corrections
    * [OWB-58] - Dependent Context unused instance variable named : owner

Task

    * [OWB-7] - Manager initialization
    * [OWB-9] - Check Client Proxy Implementation
    * [OWB-17] - Test Events
    * [OWB-20] - Compilation errors while building either webbeans-api or webbeans-impl modules
    * [OWB-21] - No main pom.xml for OpenWebBeans
    * [OWB-23] - pom.xml files are not well organized. A compilation erro while compiling inside Eclipse
    * [OWB-27] - 5.2. Primitive types and null values
    * [OWB-30] - 5.10. Instance resolution, check Unproxiable Api Type control
    * [OWB-32] - Enabling more maven reports for site creation
    * [OWB-44] - Object toString method check on the Proxy
    * [OWB-60] - code cleanup unify getStereotype vs getStereoType in the sources
    * [OWB-63] - M1-Release Content
    * [OWB-70] - Change API copied from JBoss Impl.
    * [OWB-72] - add hsqldb license to our NOTICE, LEGAL, etc
    * [OWB-73] - add license headers to all XML files

Test

    * [OWB-22] - Unit tests failures in WebBeans-Impl module
    * [OWB-36] - create a test for WebBeansScanner
    * [OWB-40] - create test cases for XML constructor injection


---------------------------------------------
How to Configure The OpenWebBeans
---------------------------------------------

There are two important jars for OpenWebBeans;

 - openwebbeans-api-1.0.0-incubating-M1.jar
 - openwebbeans-impl-1.0.0-incubating-M1.jar

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

In this release, there is a sample application located in the "/samples" directory 
of the distribution.

Name of the binary file is the "samples/guess.war", you can deploy it 
into the any Java EE web container. Source is included in the "source" distribution of the
OpenWebBeans.

--------------------------------------------
Configuration of the Samples
--------------------------------------------

"Third party" and "OpenWebBeans" jars are included within the WAR deployment(In WEB-INF/lib). 
But it still requires the "lib/javaee" Java EE API jars for running sucessfully. 
If your server does not include any of them, simply take the necessary jar from the "lib/javaee" and
put it into your server classpath.

After that;

Hit the url : http://localhost:8080/guess 

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
OpenWebBeans User Mail List
-------------------------------------------

Please mail to the user mailing list about any questions or advice
about the OpenWebBeans.

User Mailing List : [openwebbeans-users@incubator.apache.org]

-------------------------------------------
OpenWebBeans JIRA Page
-------------------------------------------

Please logs the bugs into the "https://issues.apache.org/jira/browse/OWB".
