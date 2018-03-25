= TomEE EAP 1.7.6-TT.3

== Change log

=== Changes in TomEE EAP 1.7.4-SP.1

This security release fixes the following issues:

==== Denial of Service (DoS)

* CVE-2013-2160 (cxf-rt-core, apache-tomee) The streaming XML parser in Apache CXF 2.5.x before 2.5.10, 2.6.x before 2.6.7, and 2.7.x before 2.7.4 allows remote attackers to cause a denial of service (CPU and memory consumption) via crafted XML with a large number of (1) elements, (2) attributes, (3) nested constructs, and possibly other vectors.

* CVE-2017-12624 (cxf-rt-frontend-jaxrs, apache-tomee, cxf-api) Apache CXF supports sending and receiving attachments via either the JAX-WS or JAX-RS specifications. It is possible to craft a message attachment header that could lead to a Denial of Service (DoS) attack on a CXF web service provider. Both JAX-WS and JAX-RS services are vulnerable to this attack. From Apache CXF 3.2.1 and 3.1.14, message attachment headers that are greater than 300 characters will be rejected by default. This value is configurable via the property "attachment-max-header-size".

==== XML External Entity (XXE)

* CVE-2016-8739 (cxf-rt-rs-extension-providers, apache-tomee) The JAX-RS module in Apache CXF prior to 3.0.12 and 3.1.x prior to 3.1.9 provides a number of Atom JAX-RS MessageBodyReaders. These readers use Apache Abdera Parser which expands XML entities by default which represents a major XXE risk.

* CVE-2015-3208 (activemq-broker, apache-tomee) XML external entity (XXE) vulnerability in the XPath selector component in Artemis ActiveMQ before commit 48d9951d879e0c8cbb59d4b64ab59d53ef88310d allows remote attackers to have unspecified impact via unknown vectors.

* CVE-2015-0254 (openejb-jstl, apache-tomee) Apache Standard Taglibs before 1.2.3 allows remote attackers to execute arbitrary code or conduct external XML entity (XXE) attacks via a crafted XSLT extension in a (1) <x:parse> or (2) <x:transform> JSTL XML tag.

* CVE-2013-6440 (xmltooling, apache-tomee, wss4j) The (1) BasicParserPool, (2) StaticBasicParserPool, (3) XML Decrypter, and (4) SAML Decrypter in Shibboleth OpenSAML-Java before 2.6.1 set the expandEntityReferences property to true, which allows remote attackers to conduct XML external entity (XXE) attacks via a crafted XML DOCTYPE declaration.

==== Other

* CVE-2015-7559 (activemq-client, apache-tomee) Reserved CVE.

* CVE-2015-5254 (activemq-client, apache-tomee) Apache ActiveMQ 5.x before 5.13.0 does not restrict the classes that can be serialized in the broker, which allows remote attackers to execute arbitrary code via a crafted serialized Java Message Service (JMS) ObjectMessage object.

* CVE-2014-0107 (wss4j) The TransformerFactory in Apache Xalan-Java before 2.7.2 does not properly restrict access to certain properties when FEATURE_SECURE_PROCESSING is enabled, which allows remote attackers to bypass expected restrictions and load arbitrary classes or access external resources via a crafted (1) xalan:content-header, (2) xalan:entities, (3) xslt:content-header, or (4) xslt:entities property, or a Java property that is bound to the XSLT 1.0 system-property function.

* CVE-2016-6816 (tomcat-coyote, apache-tomee) The code in Apache Tomcat 9.0.0.M1 to 9.0.0.M11, 8.5.0 to 8.5.6, 8.0.0.RC1 to 8.0.38, 7.0.0 to 7.0.72, and 6.0.0 to 6.0.47 that parsed the HTTP request line permitted invalid characters. This could be exploited, in conjunction with a proxy that also permitted the invalid characters but with a different interpretation, to inject data into the HTTP response. By manipulating the HTTP response the attacker could poison a web-cache, perform an XSS attack and/or obtain sensitive information from requests other then their own.

* CVE-2017-12615 (tomcat-catalina, apache-tomee) When running Apache Tomcat 7.0.0 to 7.0.79 on Windows with HTTP PUTs enabled (e.g. via setting the readonly initialisation parameter of the Default to false) it was possible to upload a JSP file to the server via a specially crafted request. This JSP could then be requested and any code it contained would be executed by the server.

* CVE-2018-1305 (tomcat-catalina) Security constraints defined by annotations of Servlets in Apache Tomcat 9.0.0.M1 to 9.0.4, 8.5.0 to 8.5.27, 8.0.0.RC1 to 8.0.49 and 7.0.0 to 7.0.84 were only applied once a Servlet had been loaded. Because security constraints defined in this way apply to the URL pattern and any URLs below that point, it was possible - depending on the order Servlets were loaded - for some security constraints not to be applied. This could have exposed resources to users who were not authorised to access them.

