= TomEE EAP 7.0.6-TT.2

=== Changes in TomEE EAP 7.0.6-TT.2

This release has the following changes:

* CVE-2018-1000180 - Update to Bouncy Castle 1.60

Bouncy Castle BC 1.54 - 1.59, BC-FJA 1.0.0, BC-FJA 1.0.1 and earlier have a flaw in the Low-level interface to RSA key pair generator, specifically RSA Key Pairs generated in low-level API with added certainty may have less M-R tests than expected. This appears to be fixed in versions BC 1.60 beta 4 and later, BC-FJA 1.0.2 and later.

* CVE-2018-8034 - Update to Tomcat 8.5.32

Apache Tomcat Websocket - Host Name Verification Missing in WebSocket Client

The Apache Tomcat component is vulnerable to Man-in-the-Middle (MitM) attacks. The connectToServerRecursive and createSSLEngine methods in WsWebSocketContainer.java that are used by the WebSockets client do not validate the hostname of SSL certificates. A remote attacker can exploit this behavior to spoof a legitimate server to perform a MitM attack.

* CVE-2018-8037 - Update to Tomcat 8.5.32

Apache Tomcat is vulnerable to Session Hijacking. The isClosed() method in the NioSocketWrapper and Nio2SocketWrapper classes, which is accessed from multiple threads, does not use volatile variables to store the closed field of their associated Socket objects. This means that one thread might query whether the Socket is closed while another thread is in the process of closing it; this results in a race condition, wherein a user session might be reused with a Socket that formerly belonged to another user, and was presumed closed. Because the Socketbelonged to another user, the connection and session remains active. An attacker can exploit this vulnerability by attempting to trigger the race condition, allowing the attacker to possibly take control of another user's session. Note that the attacker cannot choose which session to hijack; it is entirely dependent on the non-deterministic nature of the race condition.


=== Changes in TomEE EAP 7.0.5-TT.3

This release has the following changes:

* Update to Johnzon 1.0.1 and Tomcat 8.5.31-TT.1.

* CVE-2018-8014

The defaults settings for the CORS filter are insecure and enable 'supportsCredentials' for all origins. It is expected that users of the CORS filter will have configured it appropriately for their environment rather than using it in the default configuration. Therefore, it is expected that most users will not be impacted by this issue.

=== Changes in TomEE EAP 7.0.5-TT.2

This release has the following changes:

* CVE-2018-1304

The URL pattern of "" (the empty string) which exactly maps to the context root was not correctly handled in Apache Tomcat 9.0.0.M1 to 9.0.4, 8.5.0 to 8.5.27, 8.0.0.RC1 to 8.0.49 and 7.0.0 to 7.0.84 when used as part of a security constraint definition. This caused the constraint to be ignored. It was, therefore, possible for unauthorised users to gain access to web application resources that should have been protected. Only security constraints with a URL pattern of the empty string were affected.

* CVE-2018-1305

Security constraints defined by annotations of Servlets in Apache Tomcat 9.0.0.M1 to 9.0.4, 8.5.0 to 8.5.27, 8.0.0.RC1 to 8.0.49 and 7.0.0 to 7.0.84 were only applied once a Servlet had been loaded. Because security constraints defined in this way apply to the URL pattern and any URLs below that point, it was possible - depending on the order Servlets were loaded - for some security constraints not to be applied. This could have exposed resources to users who were not authorised to access them.

=== Changes in TomEE EAP 7.0.5-TT.1

This release has the following changes:

* MYFACES-4133 Don't deserialize the ViewState-ID if the state saving method is server
