[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Webservice Attachments 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ webservice-attachments ---
[INFO] Deleting /Users/dblevins/examples/webservice-attachments/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ webservice-attachments ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ webservice-attachments ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/webservice-attachments/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ webservice-attachments ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webservice-attachments/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ webservice-attachments ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/webservice-attachments/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ webservice-attachments ---
[INFO] Surefire report directory: /Users/dblevins/examples/webservice-attachments/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.attachment.AttachmentTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/webservice-attachments
INFO - openejb.base = /Users/dblevins/examples/webservice-attachments
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/webservice-attachments/target/classes
INFO - Beginning load: /Users/dblevins/examples/webservice-attachments/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/webservice-attachments/classpath.ear
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean AttachmentImpl: Container(type=STATELESS, id=Default Stateless Container)
INFO - Enterprise application "/Users/dblevins/examples/webservice-attachments/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/webservice-attachments/classpath.ear
INFO - Created Ejb(deployment-id=AttachmentImpl, ejb-name=AttachmentImpl, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=AttachmentImpl, ejb-name=AttachmentImpl, container=Default Stateless Container)
INFO - Deployed Application(path=/Users/dblevins/examples/webservice-attachments/classpath.ear)
INFO - Initializing network services
INFO - Creating ServerService(id=httpejbd)
INFO - Creating ServerService(id=cxf)
INFO - Creating ServerService(id=admin)
INFO - Creating ServerService(id=ejbd)
INFO - Creating ServerService(id=ejbds)
INFO - Initializing network services
  ** Starting Services **
  NAME                 IP              PORT  
  httpejbd             127.0.0.1       4204  
  admin thread         127.0.0.1       4200  
  ejbd                 127.0.0.1       4201  
  ejbd                 127.0.0.1       4203  
-------
Ready!
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.934 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ webservice-attachments ---
[INFO] Building jar: /Users/dblevins/examples/webservice-attachments/target/webservice-attachments-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ webservice-attachments ---
[INFO] Installing /Users/dblevins/examples/webservice-attachments/target/webservice-attachments-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/webservice-attachments/1.0/webservice-attachments-1.0.jar
[INFO] Installing /Users/dblevins/examples/webservice-attachments/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/webservice-attachments/1.0/webservice-attachments-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.450s
[INFO] Finished at: Fri Oct 28 17:11:04 PDT 2011
[INFO] Final Memory: 17M/81M
[INFO] ------------------------------------------------------------------------
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.attachment;
    
    import javax.activation.DataHandler;
    import javax.activation.DataSource;
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    import javax.xml.ws.BindingType;
    import javax.xml.ws.soap.SOAPBinding;
    import java.io.IOException;
    import java.io.InputStream;
    
    /**
     * This is an EJB 3 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has a single interface: {@link AttachmentWs} a webservice interface.
     */
    @Stateless
    @WebService(
            portName = "AttachmentPort",
            serviceName = "AttachmentWsService",
            targetNamespace = "http://superbiz.org/wsdl",
            endpointInterface = "org.superbiz.attachment.AttachmentWs")
    @BindingType(value = SOAPBinding.SOAP12HTTP_MTOM_BINDING)
    public class AttachmentImpl implements AttachmentWs {
    
        public String stringFromBytes(byte[] data) {
            return new String(data);
    
        }
    
        public String stringFromDataSource(DataSource source) {
    
            try {
                InputStream inStr = source.getInputStream();
                int size = inStr.available();
                byte[] data = new byte[size];
                inStr.read(data);
                inStr.close();
                return new String(data);
    
            } catch (IOException e) {
                e.printStackTrace();
    
            }
            return "";
    
        }
    
        public String stringFromDataHandler(DataHandler handler) {
    
            try {
                return (String) handler.getContent();
    
            } catch (IOException e) {
                e.printStackTrace();
    
            }
            return "";
    
        }
    
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.attachment;
    
    import javax.activation.DataHandler;
    import javax.jws.WebService;
    
    /**
     * This is an EJB 3 webservice interface to send attachments throughout SAOP.
     */
    @WebService(targetNamespace = "http://superbiz.org/wsdl")
    public interface AttachmentWs {
    
        public String stringFromBytes(byte[] data);
    
        // Not working at the moment with SUN saaj provider and CXF
        //public String stringFromDataSource(DataSource source);
    
        public String stringFromDataHandler(DataHandler handler);
    
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.attachment;
    
    import junit.framework.TestCase;
    
    import javax.activation.DataHandler;
    import javax.activation.DataSource;
    import javax.mail.util.ByteArrayDataSource;
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.xml.namespace.QName;
    import javax.xml.ws.BindingProvider;
    import javax.xml.ws.Service;
    import javax.xml.ws.soap.SOAPBinding;
    import java.net.URL;
    import java.util.Properties;
    
    public class AttachmentTest extends TestCase {
    
        //START SNIPPET: setup	
        private InitialContext initialContext;
    
        protected void setUp() throws Exception {
    
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.embedded.remotable", "true");
    
            initialContext = new InitialContext(properties);
        }
        //END SNIPPET: setup    
    
        /**
         * Create a webservice client using wsdl url
         *
         * @throws Exception
         */
        //START SNIPPET: webservice
        public void testAttachmentViaWsInterface() throws Exception {
            Service service = Service.create(
                    new URL("http://127.0.0.1:4204/AttachmentImpl?wsdl"),
                    new QName("http://superbiz.org/wsdl", "AttachmentWsService"));
            assertNotNull(service);
    
            AttachmentWs ws = service.getPort(AttachmentWs.class);
    
            // retrieve the SOAPBinding
            SOAPBinding binding = (SOAPBinding) ((BindingProvider) ws).getBinding();
            binding.setMTOMEnabled(true);
    
            String request = "tsztelak@gmail.com";
    
            // Byte array
            String response = ws.stringFromBytes(request.getBytes());
            assertEquals(request, response);
    
            // Data Source
            DataSource source = new ByteArrayDataSource(request.getBytes(), "text/plain; charset=UTF-8");
    
            // not yet supported !
    //        response = ws.stringFromDataSource(source);
    //        assertEquals(request, response);
    
            // Data Handler
            response = ws.stringFromDataHandler(new DataHandler(source));
            assertEquals(request, response);
    
        }
        //END SNIPPET: webservice
    
    }