* CVE-2018-1304 (tomcat-catalina) The URL pattern of "" (the empty string) which exactly maps to the context root was not correctly handled in Apache Tomcat 9.0.0.M1 to 9.0.4, 8.5.0 to 8.5.27, 8.0.0.RC1 to 8.0.49 and 7.0.0 to 7.0.84 when used as part of a security constraint definition. This caused the constraint to be ignored. It was, therefore, possible for unauthorised users to gain access to web application resources that should have been protected. Only security constraints with a URL pattern of the empty string were affected.

* CVE-2017-5648 (tomcat-catalina, apache-tomee) While investigating bug 60718, it was noticed that some calls to application listeners in Apache Tomcat 9.0.0.M1 to 9.0.0.M17, 8.5.0 to 8.5.11, 8.0.0.RC1 to 8.0.41, and 7.0.0 to 7.0.75 did not use the appropriate facade object. When running an untrusted application under a SecurityManager, it was therefore possible for that untrusted application to retain a reference to the request or response object and thereby access and/or modify information associated with another web application.

* CVE-2014-3603 (apache-tomee, opensaml) Reserved CVE.

* CVE-2016-6796 (tomcat-jasper, apache-tomee) A malicious web application running on Apache Tomcat 9.0.0.M1 to 9.0.0.M9, 8.5.0 to 8.5.4, 8.0.0.RC1 to 8.0.36, 7.0.0 to 7.0.70 and 6.0.0 to 6.0.45 was able to bypass a configured SecurityManager via manipulation of the configuration parameters for the JSP Servlet.

* CVE-2017-5647 (tomcat-coyote, apache-tomee) A bug in the handling of the pipelined requests in Apache Tomcat 9.0.0.M1 to 9.0.0.M18, 8.5.0 to 8.5.12, 8.0.0.RC1 to 8.0.42, 7.0.0 to 7.0.76, and 6.0.0 to 6.0.52, when send file was used, results in the pipelined request being lost when send file processing of the previous request completed. This could result in responses appearing to be sent for the wrong request. For example, a user agent that sent requests A, B and C could see the correct response for request A, the response for request C for request B and no response for request C.

* CVE-2016-6797 (tomcat-catalina, apache-tomee) The ResourceLinkFactory implementation in Apache Tomcat 9.0.0.M1 to 9.0.0.M9, 8.5.0 to 8.5.4, 8.0.0.RC1 to 8.0.36, 7.0.0 to 7.0.70 and 6.0.0 to 6.0.45 did not limit web application access to global JNDI resources to those resources explicitly linked to the web application. Therefore, it was possible for a web application to access any global JNDI resource whether an explicit ResourceLink had been configured or not.

* CVE-2016-6794 (tomcat-coyote, apache-tomee) When a SecurityManager is configured, a web application's ability to read system properties should be controlled by the SecurityManager. In Apache Tomcat 9.0.0.M1 to 9.0.0.M9, 8.5.0 to 8.5.4, 8.0.0.RC1 to 8.0.36, 7.0.0 to 7.0.70, 6.0.0 to 6.0.45 the system property replacement feature for configuration files could be used by a malicious web application to bypass the SecurityManager and read system properties that should not be visible.

* CVE-2014-3623  (apache-tomee, wss4j) Apache WSS4J before 1.6.17 and 2.x before 2.0.2, as used in Apache CXF 2.7.x before 2.7.13 and 3.0.x before 3.0.2, when using TransportBinding, does not properly enforce the SAML SubjectConfirmation method security semantics, which allows remote attackers to conduct spoofing attacks via unspecified vectors.

* CVE-2017-5664 (tomcat-catalina, tomcat-coyote, apache-tomee) The error page mechanism of the Java Servlet Specification requires that, when an error occurs and an error page is configured for the error that occurred, the original request and response are forwarded to the error page. This means that the request is presented to the error page with the original HTTP method. If the error page is a static file, expected behaviour is to serve content of the file as if processing a GET request, regardless of the actual HTTP method. The Default Servlet in Apache Tomcat 9.0.0.M1 to 9.0.0.M20, 8.5.0 to 8.5.14, 8.0.0.RC1 to 8.0.43 and 7.0.0 to 7.0.77 did not do this. Depending on the original request this could lead to unexpected and undesirable results for static error pages including, if the DefaultServlet is configured to permit writes, the replacement or removal of the custom error page. Notes for other user provided error pages: (1) Unless explicitly coded otherwise, JSPs ignore the HTTP method. JSPs used as error pages must must ensure that they handle any error dispatch as a GET request, regardless of the actual method. (2) By default, the response generated by a Servlet does depend on the HTTP method. Custom Servlets used as error pages must ensure that they handle any error dispatch as a GET request, regardless of the actual method.

