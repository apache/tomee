-------------------------------
Apache OpenWebBeans 1.0.0
-------------------------------
Welcome! 
Thanks for downloading and using OpenWebBeans. 
This document is a "Getting Started Guide" for OpenWebBeans.

This document is based on the 1.0.0 release of the OpenWebBeans.

--------------------------------
What is Apache  OpenWebBeans?
--------------------------------
OpenWebBeans is an ASL 2.0-licensed implementation of the JSR-299, Contexts and Dependency Injection for the Java EE platform.

Project's web page can be found at: 
"http://openwebbeans.apache.org"

The projects WIKI page can be found at:
https://cwiki.apache.org/confluence/display/OWB/Index


--------------------------------
OpenWebBeans 1.0.0 Release Features
--------------------------------

- 1.0.0 release supports the following features
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
* @Alternative support
* OSGi environment support with an own plugable bundle ClassPath scanner
* vastly improved Interceptor performance
* improved Serialization handling
* improved CreationalContext handling
* revised internal logging
* upgrade to JPA-2 and JSF-2 support
* support for direct CDI usage in tomcat-6 and tomcat-7 environments



- 1.0.0 release does not support the following features
--------------------------------------------
* Does not fully integrate with Geronimo 
 (You have to configure your application to use OWB functionality, 
  e.g configure web.xml, adding interceptor to EJBs etc.)


Noteable differences to CDI spec behaviour
--------------------------------------------

In a few special cases Apache OpenWebBeans might behave a little bit different than
other CDI implementations. This is to some degree caused by the JSR-299 spec being
not clear about some special topics so we needed to interpret the wording on our own.
This mainly concerns the area of section 12.1 Bean Archives (BDA) which doesn't work
out when it comes to OSGi containers and likes.
In Apache OpenWebBeans, a settings configured in a beans.xml file of a BDA is not
only effective for this very bean archive but for the whole BeanManager in control
of the Application. This is especially the case for <alternatives> and
<interceptors>! An Alternative, Interceptor or Decorator enabled in one BDA is active
for the whole Application.


-------------------------------------------
Release Notes - OpenWebBeans - Version 1.0.0
-------------------------------------------

Bug

    * [OWB-318] - multiple methods with same EJB @interceptors(foo.class) in same bean class get multiple interceptor instances
    * [OWB-384] - OWB needs to call 299-defined PrePassivate, PostActivate, and AroundTimeout interceptors for EJBs
    * [OWB-422] - Support needed for PrePassivate, PostActivate, and AroundTimeout via EJBInterceptor.
    * [OWB-429] - OpenWebBeansEjbPlugin Class Hierarchy
    * [OWB-438] - Cached Normal Scoped Proxy instances
    * [OWB-439] - EjbPlugin session bean proxy creation thread safe problem
    * [OWB-445] - we must not use javassist ProxyFactory#setHandler(MethodHandler)
    * [OWB-446] - EJB lifecycle callbacks not stacked correctly
    * [OWB-450] - NullPointerException in DependentScopedBeanInterceptorHandler when it has a NullCreationalContext (normally from a EE component).
    * [OWB-454] - ClassUtil.callInstanceMethod() doesn't propogate original exception
    * [OWB-455] - IllegalArgument method calling remove method of EJB during destroy
    * [OWB-456] - When multiple interceptors are defined for a bean OWB does NOT correctly remove the overriden base Interceptors
    * [OWB-457] - we must not create a SessionContext for static resource reqeusts
    * [OWB-460] - fix owb-openejb and owb-ejb artifactIds
    * [OWB-464] - InjectionPointImpl using wrong class loader during serialize/deserialize, dropping qualifiers, and omiting qualifier values.
    * [OWB-466] - Ensure removal of all ThreadLocal values

Improvement

    * [OWB-177] - Handling of InterceptionType#POST_ACTIVATE, PRE_PASSIVATE and AROUND_TIMEOUT is missing
    * [OWB-407] - detailed information about exceptions
    * [OWB-451] - Allow InterceptorUtil#callAroundInvokes to propogate a callers 'creational context key'
    * [OWB-459] - upgrade to newer library versions
    * [OWB-463] - EjbDefinitionUtility.defineEjbBeanProxy() should be able to create proxies for no-interface local beans
    * [OWB-465] - enhance EJB common code for crude @LocalBean support

TCK Challenge

    * [OWB-394] - Any idea why our BeforeBeanDiscovery.addInterceptorBinding() has different signature?

Task

    * [OWB-453] - add a flag to disable context activation in EJB interceptor
    * [OWB-462] - Refactor AnnotationUtil.hasAnnotationMember()


