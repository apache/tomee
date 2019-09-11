= TomEE EAP 1.7.6-TT.14

== Change log

=== Changes in TomEE EAP 1.7.6-TT.14

* TOMEE-2532 Fix deployment issue where javax.security.jacc.policy.provider is set
* SONATYPE-2018-0605 - update CXF to 2.6.17-TT.4 to mitigate SONATYPE-2018-0605
* Update Quartz to mitigate CVE-2019-13990
* Update Beanutils to mitigate CVE-2019-10086

=== Changes in TomEE EAP 1.7.6-TT.13

* TOMEE-2510 Update to Tomcat 7.0.94 to mitigate CVE-2019-0232

=== Changes in TomEE EAP 1.7.6-TT.12

This release fixes the following issue:

* CVE-2015-1796 - The PKIX trust engines in Shibboleth Identity Provider before 2.4.4 and OpenSAML Java (OpenSAML-J) before 2.6.5 trust candidate X.509 credentials when no trusted names are available for the entityID, which allows remote attackers to impersonate an entity via a certificate issued by a shibmd:KeyAuthority trust anchor.

=== Changes in TomEE EAP 1.7.6-TT.11

* TOMEE-2256 Update to Tomcat 7.0.91 to mitigate CVE-2018-11784
* TOMEE-2257 improve connection proxy handling, and warning when connections are abandoned

=== Changes in TomEE EAP 1.7.6-TT.10

* Update to ActiveMQ 5.14.6-TT.1 to mitigate CVE-2018-11775
* TOMEE-2230 Expose details of Connection Factories and Resource Adapters via JMX
* TOMEE-2245 Null attributes in MBeanPojoWrapper
* TOMEE-2237 MessageDrivenContext not injected into MDBs when using new pooling

=== Changes in TomEE EAP 1.7.6-TT.8

* Update to Tomcat 7.0.90
* Update to CXF 2.6.17-TT.3 to mitigate CVE-2018-8039

=== Changes in TomEE EAP 1.7.6-TT.7

This release fixes the following issue:

* CVE-2018-8014

The defaults settings for the CORS filter are insecure and enable 'supportsCredentials' for all origins. It is expected that users of the CORS filter will have configured it appropriately for their environment rather than using it in the default configuration. Therefore, it is expected that most users will not be impacted by this issue.

* Update OpenJPA to 2.4.3

=== Changes in TomEE EAP 1.7.6-TT.5

This release fixes the following issue:

* TOMEE-2190 check catalina base as opposed to catalina home for .ear and .rar files

=== Changes in TomEE EAP 1.7.6-TT.4

This release fixes the following issues:

* TOMEE-1694 resolve memory leak with WS http upgrade handler

=== Changes in TomEE EAP 1.7.6-TT.1

MYFACES-4133 - Don't deserialize the ViewState-ID if the state saving method is server
TOMEE-2158 update Majorra library to 2.1.29-09

=== Changes in TomEE EAP 1.7.5-TT.18

TOMEE-2145 - fix double deploy issue when deploying an EAR from the webapps directory
TOMEE-2149 - Allow beforeDelivery/afterDelivery calls with message delivery in between, as per JSR-322 section 13.5.6
TOMEE-2151 - Use correct classloader for creating application resources specified in resources.xml. Added examples and arquillian tests

=== Changes in TomEE EAP 1.7.5-TT.17

CVE-2017-12624 - Apache CXF web services that process attachments are vulnerable to Denial of Service (DoS) attacks. Message attachment headers that are greater than 300 characters will be rejected by default. This value is configurable
via the property "attachment-max-header-size".

=== Changes in TomEE EAP 1.7.5-TT.16

This release has the following changes:

* TOMEE-1574 - Backport from TomEE 7.0.x improvement to allow <Container> and <Service> definitions in an application's resources.xml
* TOMEE-2141 - Logging EJB bean instance creation and removal

=== Changes in TomEE EAP 1.7.5-TT.15

This security release fixes the following issues:

* CVE-2017-12617 (Tomcat) Apache Tomcat is vulnerable to Remote Code Execution. The AbstractFileResourceSet class allows arbitrary files to be uploaded when running on the Windows operating system. A remote attacker can craft a PUT request that will upload a JSP file with malicious code. After uploading, the attacker can then request the file from the server causing the malicious code to execute on the server.

