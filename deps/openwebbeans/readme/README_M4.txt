-------------------------------
Apache OpenWebBeans M4
-------------------------------
Welcome! Thanks for downloading and using OpenWebBeans. This document is a
"Getting Started Guide" for OpenWebBeans.

This document is based on the M4 release of the OpenWebBeans.

NOTE : Final release version of OpenWebBeans will be 1.0.0.

--------------------------------
What is Apache  OpenWebBeans?
--------------------------------
OpenWebBeans is an ASL 2.0-licensed implementation of the JSR-299, Contexts and Dependency Injection for the Java EE platform.

Project's web page can be found at: 
"http://openwebbeans.apache.org"

--------------------------------
OpenWebBeans M4 Release Features
--------------------------------

- M4 release supports the following features
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
* Passivation Capability of Beans
* @Alternative support in drop of @DeploymentType

- M4 release does not supports the following features
--------------------------------------------
* Injection into other Java EE 6 non-contextual components
  - Currently, it is not possible to use injection into Servlets, Filters etc.
    that are not contextual beans.
* Does not fully integrate with Geronimo 
 (You have to configure your application to use OWB functionality, 
  e.g configure web.xml, adding interceptor to EJBs etc.)

-------------------------------------------
Release Notes - OpenWebBeans - Version M4
-------------------------------------------
Sub-task

    * [OWB-140] - Remove javax.enterprise.event.Observer