-------------------------------------------
Release Notes - OpenWebBeans - Version 1.0.0-alpha-2
-------------------------------------------
Bug

    * [OWB-303] - upgrade Javassist to a newer version
    * [OWB-338] - our internal SessionContext and ConversationContext must support Session serialization
    * [OWB-385] - implement passivation of managed beans in ServletContextListener
    * [OWB-401] - ELContextStore not cleaned up for some JSP EL lookups
    * [OWB-402] - OpenWebBeansJsfPlugin does not recognize @ManagedBean
    * [OWB-404] - Contexts must not get stored in a static Map in BeanManager
    * [OWB-405] - AnnotatedElementFactory must not use static cache maps
    * [OWB-408] - NPE in WebBeansELResolver
    * [OWB-415] - EjbBeanProxyHandler for dependent ejb must save ejb instance
    * [OWB-416] - BaseEJBBean.destroyComponentInstance() tries to call multiple remove methods
    * [OWB-418] - EjbBeanProxyHandler must be 1:1 with proxy instances for dependent SFSB
    * [OWB-419] - fix @Dependent handling in ELResolver
    * [OWB-420] - SingletonContext is mapped to @ConversationScoped
    * [OWB-421] - defined-in-class EJB lifecycle callbacks masked by our Interceptor
    * [OWB-426] - Tweak EJBPlugin to work with Standalone Tests
    * [OWB-431] - Generic Type Inheritance not resolved correctly
    * [OWB-434] - ThreadLocal<SingletonContext> doen't get cleaned up
    * [OWB-436] - AbstractContext bean instance creation is not thread safe
    * [OWB-437] - Improve AbstractContext synchronization
    * [OWB-440] - WebBeansDecoratorConfig.getDecoratorStack always returns new Decorators
    * [OWB-442] - our EJB proxies are broken when multiple local interfaces are used on a single class
    * [OWB-443] - Normal-scoped EJB not removed by container during Contextual.destroy()

Improvement

    * [OWB-57] - cleanup problems found by maven-findbugs-plugin
    * [OWB-195] - Give warning to the developer related with non- portable operations
    * [OWB-409] - create a name for OWB our faces-config.xml
    * [OWB-410] - lazy initialisation of ejbInterceptors
    * [OWB-411] - cache calls to isNormalScope()
    * [OWB-412] - Allow container specific extensions to WebConfigurationListener access to the lifecycle
    * [OWB-413] - cache calls to ClassUtils#getObjectMethodNames()
    * [OWB-414] - improve Interceptor performance
    * [OWB-425] - improve performance of owb-el-resolver
    * [OWB-427] - improve read-performance of AbstractOwbBean
    * [OWB-430] - improve performance of WebBeansPhaseListener
    * [OWB-432] - Create Singleton Service SPI
    * [OWB-441] - new configuration properties mechanism

Sub-task

    * [OWB-193] - If an interceptor or decorator has any scope other than @Dependent, non-portable behavior results.
    * [OWB-194] - If an interceptor or decorator has a name, non-portable behavior results.
    * [OWB-196] - If an interceptor or decorator is an alternative, non-portable behavior results.
    * [OWB-197] - If a stereotype declares any other qualifier an- notation, non-portable behavior results.
    * [OWB-198] - If a stereotype is annotated @Typed, non-portable behavior results.


TCK Challenge

    * [OWB-424] - Adding Document for How to Configure and Run TCK (standalone and web profile TCKs)