=== Changes from Tomcat 7.0.82

==== Catalina

* fix 61210: When running under a SecurityManager, do not print a warning about not being able to read a logging configuration file when that file does not exist. (markt)
* add 61280: Add RFC 7617 support to the BasicAuthenticator. Note that the default configuration does not change the existing behaviour. (markt)
* fix 61452: Fix a copy paste error that caused an UnsupportedEncodingException when using WebDAV. (markt)
* fix Correct regression in 7.0.80 that broke the use of relative paths with the extraResourcePaths attribute of a VirtualDirContext. (markt)
* add 61489: When using the CGI servlet, make the generation of command line arguments from the query string (as per section 4.4 of RFC 3875) optional. The feature is enabled by default for consistency with previous releases. Based on a patch by jm009. (markt)
* fix Correct a regression in 7.0.80 and 7.0.81 that wrapped the DirContext that represented the web application in a ProxyDirContext twice rather than just once. (markt)
* fix 61542: Fix CVE-2017-12617 and prevent JSPs from being uploaded via a specially crafted request when HTTP PUT was enabled. (markt)
* fix Use the correct path when loading the JVM logging.properties file for Java 9. (rjung)
* fix 61554: Exclude test files in unusual encodings and markdown files intended for display in GitHub from RAT analysis. Patch provided by Chris Thistlethwaite. (markt)

==== Coyote

* fix 48655: Enable Tomcat to shutdown cleanly when using sendfile, the APR/native connector and a multi-part download is in progress. (markt)
* fix 58244: Handle the case when OpenSSL resumes a TLS session using a ticket and the full client certificate chain is not available. In this case the client certificate without the chain will be presented to the application. (markt)
* fix Fix random SocketTimeoutExceptions when reading the request InputStream. Based on a patch by Peter Major. (markt)
* fix 60900: Avoid a NullPointerException in the APR Poller if a connection is closed at the same time as new data arrives on that connection. (markt)
* add Add an option to reject requests that contain HTTP headers with invalid (non-token) header names with a 400 response. (markt)

==== WebSocket

* fix 61491: When using the permessage-deflate extension, correctly handle the sending of empty messages after non-empty messages to avoid the IllegalArgumentException. (markt)

==== Tribes

* fix To avoid unexpected session timeout notification from backup session, update the access time when receiving the map member notification message. (kfujino)
* fix Add member info to the log message when the failure detection check fails in TcpFailureDetector. (kfujino)
* fix Avoid Ping timeout until the added map member by receiving MSG_START message is completely started. (kfujino)
* fix When sending a channel message, make sure that the Sender has connected. (kfujino)
* fix Correct the backup node selection logic that node 0 is returned twice consecutively. (kfujino)
* fix Fix race condition of responseMap in RpcChannel. (kfujino)

==== jdbc-pool

* fix 61391: Ensure that failed queries are logged if the SlowQueryReport interceptor is configured to do so and the connection has been abandoned. Patch provided by Craig Webb. (markt)
* fix 61425: Ensure that transaction of idle connection has terminated when the testWhileIdle is set to true and defaultAutoCommit is set to false. Patch provided by WangZheng. (kfujino)
* fix 61545: Correctly handle invocations of methods defined in the PooledConnection interface when using pooled XA connections. Patch provided by Nils Winkler. (markt)

==== Other

* fix 61439: Remove the Java Annotation API classes from tomcat-embed-core.jar and package them in a separate JAR in the embedded distribution to provide end users with greater flexibility to handle potential conflicts with the JRE and/or other JARs. (markt)
* fix 61441: Improve the detection of JAVA_HOME by the daemon.sh script when running on a platform where Java has been installed from an RPM. (rjung)
* update Update the packaged version of the Tomcat Native Library to 1.2.14 to pick up the latest Windows binaries built with APR 1.6.2 and OpenSSL 1.0.2l. (markt)
* fix Update fix for 59904 so that values less than zero are accepted instead of throwing a NegativeArraySizeException. (remm)
* fix 61563: Correct typos in Spanish translation. Patch provided by Gonzalo Vásquez. (csutherl)
