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

To run the example simply type:

 $ mvn clean install

Amongst the output you should see OpenEJB startup with complete with the ServerServices you have in the classpath:

  bq. Apache OpenEJB 3.0-incubating-SNAPSHOT    build: 20061228-01:59
  bq. http://incubator.apache.org/openejb
  bq. 22:32:25,240 INFO  [startup] Found EjbModule in classpath: /Users/dblevins/work/openejb3/examples/telephone-stateful/target/classes
  bq. 22:32:26,601 WARN  [startup] No ejb-jar.xml found assuming annotated beans present: module: /Users/dblevins/work/openejb3/examples/telephone-stateful/target/classes
  bq. 22:32:26,635 WARN  [OpenEJB] Auto-deploying ejb TelephoneBean: EjbDeployment(deployment-id=TelephoneBean, container-id=Default Stateful Container)
  bq. 22:32:26,636 WARN  [OpenEJB] Auto-creating a container for bean TelephoneBean: Container(type=STATEFUL, id=Default Stateful Container)
  bq. 22:32:26,658 INFO  [startup] Loaded Module: /Users/dblevins/work/openejb3/examples/telephone-stateful/target/classes
  bq. 22:32:27,272 INFO  [startup] OpenEJB ready.
  bq. OpenEJB ready.
  bq.   ** Starting Services **
  bq.   NAME                 IP              PORT  
  bq.   ejbd                 0.0.0.0         4201  
  bq.   admin thread         0.0.0.0         4200  
  bq. -------
  bq. Ready!

If you want to add more ServerServices such as the http version of the ejbd protocol you'd simply add the openejb-httpejbd jar to your classpath.  A number of ServerServices are available currently:

  * openejb-ejbd
  * openejb-http
  * openejb-telnet
  * openejb-derbynet
  * openejb-hsql
  * openejb-activemq