-------------------------------------------
Release Notes - OpenWebBeans - Version 1.0.0-alpha-1
-------------------------------------------
Bug

    * [OWB-216] - Update pom.xml svn links
    * [OWB-231] - exception using abstract decorators
    * [OWB-245] - Using parameterized type varaibles fails for Producer Method injection
    * [OWB-259] - Implement spec 11.5.5. ProcessModule event
    * [OWB-289] - Owb return 2 beans for Indirect specialized producer beans
    * [OWB-302] - InjectionPoint injections (both method and field based) in Decorators result in null
    * [OWB-312] - Add dopriv's to allow OWB to function with java 2 security enabled
    * [OWB-317] - creationalContext in InvocationContextImpl is always null
    * [OWB-318] - multiple methods with same EJB @interceptors(foo.class) in same bean class get multiple interceptor instances
    * [OWB-327] - annotating an Interceptor with @ApplictionScoped leads to OutOfMemory
    * [OWB-329] - Interceptor instances get created each time the interceptor gets called
    * [OWB-332] - Destroy Depdent Of Producer Method Beans when Invocation Completes
    * [OWB-333] - InjectionTarget and Producer Handling
    * [OWB-334] - cid is missing when using redirect for a jsf 2.0 application
    * [OWB-336] - injected BeanManager must be Serializable
    * [OWB-337] - events must not get broadcasted to beans which have no active Context
    * [OWB-339] - Injecting Non-Contextual Beans Causes NPE in WebBeansUtil
    * [OWB-340] - BeanManagerImpl.createInjectionTarget() Throws Exception When No Constructor Found
    * [OWB-341] - CreationalContext#incompleteInstance should get cleaned after create()
    * [OWB-342] - InterceptorHandler crashes with NullPointerException after deserialisation
    * [OWB-343] - upgrade JPA spec from 1.0-PFD2 to 1.0 final revision
    * [OWB-345] - Remove duplicate dependencies
    * [OWB-351] - OWB picks up @SessionScoped contextual instances from expired sessions
    * [OWB-352] - Thread Safety Problem in our InterceptorHandlers, aka proxies
    * [OWB-353] - NPE in removeDependents@CreationalContextImpl
    * [OWB-354] - WebContextService may throw NPE in tiered classloading environmemt
    * [OWB-357] - WebbeansFinder should index first on ClassLoader, not singleton type
    * [OWB-359] - ownerCreationalContext sometimes causes NPE in InterceptorDataImpl.createNewInstance()
    * [OWB-361] - underlying EJB method not actually in our interceptors stack
    * [OWB-362] - InvocationTargetException when invoking Stateless SessionBean
    * [OWB-363] - Intermittent bug with ApplicationScope disposers not being called
    * [OWB-366] - ContextNotActiveException fired from AppScope/NormalScopedBeanInterceptorHandler when a proxied object finalized
    * [OWB-368] - The 299 spec (that I have) uses receive=IF_EXISTS but OWB uses notifyObserver=IF_EXISTS.
    * [OWB-369] - Static ContextsService in ContextFactory causes wrong webContextService used for multiple applications
    * [OWB-370] - Intransient Conversation context get rdestroyed randomly by destroyWithRespectToTimout
    * [OWB-371] - no lifecycle interceptors for non-contextual EJB
    * [OWB-372] - creational context not cleaned up for non-contextual EJB interceptions
    * [OWB-373] - build crashes with missing artifact error
    * [OWB-374] - migrate jsf2sample from sun to MyFaces
    * [OWB-376] - [patch] Guess example broken with Jetty plugin 6.x due to EL 2.2
    * [OWB-377] - revise logging
    * [OWB-378] - ejb at bottom of decorator stack doesn't handle changed method
    * [OWB-380] - NormalScopedBeanInterceptorHandler throws NPE when handling 3rd party Contexts
    * [OWB-381] - NPE thrown from AbstractInjectable if dependent producer returns null
    * [OWB-382] - injecting a @Dependent bean into a passivatation scoped bean causes a NonSerializableException
    * [OWB-387] - DependentContext Interceptor Double Call for PostConstruct
    * [OWB-390] - fix broken links in our 'site'
    * [OWB-396] - fix poms to work with maven 3
    * [OWB-398] - DelegateHandler cached too agressively
    * [OWB-399] - Proxy objects could not be correctly deserialized by using javassist 3.11. we need to update to 3.12
    * [OWB-400] - starting OWB as part of an EAR in geronimo causes a exception due to missing 'bundle' protocol

