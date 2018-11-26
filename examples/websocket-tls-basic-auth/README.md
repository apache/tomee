# websocket-tls-basic-auth

Websocket example project using SSL and basic authentication.

This was originally created for TomEE 8.0.0. using JEE 8 and Websocket API 1.1.

There is an arquillian test that will perform basic authentication using the username and password configured in 
the _.../conf/tomcat-users.xml_.

The _.../conf/server.xml_ file used in the Arquillian test can serve as a starting point to a real server configuration.

The _.../conf/keystore.jks_ is a self signed certificate created for demonstration purposes.

For additional information on websockets and keystores with TomEE, please look at this blog post: 
https://www.tomitribe.com/blog/tomee-ssl-tls-secured-websockets/
