<!--

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
============
OVERVIEW
============
This example creates an applet which requires the user to enter two numbers.
It then uses the Calculator EJB to add the numbers and displays the result.

Before you run the example, there are a few things worth noting. 
One has to keep in mind that applets run in a very restrictive environment. A couple of 
them are
1. Applets can only establish a connection to the host they came from
2. A signed applet gets around most of the restrictions (e.g. calls to System.getProperty() etc)
3. Any resources/classes needed by the applet should be available in the same directory where
the applet originated from, or its sub-directory/ies

Since our applet will establish a connection back to the EJB, so we need to make sure 
we are not trying to establish a connection to some other webapp. We will see in a minute 
what that means.
We shall also sign our applet using jarsigner, however would would first need to create
a certificate. We will get to this part also in a minute.

Since our applet would be running in a remote JVM, it would use the RemoteInitialContextFactory
instead of the LocalInitialContextFactory. The RemoteInitialContextFactory is available
in openejb-client.jar. The applet would also need the java-ee-api.jar in its class-path.

Lets see how we can use maven + ant to get around the above restrictions of applets:-

The pom.xml of this project 
- adds the client and the ee jars as dependencies. 
- uses the ant jar task (in the maven-antrun-plugin) to jar up all classes in WEB-INF/classes and puts them under the root
directory of the web app (this is required, since web app clients cannot access anything under
WEB-INF directory, hence we copied those classes to web-apps root directory)
[Note:- The ant script puts all classes under WEB-INF/classes inside a jar. All classes
are not required by the applet. Instead, the applet only needs the EJB interface and Applet 
classes in the jar]
- uses maven-dependency-plugin to copy javaee-api-embedded and openejb-client jars to src/main/webapp (this is the webapp root directory)
- uses the signjar ant task to sign all the jar files

The APPLET is located inside index.jsp . Notice how the archive attribute of the applet
is used to add the applet , java ee and openejb client jars to the applets classpath

Since the APPLET can only communicate from the host it originates, notice how the web context
root is used in the provider url to obtain the InitialContext. 
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.openejb.client.RemoteInitialContextFactory");
		props.put(Context.PROVIDER_URL,"http://127.0.0.1:8080/applet/ejb");

The example web-app has a context root named applet. If you were making another webapp with a 
different context root, then you would use that in the provider url instead.
If you open the web.xml of this project, you will find a <servlet-mapping> section which 
actually maps the /ejb/* to a servlet named ServerServlet. This is another thing which you need
too keep in mind in your web-app. Yes, the provider url actually points to a servlet named
ServerServlet. This servlet is provided by openejb and is automatically added to you webapps
classpath.
[Note:- All other clients (except applets) will use a provider url of http://127.0.0.1:8080/tomee/ejb,
since an applet cannot connect to another web-app, hence the above little trick to work around this
limitation. If you do not make the change, you will get a HTTP 403 error i.e. server denied 
access to the ServerServlet . A unit test named JNDILookupTest has been added to the example to 
show how a non-applet remote client would obtain the Context]

HOW TO RUN THE EXAMPLE
======================
--USE KEYTOOL --
You would need to do a bit of work (if you have not done that already) in creating a self-signed
certificate (in case you do not have one from one of the certificate authorities).
Here is the steps performed to create the certificate (notice the store password is openejb - 
this password is also used in pom.xml while signing the jar -- if your store passwd is different
please update the pom.xml with the correct password before proceeding forward)
Use the keytool as shown below (answer all questions asked by keytool -- the answers do not
have to match as shown below (except for store password and alias <mykey> -- since it is used by maven))

karan@jee:~$ keytool -genkey -alias mykey
Enter keystore password:  openejb
What is your first and last name?
  [Unknown]:  karan malhi
What is the name of your organizational unit?
  [Unknown]:  Superbiz   
What is the name of your organization?
  [Unknown]:  Superbiz
What is the name of your City or Locality?
  [Unknown]:  Deptford
What is the name of your State or Province?
  [Unknown]:  NJ
What is the two-letter country code for this unit?
  [Unknown]:  US
Is CN=karan malhi, OU=Superbiz, O=Superbiz, L=Deptford, ST=NJ, C=US correct?
  [no]:  yes

Enter key password for <mykey>
        (RETURN if same as keystore password):  
karan@jee:~$ 

-- INSTALL AND CONFIGURE TOMCAT --

1. Install latest Tomcat
2. Deploy OpenEJB WAR in tomcat
3. Open <<tomcat-install>>/conf/tomcat-users.xml and add the following user
   <user username="admin" password="" roles="manager"/>
   
-- DEPLOY AND TEST THE EXAMPLE --
1. Run the following command while in the applet directory
   mvn clean install war:exploded tomcat:deploy -Dmaven.test.skip=true
   [It is required to skip the test since the application is not yet deployed on tomcat. If you run
the test now, it will fail]
2. The above will deploy this web application to tomcat.
3. To test the application, open a web browser and navigate to 
   http://localhost:8080/applet
4. You will be prompted to accept digital signature. Accept it to run the applet
5. Enter two numbers and click the add button, the result should be displayed next
to the button

[Note:- If you make changes and want to redeploy the app use the following command:
 mvn clean install war:exploded tomcat:redeploy]



