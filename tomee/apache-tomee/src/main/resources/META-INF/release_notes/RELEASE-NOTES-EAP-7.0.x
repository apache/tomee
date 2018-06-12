= TomEE EAP 7.0.5-TT.3

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