Bug

    * [OWB-122] - use the latest TCK suite for webbeans-tck
    * [OWB-126] - remove old rules and formattings for handling of beans.xml
    * [OWB-127] - @Stateful EJBs have to be handled as being passivation capable
    * [OWB-128] - OWB crashes while parsing methods with generic parameters
    * [OWB-129] - OWB crashes on creating disposer method
    * [OWB-141] - Conversation.isLongRunning() was removed in favor of Conversation.isTransient()
    * [OWB-142] - If an injection point declares no qualifier, then @Default should be assumed.
    * [OWB-143] - Decorator matching looks for all bean qualifiers in the list of delegate qualifiers
    * [OWB-144] - upgrade OWB TCK from webbeans to weld
    * [OWB-146] - A new extract of openwebbeans on a fresh system has difficulty resolving dependencies and repositories.
    * [OWB-147] - component->comp typo interceptor stack copy
    * [OWB-148] - create a test case for the BeforeShutDown event
    * [OWB-149] - BeforeShutDown (in current code) should be BeforeShutdown to match spec.
    * [OWB-150] - remove ActivityManager from OWB
    * [OWB-152] - @New needs a value parameter for the Class it should create
    * [OWB-153] - javax.enterprise.inject.spi.Decorator#getDelegateBindings() must be renamed to getDelegateQualifiers();
    * [OWB-154] - remove Bean#getDeploymentType()
    * [OWB-155] - Conversation#isLongRunning() logic must be converted to isTransient();
    * [OWB-156] - ProcessSessionBean SPI interface needs to be updated to the latest spec
    * [OWB-157] - Interceptors register all annotations as Interceptor Bindings
    * [OWB-158] - interceptor stack does not honor beans.xml ordering
    * [OWB-159] - interceptor/bean binding relationship is inverted in WebBeansInterceptor.hasBinding()
    * [OWB-160] - interceptor bindings at class-level added to stack twice
    * [OWB-161] - Producer fields do not honor generics
    * [OWB-162] - WebBeansJSFFilter is cutting off included ViewParams
    * [OWB-163] - Conversations are not scoped to a particular session
    * [OWB-165] - Missing bean interception stack during TagHandler expression evaluation
    * [OWB-166] - Interceptor bindings, that are defined on stereotypes are not applied to beans
    * [OWB-167] - Buildin Bean types should be decoratable
    * [OWB-168] - cid parameter is not propagated for redirects caused by ajax requests and on <h:link/>
    * [OWB-169] - PrimitiveProducerTest creates a NullPointerException
    * [OWB-171] - CID during GET requests must be set on UIViewRoot earlier than before render response
    * [OWB-172] - Producer field type that is a type variable leads to a NPE rather than definition error (Section 3.4)
    * [OWB-173] - Singleton context is not set as ThreadLocal on every request
    * [OWB-175] - SingletonContext not available in in multithreaded SE environments, which use the StandaloneLifeCycle
    * [OWB-176] - InterceptionType.AROUND_TIMEOUT is missing
    * [OWB-179] - PluginLoader need to actually startUp each plugin at load time
    * [OWB-180] - remove e.printStackTrace() and use proper logging
    * [OWB-181] - TCK requires to create newInstance of hidden constructors
    * [OWB-182] - Even if @PreDestroy is used in an Interceptor, it doesn't need an InvoicationContext parameter
    * [OWB-184] - BeanManager itself needs to be added as managed bean
    * [OWB-185] - Managed beans with non-default constructors lead to InstantiationException when creating the proxy
    * [OWB-189] - @Interceptors-defined interceptors run after JCDI interceptors
    * [OWB-192] - Bean Api Types Does not contain Object.class
    * [OWB-199] - Bug in ProducerMethod primitive return type resolution
    * [OWB-200] - @Type annotation does not work correctly
    * [OWB-201] - @New must use its value field while creating New bean
    * [OWB-206] - proxies only get injected for the 1st instance of a bean
    * [OWB-207] - <interceptors> may now be defined in multiple beans.xml
    * [OWB-208] - the atinject-tck must use our genonimo-cdi interface instead of javax.inject:javax.inject
    * [OWB-210] - creating a proxy fails for beans which have the same interface defined multiple times in their declaration chain
    * [OWB-211] - JSFUtil.getViewId() crashes if viewRoot is not yet set
    * [OWB-215] - after leaving the incubator a new location for the site needs to be configured
    * [OWB-217] - IllegalStateException must be thrown on Already Begin or End Conversations
    * [OWB-224] - we need to destroy() our Contextual instances on a Conversation timeout
    * [OWB-225] - BeforeBeanDiscoveryImpl is not currently implemented
    * [OWB-227] - handle errors set via AfterBeanDiscovery#addDefinitionError
    * [OWB-228] - move faces-config from webbeans-impl to webbeans-jsf module
    * [OWB-232] - exceptions with non-OWB XML don't contain interceptor/decorator classnames
    * [OWB-233] - Circular Dependent Between Beans
    * [OWB-240] - check on existing conversation is missing in Conversation#begin(String id)
    * [OWB-241] - Conversation scoped bean instance gets destroyed for every ELResolver.getValue
    * [OWB-244] - OWB injects a new object for @disposes injection point for dependent scope
    * [OWB-246] - BeanManager#getReference must create a bean with resolved dependencies
    * [OWB-247] - notifying an ObserverMethod with Reception.ALWAYS must not cause a getReference() if the bean already exists
    * [OWB-249] - ObserverMethods should not use the contextual instance directly but the proxy instead
    * [OWB-251] - Proxies created with JavassistProxyFactory do not intercept private functions
    * [OWB-252] - Decorators stack not treated as a true stack
    * [OWB-253] - Caching of Bean Proxies
    * [OWB-258] - InjectionPoint meta-data injection into dependent beans not work correctly
    * [OWB-260] - Handle Interceptors and Decorators properly when both are applied to a bean
    * [OWB-261] - Implement BeanManager#getPassivationCapableBean(String id)
    * [OWB-262] - Beans defined via TextContext XML get added twice
    * [OWB-263] - BeanManagerImpl#isPassivatingScope need to additionally consider Scopes added via BeforeBeanDiscovery#addScope()
    * [OWB-266] - transitive interceptor bindings not applied to Bean subclasses
    * [OWB-267] - interceptor bindings in a stereotype are not inherited by subclass
    * [OWB-269] - NPE when using WebApplicationLifeCycle in Test
    * [OWB-270] - openwebbeans-resouce must not contain EJB dependencies
    * [OWB-271] - method-level interceptor bindings not inherited properly
    * [OWB-272] - memory leak and huge cpu consumption in latest snapshot
    * [OWB-273] - Given class is not annotated with @Alternative Exception when try to enable alternative producer/producer field beans
    * [OWB-274] - interceptor and decorator don't read re-Annotated meta data
    * [OWB-279] - Indirect specialization (4.3.1) throws InconsistentSpecializationException
    * [OWB-283] - serializable check on Interceptor and Decorator stack should check for null
    * [OWB-284] - OWB could not find default bean if alternative specialized bean is not enabled.
    * [OWB-285] - Interceptor and Decorator instances must be @Dependent scoped
    * [OWB-289] - Owb return 2 beans for Indirect specialized producer beans
    * [OWB-291] - InterceptorHandler crashes with ClassCastException when getting serialized
    * [OWB-292] - InterceptorHandler crashes with NullPointerException if no creationalContext is present
    * [OWB-294] - manually added scopes doesn't get detected correctly in AbstractOwbBean
    * [OWB-296] - DependentCreationalContext doesn't properly serialize it's Contextual<T>
    * [OWB-297] - DelegateInjection point should not be required to be an interface.
    * [OWB-298] - When checking that the delegate implements all decorated types, we must not check for Serializable
    * [OWB-299] - WebBeansUtil's checkDecoratorResolverParams does not always check that the annotations passed are qualifiers
    * [OWB-301] - WebBeansDecorators getDecoratedTypes returns the types of the wrapped bean instead of its list of decorated types
    * [OWB-304] - Beans from a ProducerMethods don't get injected anymore
    * [OWB-305] - exceptions become InvocationTargetException when propogated up interceptor stack
    * [OWB-307] - InvocationContext.setParameters() and primitive vs. wrapped types
    * [OWB-308] - minor clean up on specialization code path
    * [OWB-309] - signature check for @Interceptors-enabled interceptors is too strict
    * [OWB-311] - @ExcludeClassInterceptors doesn't remove in all cases

