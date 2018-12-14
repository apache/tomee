index-group=Unrevised
type=page
status=published
~~~~~~
# websocket-tls-basic-auth

Websocket example project using SSL and basic authentication.

This was originally created for TomEE 8.0.0. using JEE 8 and Websocket API 1.1.

The example was created with a server to server typo of connection in mind. 
For Browser to server connections, you will need to refer to your frontend framework of choice but many server 
side configurations in here can be reused.

There is an Arquillian test that will perform basic authentication using the username and password configured in 
the _.../conf/tomcat-users.xml_.

The _.../conf/server.xml_ file used in the Arquillian test can serve as a starting point to a real server configuration.

The _.../conf/keystore.jks_ is a self signed certificate created for demonstration purposes.

For additional information on websockets and keystores with TomEE, please look at this blog post: 
https://www.tomitribe.com/blog/tomee-ssl-tls-secured-websockets/
