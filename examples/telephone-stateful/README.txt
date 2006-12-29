This example shows how to use OpenEJB3's remoting capabilities in an embedded scenario.

The basic recipe is the same for a standard embedded scenario but with these added ingreditents:

  * *openejb.embedded.remotable* property
  * *openejb-ejbd* jar

While creating the InitialContext, pass in the openejb.embedded.remotable property with the value of "true".
When this is seen by the LocalInitialContextFactory, it will boot up the Server ServiceManager in the VM which
will in turn look for ServerServices in the classpath.

Provided you have the openejb-ejbd jar in your classpath along with it's dependencies (openejb-server,
openejb-client, openejb-core), then those services will be brought online and remote clients will be
able to connect into your vm and invoke beans.