Improvement

    * [OWB-98] - Check ManagedBean Public Field
    * [OWB-99] - Interceptors and decorators may not declare producer methods.
    * [OWB-100] - Update for Disposal Methods
    * [OWB-101] - Update @New Binding Type for Taking Class Member Method
    * [OWB-102] - Initializer Methods Validation for Generic Methods
    * [OWB-103] - Inconsistent Specialization Extra Check For Injection Points
    * [OWB-104] - Check Inherited Member for producer methods and fields
    * [OWB-164] - Consistent logging in WebBeansInterceptorConfig
    * [OWB-174] - ApplicationContext available in StandaloneLifeCycle
    * [OWB-186] - Upgrade OWB to the JPA-2 spec
    * [OWB-188] - remove webbeans-jpa and cleanup webbeans-resource
    * [OWB-190] - Make the TestLifeCycles available in webbeans-impl
    * [OWB-191] - Convert logging to use keyed, formatted strings from a ResourceBundle to allow for translation.
    * [OWB-202] - upgrade webbeans-jsf to JSF-2
    * [OWB-203] - Improve logging of webbeans-resource if a PersistenceUnit cannot be found
    * [OWB-212] - Improve logging for various failures in Producer and Disposal methods
    * [OWB-213] - Update Interceptor Project Dependency to 1.1
    * [OWB-248] - upgrade to apache-parent 6 (from 4)
    * [OWB-250] - Update Extensions Event Calling More TypeSafe & Adding more tests for Extensions
    * [OWB-255] - injection point errors at deployment don't tell you the injection point!
    * [OWB-264] - Location of redirect in faces-config.xml files is causing warnings.
    * [OWB-265] - Some of the resources in the bundle needed to be removed or updated.
    * [OWB-268] - Implementation of BeanManager#createInjectionTarget
    * [OWB-276] - Improve consistency of Asserts referencing null clazz.
    * [OWB-278] - Remove DEBUG messages from resource bundle since they don't need to be translated.
    * [OWB-280] - Update Bean Class Hierarchy
    * [OWB-281] - Create Resource Bean (Section 3.5), update resource plugin handling
    * [OWB-282] - Adding Default SPI Implementation for 3.6. Update names of Default service implementations.
    * [OWB-287] - DefaultScannerService should skip WAR checks if no context is given
    * [OWB-290] - provide SPI for Servlet ContainerLifecycle for better testing support
    * [OWB-293] - upgrade OWB samples to MyFaces Core v2.0.0-beta-2
    * [OWB-300] - Currently OWB requires log4j for logging. This patch provides a means of choosing other loggers.

New Feature

    * [OWB-135] - implement API changes from spec version PFD2
    * [OWB-137] - implement BusyConversationException use case
    * [OWB-138] - implement NonexistentConversationException use case
    * [OWB-226] - create a way to dynamically add Extensions to our tests
    * [OWB-229] - add support for the JSF2 javax.faces.bean.ViewScoped annotation
    * [OWB-230] - Enable/Disable Configuration of JSF2 Extensions
    * [OWB-277] - Owb on Google App Engine