* CVE-2015-0226 (apache-tomee, wss4j) Apache WSS4J before 1.6.17 and 2.0.x before 2.0.2 improperly leaks information about decryption failures when decrypting an encrypted key or message data, which makes it easier for remote attackers to recover the plaintext form of a symmetric key via a series of crafted messages.  NOTE: this vulnerability exists because of an incomplete fix for CVE-2011-2487.

* CVE-2015-0227 (apache-tomee, wss4j) Apache WSS4J before 1.6.17 and 2.x before 2.0.2 allows remote attackers to bypass the requireSignedEncryptedDataElements configuration via a vectors related to "wrapping attacks."

* CVE-2016-8745 (tomcat-coyote, apache-tomee) A bug in the error handling of the send file code for the NIO HTTP connector in Apache Tomcat 9.0.0.M1 to 9.0.0.M13, 8.5.0 to 8.5.8, 8.0.0.RC1 to 8.0.39, 7.0.0 to 7.0.73 and 6.0.16 to 6.0.48 resulted in the current Processor object being added to the Processor cache multiple times. This in turn meant that the same Processor could be used for concurrent requests. Sharing a Processor can result in information leakage between requests including, not not limited to, session ID and the response body. The bug was first noticed in 8.5.x onwards where it appears the refactoring of the Connector code for 8.5.x onwards made it more likely that the bug was observed. Initially it was thought that the 8.5.x refactoring introduced the bug but further investigation has shown that the bug is present in all currently supported Tomcat versions.

* CVE-2017-5656 (apache-tomee, cxf-rt-ws-security) Apache CXF's STSClient before 3.1.11 and 3.0.13 uses a flawed way of caching tokens that are associated with delegation tokens, which means that an attacker could craft a token which would return an identifer corresponding to a cached token for another user.

* CVE-2009-2625 (wss4j) XMLScanner.java in Apache Xerces2 Java, as used in Sun Java Runtime Environment (JRE) in JDK and JRE 6 before Update 15 and JDK and JRE 5.0 before Update 20, and in other products, allows remote attackers to cause a denial of service (infinite loop and application hang) via malformed XML input, as demonstrated by the Codenomicon XML fuzzing framework.

* CVE-2017-3156 (cxf-rt-rs-security-oauth2, apache-tomee) The OAuth2 Hawk and JOSE MAC Validation code in Apache CXF prior to 3.0.13 and 3.1.x prior to 3.1.10 is not using a constant time MAC signature comparison algorithm which may be exploited by sophisticated timing attacks.

* CVE-2016-5018 (tomcat-jasper, apache-tomee) In Apache Tomcat 9.0.0.M1 to 9.0.0.M9, 8.5.0 to 8.5.4, 8.0.0.RC1 to 8.0.36, 7.0.0 to 7.0.70 and 6.0.0 to 6.0.45 a malicious web application was able to bypass a configured SecurityManager via a Tomcat utility method that was accessible to web applications.

* CVE-2013-2035 (apache-tomee, jansi) Race condition in hawtjni-runtime/src/main/java/org/fusesource/hawtjni/runtime/Library.java in HawtJNI before 1.8, when a custom library path is not specified, allows local users to execute arbitrary Java code by overwriting a temporary JAR file with a predictable name in /tmp.

* CVE-2016-6812 (apache-tomee, cxf-rt-transports-http) The HTTP transport module in Apache CXF prior to 3.0.12 and 3.1.x prior to 3.1.9 uses FormattedServiceListWriter to provide an HTML page which lists the names and absolute URL addresses of the available service endpoints. The module calculates the base URL using the current HttpServletRequest. The calculated base URL is used by FormattedServiceListWriter to build the service endpoint absolute URLs. If the unexpected matrix parameters have been injected into the request URL then these matrix parameters will find their way back to the client in the services list page which represents an XSS risk to the client.

* CVE-2016-0762 (tomcat-catalina, apache-tomee) The Realm implementations in Apache Tomcat versions 9.0.0.M1 to 9.0.0.M9, 8.5.0 to 8.5.4, 8.0.0.RC1 to 8.0.36, 7.0.0 to 7.0.70 and 6.0.0 to 6.0.45 did not process the supplied password if the supplied user name did not exist. This made a timing attack possible to determine valid user names. Note that the default configuration includes the LockOutRealm which makes exploitation of this vulnerability harder.

* CVE-2017-7674 (tomcat-catalina, apache-tomee) The CORS Filter in Apache Tomcat 9.0.