Improvement

    * [OWB-116] - Update Business Method Definition
    * [OWB-118] - Supports Decorators for Other Delegate Injections
    * [OWB-136] - fix 'broken' license headers in our java files
    * [OWB-170] - Address findbug issues in webbeans-impl
    * [OWB-183] - Improve webbeans-doc module to get a documentation more user friendly
    * [OWB-214] - get rid of javax.transaction.Transaction dependency in webbeans-impl
    * [OWB-237] - NoSuchElementException when WebBeansConfigurationListener is absent
    * [OWB-275] - remove unused imports and cleanup code
    * [OWB-286] - java.lang.NoClassDefFoundError: javax/validation/Validator
    * [OWB-313] - create caching strategies for resolving Bean<T> for BeanManager and EL invocations
    * [OWB-314] - cache resolved instances in NormalScopedBeanMethodHandlers of @ApplicationScoped beans
    * [OWB-315] - cache resolved instances in NormalScopedBeanMethodHandlers of @SessionScoped beans
    * [OWB-319] - Strange logging when writing non-Serializable SessionScoped bean
    * [OWB-320] - Remove Java EE Dependencies from WebBeans Core
    * [OWB-322] - Create new EJB project and separate common EJB classes from OpenEJB plugin
    * [OWB-325] - Relocate SPI Classes to SPI Module. Change JSR299, JSR330 as optional pom dependency.
    * [OWB-326] - improve producer tests
    * [OWB-328] - improve logger performance
    * [OWB-330] - reduce BeanManagerImpl#getManager() calls inside the same functions
    * [OWB-331] - Cache Interceptor & Decorator Stack oon Interceptor Handler
    * [OWB-346] - Make EJB samples running
    * [OWB-347] - Using InjectableBeanManager in TCK
    * [OWB-349] - ignore exception during type hierarchy scan
    * [OWB-350] - Support Interceptor for non-contextual EJBs
    * [OWB-355] - OpenEjbBean should look for @Remove methods
    * [OWB-356] - EjbPlugin only looks for DeployementInfo once, so new deployed application won't be discovered
    * [OWB-358] - provide property to skip injection in @PostConstruct of OpenWebBeansEjbInterceptor
    * [OWB-360] - Add BeanManager to a ServletContext attribute
    * [OWB-364] - Reduce the amount of info level logging
    * [OWB-365] - make injection optional in OWBEJBInterceptor
    * [OWB-375] - Performance: OWB logging performs operations when logging disabled.
    * [OWB-379] - upgrade to final atinject-spec artifact
    * [OWB-386] - upgarde CDI TCK to 1.0.2.CR1
    * [OWB-389] - atinject-tck upgrade to final 1.0 release
    * [OWB-397] - Add helper method and some debug to WebBeansFinder

New Feature

    * [OWB-316] - Implement a generic TestContainer for CDI implementations
    * [OWB-323] - Provide methods to pass classloader into ServiceLoader and WebBeansFinder for use in tiered classloader situations
    * [OWB-324] - Add Tomcat Plugin
    * [OWB-348] - Adding Interceptor and Decorator Support for EJB Beans
    * [OWB-395] - OpenWebBeans Tomcat 7 Support

TCK Challenge

    * [OWB-388] - Pass TCK 1.0.2 CR1 Web Profile

Task

    * [OWB-6] - Scope passivation
    * [OWB-14] - Update WebBeans Lifecycle for Servlet Beans
    * [OWB-46] - Injection into non-contextual objects
    * [OWB-204] - Update Samples for JSF2 Usage
    * [OWB-220] - Update site.xml links and bread crumbs to point to non-incubator.
    * [OWB-310] - Drop dom4j and use jre builtin xml parsers for processing beans.xml
    * [OWB-391] - create a owb-build-tools project to maintain project specific checkstyle rules, etc.

Test

    * [OWB-56] - Integrate the official JSR-299 TCK test suite
    * [OWB-222] - Update website download link, and fix relative URL translation
    * [OWB-367] - Add a unit test for IF_EXISTS


----------------------------------------------
Required Platform
----------------------------------------------
Java Version : Java SE >= 6.0
Java EE Must : Java EE >= 5.0

---------------------------------------------
How to Configure OpenWebBeans
---------------------------------------------

This section explains a content of the distribution bundle, OWB plugins and its
dependent libraries. 