Task

    * [OWB-106] - Update code for new deployment type handling.
    * [OWB-107] - Check EL Name Ambigious
    * [OWB-145] - Refactor InterceptorHandler filter methods
    * [OWB-205] - Update Samples for MyFaces 1.2 Latest Version
    * [OWB-218] - Work on (Section 2.5 - Bean EL names) of coverege-cdi.html of RI TCK
    * [OWB-219] - Work on (Section 2.6 - Alternatives) of coverege-cdi.html of RI TCK
    * [OWB-221] - Work on (Section 2.7 - Stereotypes) of coverege-cdi.html of RI TCK
    * [OWB-223] - Work on (Section 3.1 - Managed beans) of coverege-cdi.html of RI TCK
    * [OWB-234] - Update geronimo-cdi to geronimo-jcdi, update geronimo-interceptor specs
    * [OWB-235] - Pass TCK Event Tests
    * [OWB-242] - Update Interceptor Project Dependency to 1.1 with EA1-SNAPSHOT Version
    * [OWB-243] - Ensure serialized access to conversation scope by blocking or rejecting concurrent requests
    * [OWB-256] - Define SPI Maven Module
    * [OWB-257] - Delete webbeans-geronimo module

Wish

    * [OWB-236] - OWB docs online

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
M4 Distribution Content
---------------------------------------------
There are several jars in the OpenWebBeans M4 distribution;

 - openwebbeans-impl-1.0.0-M4.jar     --> Includes Core Dependency Injection Service.
 - openwebbeans-ejb-1.0.0-M4.jar      --> EJB Plugin(Supports EJBs in OpenEJB embedded in Tomcat).
 - openwebbeans-jms-1.0.0-M4.jar      --> JMS Plugin(Supports injection of JMS related artifacts,i.e, ConnectionFactory, Session, Connection etc.)
 - openwebbeans-jsf-1.0.0-M4.jar      --> JSF Plugin(JSF Conversation Scoped Support).
 - openwebbeans-resource-1.0.0-M4.jar --> Java EE Resource Injection for Web Projects (Includes @PersistenceContext,@PersistenceUnit
                                          and @Resource injection into the Managed Beans. @Resource injections use java:/comp/env of the
                                          Web application component. @PersistenceContext is based on extended EntityManager.
 - openwebbeans-spi-1.0.0-M4.jar      --> OpenWebBeans Server Provider Interfaces. They are implemented by runtime environments that would
                                          like to use OpenWebBeans as a JSR-299 implementation.
 - samples                            --> Includes source code of the samples. Samples are mavenized project  therefore you can easily build and run
                                          them from your environment that has maven runtime.


------------------------------------------
How OWB Plugins Work
------------------------------------------

OpenWebBeans has been developing as a plugin way. Core dependency injection service
is provided with the Core implementation jar,i.e, openwebbeans-impl. If you need further service functionality, 
you have to add respective plugin jars into the application classpath. Plugin functionality uses Java SE 6.0 
"java.util.ServiceLoader" utility.

Current Plugins:
---------------------
Look at "M4 Distribution Content" above.

------------------------------------------
Dependent Libraries
------------------------------------------

Third Party jars:
-----------------
They are necessary at runtime in the Core Implementation.

log4j: Version 1.2.14 
dom4j: Version 1.6.1
javassist : Version 3.11.0.GA
scannotation : Version 1.0.2

Java EE APIs jars(Container Provider Libraries) :
-------------------------------------------------
Generally full Java EE servers provides these jars. But web containers like Tomcat or Jetty
does not contain some of them, such as JPA, JSF, Validation API etc. So, if you do not want to bundle
these libraries within your application classpath, you have to put these libraries into your
server common classpath if it does not contain.

jcdi-api (JSR-299 Specification API)
atinject-api (JSR-330 Specification API)
servlet-2.5 or servlet 3.0 (Servlet Specification API)
ejb-3.1 (EJB Specification API)
el-2.2 (Expression Langauge Specification API)
jsf-2.0 (Java Server Faces API)
jsr-250 (Annotation API)
interceptor-1.1 (Interceptor API)
jta-1.1 (Java Transaction API)
jsp.2.1 or jsp-2.2 (Java Server Pages API)
jpa-2.0 (Java Persistence API)
jaxws-2.1 or jaxws-2.2 (Java Web Service API)
jms-1.1 or jms-1.2 (Java Messaging Service API)
validation (Validation Specification)

Dependencies of OpenWebBeans Maven Modules&Plugins
--------------------------------------------------

openwebbeans-impl : 
------------------
Third party        : log4j, dom4j, javassist, scannotation, openwebbeans-spi
Container Provided : jcdi-api, at-inject, servlet, el, jsr-250, interceptor, jta, jsp, validation

openwebbeans-ejb:
-----------------
Third party        : openwebbeans-impl and its dependencies
Container Provided : OpenWebBeans EJB plugin is based on OpenEJB in Tomcat. Therefore, if you install OpenEJB
                     within Tomcat correctly, there is no need to add any additional libraries. Look at the
                     OpenEJB in Tomcat configuration section.

openwebbeans-jms:
-----------------
Third party        : openwebbeans-impl and its dependencies
Container Provided : jms

openwebbeans-jsf:
-----------------
Third party        : openwebbeans-impl and its dependencies
Container Provided : jsf

NOTE : We are trying to decrease dependent libraries of the our core, i.e, openwebbeans-impl. 
At 1.0.0, dependent third party libraries will be decreased. We have a plan to create profile
plugins, therefore each profile plugin provides its own dependent libraries. For example, in 
fully Java EE Profile Plugin, Transaction API is supported but this will not be the case
for Java Web Profile Plugin or Java SE Profile Plugin. Stay Tune!

Currently, as you have seen above, openwebbeans-impl depends on some Java EE/Runtime
provided libraries (servlet, jsp, el etc). In the future, with OpenWebBeans profiling support,
openwebbeans-impl will not depend on any Java EE APIs. Those APIs will be provided
by OpenWebBeans profiles/plugins that openwebbeans-impl will be used. Therefore,
you will able to use OpenWebBeans in your own runtime environment easily by writing
your own plugins and contributing it to OpenWebBeans :)
        
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
add OWB specific interceptor into your EJB beans. Look at the EJB section of this readme 
for further details.

---------------------------------------------
OpenWebBeans Properties File
---------------------------------------------
OpenWebBeans uses a default configuration file to configure some of its
properties. Default configuration file is embedded into the OWB implementation
jar file. Instead of opening this jar file and changing configuration properties, simply add
"openwebbeans.properties" file into a "META-INF/openwebbeans" folder of your application
classpath. This will override the default configuration.

Belows are default configuration properties of the OpenWebBeans that is embedded into openwebbeans-impl jar file.

Each plugin or developer can provide its own SPI implementation class and its own configuration values. If you woud like
to use those implementation classes or configuration values, you have to override default configuration file as explained
in the above paragraph, i.e, putting "openwebbeans.properties" file into "META-INF/openwebbeans" folder of your application.

For example : You add "META-INF/openwebbeans/openwebbeans.properties" in your application classpath. And you add the following
key-value pair to use. And this service implementation is provided by your plugin, for example OpenWebBeans OpenEJB plugin.

Override default value of ResourceInjectionService
-------------------------------------------------
org.apache.webbeans.spi.ResourceInjectionService=org.apache.webbeans.ejb.resource.OpenEjbResourceInjectionService

OpenWebBeans uses the "OpenEjbResourceInjectionService" class to inject resources into the managed bean instances. 

Configuration Names and Their Default Values :

- "org.apache.webbeans.spi.ContainerLifecycle"
   Description : Implementation of org.apache.webbeans.spi.ContainerLifecycle. All magic starts from here.
   Values      : org.apache.webbeans.lifecycle.DefaultLifecycle, OR CUSTOM
   Default     : org.apache.webbeans.lifecycle.DefaultLifecycle

- "org.apache.webbeans.spi.JNDIService"
   Description  : Configures JNDI provider implementation.
   Values       : org.apache.webbeans.spi.se.DefaultJndiService OR CUSTOM
   Default      : org.apache.webbeans.spi.se.DefaultJndiService

- "org.apache.webbeans.spi.conversation.ConversationService"
   Description  : Implementation of conversation.
   Values       : org.apache.webbeans.spi.conversation.jsf.DefaultConversationService OR CUSTOM
   Default      : org.apache.webbeans.spi.conversation.jsf.DefaultConversationService

- "org.apache.webbeans.spi.ScannerService"
   Description  : Default implementation of org.apache.webbeans.spi.ScannerService. It is used for scanning application deployment
                  for finding bean classes and configuration files.
   Values       : org.apache.webbeans.spi.ee.deployer.DefaultScannerService OR CUSTOM
   Default      : org.apache.webbeans.spi.ee.deployer.DefaultScannerService