---------------------------------------------
1.0.0 Distribution Content
---------------------------------------------
There are several jars in the OpenWebBeans 1.0.0 distribution;

 - openwebbeans-impl-1.0.0.jar     --> Includes Core Dependency Injection Service.
 - openwebbeans-ejb-1.0.0.jar      --> EJB Plugin(Supports EJBs in OpenEJB embedded in Tomcat).
 - openwebbeans-openejb-1.0.0.jar  --> OpenEJB specific Plugin SPI implementations as extension to openwebbeans-ejb
 - openwebbeans-jms-1.0.0.jar      --> JMS Plugin(Supports injection of JMS related artifacts,i.e, ConnectionFactory, Session, Connection etc.)
 - openwebbeans-jsf-1.0.0.jar      --> JSF-2.0 Plugin(JSF Conversation Scoped Support).
 - openwebbeans-jsf12-1.0.0.jar    --> JSF-1.2 Plugin(JSF Conversation Scoped Support).
 - openwebbeans-resource-1.0.0.jar --> Java EE Resource Injection for Web Projects (Includes @PersistenceContext,@PersistenceUnit
                                          and @Resource injection into the Managed Beans. @Resource injections use java:/comp/env of the
                                          Web application component. @PersistenceContext is based on extended EntityManager.
 - openwebbeans-spi-1.0.0.jar      --> OpenWebBeans Server Provider Interfaces. They are implemented by runtime environments that would
                                          like to use OpenWebBeans as a JSR-299 implementation.
 - samples                            --> Includes source code of the samples. Samples are mavenized project  therefore you can easily build and run
                                          them from your environment that has maven runtime.
 - openwebbeans-osgi-1.0.0.jar     --> ClassPath ScannerService SPI implementation for OSGI environments like Apache Geronimo-3
 - openwebbeans-web-1.0.0.jar      --> Basic Servlet integration
 - openwebbeans-tomcat6-1.0.0.jar  --> Support for deeper integration into Apache Tomcat-6




------------------------------------------
How OWB Plugins Work
------------------------------------------

OpenWebBeans has been developed with a plugin architecture. The Core dependency injection service
is provided with openwebbeans-impl. If you need further service functionality, 
you have to add respective plugin jars into the application classpath. OpenWebBeans 
uses the Java SE 6.0 java.util.ServiceLoader mechanism to pickup plugins at runtime.

Current Plugins:
---------------------
Look at "1.0.0 Distribution Content" above.

------------------------------------------
Dependent Libraries
------------------------------------------

Third Party jars:
-----------------
They are necessary at runtime in the Core Implementation.

javassist : Version 3.12.0.GA
scannotation : Version 1.0.2 (if not running in an OSGi environment like Apache Geronimo-3)

Java EE APIs jars(Container Provider Libraries) :
-------------------------------------------------
Generally full Java EE servers provides these jars. But web containers like Tomcat or Jetty
do not contain some of them, such as JPA, JSF, Validation API etc. So, if you do not want to bundle
these libraries within your application classpath, you have to include these libraries in your
server common classpath.

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
Third party        : javassist, scannotation, openwebbeans-spi
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
for Java Web Profile Plugin or Java SE Profile Plugin. Stay Tuned!

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
you could add the JSR-299 API and JSR-330 API into the server common classpath, and
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
properties. Default configuration files are embedded into OWB implementation
jar files. Instead of opening the jars file and changing configuration properties, simply add
an "openwebbeans.properties" file into a "META-INF/openwebbeans" folder of your application
classpath. This will override the values from the default configuration.
You can specify a property 'configuraion.ordinal' in this file to define the overlay order.
A properties file with higher 'configuration.ordinal' value will applied later and thus
have a higher precedence. If you don't specify a 'configuration.ordinal' a value of 100 is assumed;
This allows to have multiple openwebbeans.properties files e.g. a common one in an EAR lib
(with configuration.ordinal=100) and more specific ones for each WebApp in your EAR (with a
configuration.ordinal of e.g. 101).

Each plugin developer can provide their own SPI implementation class and own configuration values. If you would like
to use those implementation classes or configuration values, you have to override the default configuration file as explained
in the above paragraph, i.e, putting "openwebbeans.properties" file into "META-INF/openwebbeans" folder of your application.
It is recommended to use a 'configuration.ordinal' between 50 and 99 for custom SPI implementations.

Below are OpenWebBeans' default configuration properties from our openwebbeans-impl.jar file and our plugins such as
our OpenEJB plugin.

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

- "org.apache.webbeans.spi.FailOverService"
   Description   : Implementation of the org.apache.webbeans.spi.FailOverService. It is used for enabling passivation/failover beans.
   Values        : org.apache.webbeans.web.failover.DefaultOwbFailOverService or CUSTOM
   Default       : org.apache.webbeans.web.failover.DefaultOwbFailOverService

- "org.apache.webbeans.web.failover.issupportfailover"
   Description   : Support failover of beans or not
   Values        : false, true
   Default       : false

- "org.apache.webbeans.web.failover.issupportpassivation"
   Description   : Support passivation of beans or not
   Values        : false, true
   Default       : false

- "org.apache.webbeans.forceNoCheckedExceptions"
   Description   : The interceptors spec defines that @PostConstruct & Co must not throw checked Exceptions. Setting this configuration to 'false' disables this check.
   Values        : false, true
   Default       : true


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

You could look at ejb-sample.war for "WEB-INF/lib" libraries as an example to develop custom applications.
The source of this project is also available.

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


8) "Standalone Sample" : Shows usage of OpenWebBeans in Standalone Swing Application.
Look at "OpenWebBeans in Java SE" section.

Configuring and Running the Applications:
--------------------------------------------
See section Compile and Run Samples via Jetty&Tomcat Plugin.

--------------------------------------------
Maven Install and Package From the Source
--------------------------------------------

Maven Version : Apache Maven 2.2.1 or later

First you have to download the "source" version of the OpenWebBeans project that
contains the all source code of the OpenWebBeans.

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
OpenWebBeans can be used in Java SE environments such as Java Swing
applications. A Standalone Sample is provided to show how to use OpenWebBeans
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
Please mail to the user mailing list with any questions or advice
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

Your OpenWebBeans Team

Enjoy!