- "org.apache.webbeans.spi.SecurityService"
   Description   : Implementation of org.apache.webbeans.spi.SecurityService. It is used for getting current "Principal".
   Values        : org.apache.webbeans.spi.se.DefaultSecurityService or CUSTOM
   Default       : org.apache.webbeans.spi.se.DefaultSecurityService

- "org.apache.webbeans.spi.ValidatorService"
   Description   : Implementation of org.apache.webbeans.spi.ValidatorService. It is used for getting "ValidatorFactory" and "Validator".
   Values        : org.apache.webbeans.spi.se.DefaultValidatorService or CUSTOM
   Default       : org.apache.webbeans.spi.se.DefaultValidatorService

- "org.apache.webbeans.spi.TransactionService"
   Description   : Implementation of org.apache.webbeans.spi.TransactionService. It is used for getting "TransactionManager" and "Transaction".
   Values        : org.apache.webbeans.spi.se.DefaultTransactionService or CUSTOM
   Default       : org.apache.webbeans.spi.se.DefaultTransactionService

- "org.apache.webbeans.spi.ResourceInjectionService" 
   Description   : Implementation of org.apache.webbeans.spi.ResourceInjectionService. It is used for injection Java EE enviroment resource into the
                   Managed Bean instances.
   Values        : org.apache.webbeans.se.DefaultResourceInjectionService or CUSTOM
   Default       : org.apache.webbeans.se.DefaultResourceInjectionService

- "org.apache.webbeans.spi.JNDIService.jmsConnectionFactoryJndi"
   Description   : Configures JMS ConnectionFactory object jndi name
   Values        : Server specific JNDI name
   Default       : ConnectionFactory

- "org.apache.webbeans.conversation.Conversation.periodicDelay"
   Description   : Conversation removing thread periodic delay
   Values        : Configured in millisecond
   Default       : 150000 ms

- "org.apache.webbeans.spi.deployer.useEjbMetaDataDiscoveryService"
   Description   : Use EJB functionality or not. If use OpenEJB configures to true
   Values        : false, true
   Default       : false

---------------------------------------------
EJB Support via Embeddable OpenEJB Container in Tomcat 6.X
---------------------------------------------

Configuration Steps:
--------------------------------------------
1* Download Tomcat 6.X version
2* Configure OpenEJB. Look at URL http://openejb.apache.org/tomcat.html for installation.
3* Copy JSR-330 API to Tomcat /lib folder.
4* Copy JSR-299 API to Tomcat /lib folder
5* Put all dependent libraries of the OpenWebBeans OpenEJB Plugin
   - openwebbeans-ejb
   - openwebbeans-impl and its dependencies

You could look at ejb-sample.war for "WEB-INF/lib" libraries to develop custom application.
You can also look at a source of the project.

To use EJB functionality, you will use OpenEJB collapse-ear support. In this configuration,
your EJB beans live within your "war" bundle.

How to Develop EJB Applications
---------------------------------------------
1* Add "META-INF/openwebbeans.properties" into your application classpath.
2* Add "org.apache.webbeans.spi.deployer.useEjbMetaDataDiscoveryService=true" to use EJB functionality.
   So OWB container looks for EJBs.
3* Add "org.apache.webbeans.resource.spi.ResourceService=org.apache.webbeans.ejb.resource.OpenEjbResourceInjectionService to
use OpenEJB Resource injections.
4* Add "openwebbeans-ejb", plugin into your web application classpath. 
5* If you want to use other plugins, add respective plugins into your application classpath. For example, if you wish to use
JSF framework, you add "openwebbeans-jsf" plugin.
6* Add OWB related interceptor into your EJB Beans. This is called "org.apache.webbeans.ejb.interceptor.OpenWebBeansEjbInterceptor"
This is needed for OWB injections.
7* Update your application's "web.xml" to add OWB specific configuration.

---------------------------------------------
How to Run Samples
---------------------------------------------

In this release, there are several sample applications located in the "/samples" directory 
of the distribution. You can run those samples via simple maven command.

1) "Guess Application" : Simple usage of the OWB + JSF. 
It can be run in the jetty web container via maven jetty plugin from source. 
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section.

2) "Hotel Reservation Application" : Show usage of JSF + JPA + OWB  
It can be run in the jetty web container via maven jetty plugin from source. 
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section.

3) "EJB Sample Application" : Shows the usage of EJBs with embeddable OpenEJB in Tomcat. Firstly
configure OpenEJB with Tomcat as explained above.
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section.

4) "EJB Telephone Application" : Shows the usage of OpenEJB resource injection service.
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section.

5) "JMS Injection Sample" : Show JMS injections. JMS injection currently uses
   ConnectionFactory as JMS connection factory jndi name. You can change this
   via configuration file. Look above explanation for how to configure JMS jndi. Also,
   JMS injection requires to use of a JMS provider. Generally Java EE servers contains
   default JMS provider. It can be run on JBoss and Geronimo. It uses Queue with jndi_name = "queue/A". 
   So you have to create a queue destination in your JMS provider with name "queue/A" to run example. 
   If you want to change queue jndi name, then look at source and change it from "WEB-INF/beans.xml" file.

6) "Conversation Sample" : Shows usage of JSF conversations.
It can be run in the jetty web container via maven jetty plugin from source.
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section.

7) "JSF2 Sample" : Shows usage of JSF2 Ajax.
It can be run in the jetty web container via maven jetty plugin from source.
Look at "Compile and Run Samples via Jetty&Tomcat Plugin" section. It requires
to use JSF2 runtime.


8) "Standalone Sample" : Shows usage of OpenWebBeans in Stadnalone Swing Application.
Look at "OpenWebBeans in Java SE" section.

Configuring and Running the Applications:
--------------------------------------------
See section Compile and Run Samples via Jetty&Tomcat Plugin.

--------------------------------------------
Maven Install and Package From the Source
--------------------------------------------

Maven Version : Apache Maven 2.2.1 or later

Firstly you have to download the "source" version of the OpenWebBeans project that
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
This section shows how to run samples in Jetty or OpenEJB Embedded Tomcat.

------------------------------------------
Samples Run within Jetty Plugin
------------------------------------------
You can compile and run "guess","jsf2","conversation-sample" and "reservation "samples via maven Jetty plugin.
Go to the source bundle "samples/" directory. In the "guess/" or "reservation/" directory, run
the following maven commands. It will start up maven Jetty container. It bundles all of the
required jars into the WEB-INF/lib folder. You are not required to add any jar to the classpath.

Samples : Guess and Reservation
------------------------------
Go to the source folder of projects and run

> mvn clean install -Pjetty
> mvn jetty:run -Pjetty

Guess URL               : http://localhost:8080/guess
Reservation URL         : http://localhost:8080/reservation

Samples : Conversation Sample and JSF2
-------------------------------------
Go to the source folder of projects and run

>mvn clean install
>mvn jetty:run

Conversation Sample URL : http://localhost:8080/conversation-sample
JSF2 Sample URL         : http://localhost:8080/jsf2sample

------------------------------------------
Samples Run within Tomcat Plugin
------------------------------------------
OpenEJB samples are run with Maven Tomcat Plugin.

Tomcat Plugin uses http://localhost:8080/manager application to deploy war file
into your embeddable EJB Tomcat container. There must be an tomcat-users.xml
file in the "conf" directory of the server that contains manager role and username.

>Start Tomcat server if not started
>mvn tomcat:deploy

Ejb Sample URL    : http://localhost:8080/ejb-sample
Ejb Telephone URL : http://localhost:8080/ejb-telephone

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

-----------------------------------------
OpenWebBeans in Java SE
----------------------------------------
OpenWebBeans can perfeclty use in Java SE environment like Java Swing
applications. Standalone Samples is provided to show how to use OpenWebBeans
in Java SE.

Go to the source directory of the standalone sample:
>mvn clean package;
>cd target;
>jar -xvf standalone-sample.jar
>java -jar standalone-sample-1.0.0-SNAPSHOT.jar
>Enjoy :)

-----------------------------------------------
OpenWebBeans User and Development Mailing Lists
-----------------------------------------------
Please mail to the user mailing list about any questions or advice
about the OpenWebBeans.

User Mailing List : [users@openwebbeans.apache.org]

You can also join the discussions happening in the dev list

Dev Mailing List  : [dev@openwebbeans.apache.org]

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
http://openwebbeans.apache.org
---------------------------------------

OpenWebBeans Team

Enjoy!
