[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: EJB Examples War 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ ejb-examples ---
[INFO] Deleting /Users/dblevins/examples/webapps/ejb-examples/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ ejb-examples ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 4 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ ejb-examples ---
[INFO] Compiling 20 source files to /Users/dblevins/examples/webapps/ejb-examples/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ ejb-examples ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/ejb-examples/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ ejb-examples ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ ejb-examples ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/webapps/ejb-examples/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-war-plugin:2.1.1:war (default-war) @ ejb-examples ---
[INFO] Packaging webapp
[INFO] Assembling webapp [ejb-examples] in [/Users/dblevins/examples/webapps/ejb-examples/target/ejb-examples-1.0]
[INFO] Processing war project
[INFO] Copying webapp resources [/Users/dblevins/examples/webapps/ejb-examples/src/main/webapp]
[INFO] Webapp assembled in [40 msecs]
[INFO] Building war: /Users/dblevins/examples/webapps/ejb-examples/target/ejb-examples-1.0.war
[INFO] WEB-INF/web.xml already added, skipping
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ ejb-examples ---
[INFO] Installing /Users/dblevins/examples/webapps/ejb-examples/target/ejb-examples-1.0.war to /Users/dblevins/.m2/repository/org/superbiz/ejb-examples/1.0/ejb-examples-1.0.war
[INFO] Installing /Users/dblevins/examples/webapps/ejb-examples/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/ejb-examples/1.0/ejb-examples-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.324s
[INFO] Finished at: Fri Oct 28 17:04:49 PDT 2011
[INFO] Final Memory: 10M/81M
[INFO] ------------------------------------------------------------------------
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.annotation.Resource;
    import javax.ejb.LocalBean;
    import javax.ejb.Stateless;
    import javax.sql.DataSource;
    
    @Stateless
    @LocalBean
    public class AnnotatedEJB implements AnnotatedEJBLocal, AnnotatedEJBRemote {
        @Resource
        private DataSource ds;
    
        private String name = "foo";
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public DataSource getDs() {
            return ds;
        }
    
        public void setDs(DataSource ds) {
            this.ds = ds;
        }
    
        public String toString() {
            return "AnnotatedEJB[name=" + name + "]";
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.ejb.Local;
    import javax.sql.DataSource;
    
    @Local
    public interface AnnotatedEJBLocal {
        String getName();
    
        void setName(String name);
    
        DataSource getDs();
    
        void setDs(DataSource ds);
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.ejb.Remote;
    
    @Remote
    public interface AnnotatedEJBRemote {
        String getName();
    
        void setName(String name);
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.annotation.Resource;
    import javax.ejb.EJB;
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    import javax.servlet.ServletException;
    import javax.servlet.ServletOutputStream;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import javax.sql.DataSource;
    import java.io.IOException;
    
    public class AnnotatedServlet extends HttpServlet {
        @EJB
        private AnnotatedEJBLocal localEJB;
    
        @EJB
        private AnnotatedEJBRemote remoteEJB;
    
        @EJB
        private AnnotatedEJB localbeanEJB;
    
        @Resource
        private DataSource ds;
    
    
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
    
            out.println("LocalBean EJB");
            out.println("@EJB=" + localbeanEJB);
            if (localbeanEJB != null) {
                out.println("@EJB.getName()=" + localbeanEJB.getName());
                out.println("@EJB.getDs()=" + localbeanEJB.getDs());
            }
            out.println("JNDI=" + lookupField("localbeanEJB"));
            out.println();
    
            out.println("Local EJB");
            out.println("@EJB=" + localEJB);
            if (localEJB != null) {
                out.println("@EJB.getName()=" + localEJB.getName());
                out.println("@EJB.getDs()=" + localEJB.getDs());
            }
            out.println("JNDI=" + lookupField("localEJB"));
            out.println();
    
            out.println("Remote EJB");
            out.println("@EJB=" + remoteEJB);
            if (localEJB != null) {
                out.println("@EJB.getName()=" + remoteEJB.getName());
            }
            out.println("JNDI=" + lookupField("remoteEJB"));
            out.println();
    
    
            out.println("DataSource");
            out.println("@Resource=" + ds);
            out.println("JNDI=" + lookupField("ds"));
        }
    
        private Object lookupField(String name) {
            try {
                return new InitialContext().lookup("java:comp/env/" + getClass().getName() + "/" + name);
            } catch (NamingException e) {
                return null;
            }
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.xml.ws.handler.Handler;
    import javax.xml.ws.handler.MessageContext;
    
    public class ClientHandler implements Handler {
        public boolean handleMessage(MessageContext messageContext) {
            WebserviceServlet.write("    ClientHandler handleMessage");
            return true;
        }
    
        public void close(MessageContext messageContext) {
            WebserviceServlet.write("    ClientHandler close");
        }
    
        public boolean handleFault(MessageContext messageContext) {
            WebserviceServlet.write("    ClientHandler handleFault");
            return true;
        }
    }/**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.jws.WebService;
    
    @WebService(targetNamespace = "http://examples.org/wsdl")
    public interface HelloEjb {
        String hello(String name);
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.ejb.Stateless;
    import javax.jws.HandlerChain;
    import javax.jws.WebService;
    
    @WebService(
            portName = "HelloEjbPort",
            serviceName = "HelloEjbService",
            targetNamespace = "http://examples.org/wsdl",
            endpointInterface = "org.superbiz.servlet.HelloEjb"
    )
    @HandlerChain(file = "server-handlers.xml")
    @Stateless
    public class HelloEjbService implements HelloEjb {
        public String hello(String name) {
            WebserviceServlet.write("                HelloEjbService hello(" + name + ")");
            if (name == null) name = "World";
            return "Hello " + name + " from EJB Webservice!";
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.jws.WebService;
    
    @WebService(targetNamespace = "http://examples.org/wsdl")
    public interface HelloPojo {
        String hello(String name);
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.jws.HandlerChain;
    import javax.jws.WebService;
    
    @WebService(
            portName = "HelloPojoPort",
            serviceName = "HelloPojoService",
            targetNamespace = "http://examples.org/wsdl",
            endpointInterface = "org.superbiz.servlet.HelloPojo"
    )
    @HandlerChain(file = "server-handlers.xml")
    public class HelloPojoService implements HelloPojo {
        public String hello(String name) {
            WebserviceServlet.write("                HelloPojoService hello(" + name + ")");
            if (name == null) name = "World";
            return "Hello " + name + " from Pojo Webservice!";
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.naming.NameClassPair;
    import javax.naming.NamingException;
    import javax.servlet.ServletException;
    import javax.servlet.ServletOutputStream;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    import java.util.Collections;
    import java.util.Map;
    import java.util.TreeMap;
    
    public class JndiServlet extends HttpServlet {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
    
            Map<String, Object> bindings = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
            try {
                Context context = (Context) new InitialContext().lookup("java:comp/");
                addBindings("", bindings, context);
            } catch (NamingException e) {
                throw new ServletException(e);
            }
    
            out.println("JNDI Context:");
            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                if (entry.getValue() != null) {
                    out.println("  " + entry.getKey() + "=" + entry.getValue());
                } else {
                    out.println("  " + entry.getKey());
                }
            }
        }
    
        private void addBindings(String path, Map<String, Object> bindings, Context context) {
            try {
                for (NameClassPair pair : Collections.list(context.list(""))) {
                    String name = pair.getName();
                    String className = pair.getClassName();
                    if ("org.apache.naming.resources.FileDirContext$FileResource".equals(className)) {
                        bindings.put(path + name, "<file>");
                    } else {
                        try {
                            Object value = context.lookup(name);
                            if (value instanceof Context) {
                                Context nextedContext = (Context) value;
                                bindings.put(path + name, "");
                                addBindings(path + name + "/", bindings, nextedContext);
                            } else {
                                bindings.put(path + name, value);
                            }
                        } catch (NamingException e) {
                            // lookup failed
                            bindings.put(path + name, "ERROR: " + e.getMessage());
                        }
                    }
                }
            } catch (NamingException e) {
                bindings.put(path, "ERROR: list bindings threw an exception: " + e.getMessage());
            }
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.persistence.Column;
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;
    
    @Entity
    public class JpaBean {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private int id;
    
        @Column(name = "name")
        private String name;
    
        public int getId() {
            return id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
    
        public String toString() {
            return "[JpaBean id=" + id + ", name=" + name + "]";
        }
    }/**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.persistence.EntityManager;
    import javax.persistence.EntityManagerFactory;
    import javax.persistence.EntityTransaction;
    import javax.persistence.PersistenceUnit;
    import javax.persistence.Query;
    import javax.servlet.ServletException;
    import javax.servlet.ServletOutputStream;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    
    public class JpaServlet extends HttpServlet {
        @PersistenceUnit(name = "jpa-example")
        private EntityManagerFactory emf;
    
    
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
    
            out.println("@PersistenceUnit=" + emf);
    
            EntityManager em = emf.createEntityManager();
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
    
            JpaBean jpaBean = new JpaBean();
            jpaBean.setName("JpaBean");
            em.persist(jpaBean);
    
            transaction.commit();
            transaction.begin();
    
            Query query = em.createQuery("SELECT j FROM JpaBean j WHERE j.name='JpaBean'");
            jpaBean = (JpaBean) query.getSingleResult();
            out.println("Loaded " + jpaBean);
    
            em.remove(jpaBean);
    
            transaction.commit();
            transaction.begin();
    
            query = em.createQuery("SELECT count(j) FROM JpaBean j WHERE j.name='JpaBean'");
            int count = ((Number) query.getSingleResult()).intValue();
            if (count == 0) {
                out.println("Removed " + jpaBean);
            } else {
                out.println("ERROR: unable to remove" + jpaBean);
            }
    
            transaction.commit();
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    public class ResourceBean {
        private String value;
    
        public String getValue() {
            return value;
        }
    
        public void setValue(String value) {
            this.value = value;
        }
    
        public String toString() {
            return "[ResourceBean " + value + "]";
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.ejb.EJB;
    import javax.ejb.EJBAccessException;
    import javax.servlet.ServletException;
    import javax.servlet.ServletOutputStream;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    import java.security.Principal;
    
    public class RunAsServlet extends HttpServlet {
        @EJB
        private SecureEJBLocal secureEJBLocal;
    
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
    
            out.println("Servlet");
            Principal principal = request.getUserPrincipal();
            if (principal != null) {
                out.println("Servlet.getUserPrincipal()=" + principal + " [" + principal.getName() + "]");
            } else {
                out.println("Servlet.getUserPrincipal()=<null>");
            }
            out.println("Servlet.isCallerInRole(\"user\")=" + request.isUserInRole("user"));
            out.println("Servlet.isCallerInRole(\"manager\")=" + request.isUserInRole("manager"));
            out.println("Servlet.isCallerInRole(\"fake\")=" + request.isUserInRole("fake"));
            out.println();
    
            out.println("@EJB=" + secureEJBLocal);
            if (secureEJBLocal != null) {
                principal = secureEJBLocal.getCallerPrincipal();
                if (principal != null) {
                    out.println("@EJB.getCallerPrincipal()=" + principal + " [" + principal.getName() + "]");
                } else {
                    out.println("@EJB.getCallerPrincipal()=<null>");
                }
                out.println("@EJB.isCallerInRole(\"user\")=" + secureEJBLocal.isCallerInRole("user"));
                out.println("@EJB.isCallerInRole(\"manager\")=" + secureEJBLocal.isCallerInRole("manager"));
                out.println("@EJB.isCallerInRole(\"fake\")=" + secureEJBLocal.isCallerInRole("fake"));
    
                try {
                    secureEJBLocal.allowUserMethod();
                    out.println("@EJB.allowUserMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.allowUserMethod() DENIED");
                }
    
                try {
                    secureEJBLocal.allowManagerMethod();
                    out.println("@EJB.allowManagerMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.allowManagerMethod() DENIED");
                }
    
                try {
                    secureEJBLocal.allowFakeMethod();
                    out.println("@EJB.allowFakeMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.allowFakeMethod() DENIED");
                }
    
                try {
                    secureEJBLocal.denyAllMethod();
                    out.println("@EJB.denyAllMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.denyAllMethod() DENIED");
                }
            }
            out.println();
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.annotation.Resource;
    import javax.annotation.security.DeclareRoles;
    import javax.annotation.security.DenyAll;
    import javax.annotation.security.RolesAllowed;
    import javax.ejb.SessionContext;
    import javax.ejb.Stateless;
    import java.security.Principal;
    
    @Stateless
    @DeclareRoles({"user", "manager", "fake"})
    public class SecureEJB implements SecureEJBLocal {
        @Resource
        private SessionContext context;
    
        public Principal getCallerPrincipal() {
            return context.getCallerPrincipal();
        }
    
        public boolean isCallerInRole(String role) {
            return context.isCallerInRole(role);
        }
    
        @RolesAllowed("user")
        public void allowUserMethod() {
        }
    
        @RolesAllowed("manager")
        public void allowManagerMethod() {
        }
    
        @RolesAllowed("fake")
        public void allowFakeMethod() {
        }
    
        @DenyAll
        public void denyAllMethod() {
        }
    
        public String toString() {
            return "SecureEJB[userName=" + getCallerPrincipal() + "]";
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.ejb.Local;
    import java.security.Principal;
    
    @Local
    public interface SecureEJBLocal {
        Principal getCallerPrincipal();
    
        boolean isCallerInRole(String role);
    
        void allowUserMethod();
    
        void allowManagerMethod();
    
        void allowFakeMethod();
    
        void denyAllMethod();
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.ejb.EJB;
    import javax.ejb.EJBAccessException;
    import javax.servlet.ServletException;
    import javax.servlet.ServletOutputStream;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    import java.security.Principal;
    
    public class SecureServlet extends HttpServlet {
        @EJB
        private SecureEJBLocal secureEJBLocal;
    
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
    
            out.println("Servlet");
            Principal principal = request.getUserPrincipal();
            if (principal != null) {
                out.println("Servlet.getUserPrincipal()=" + principal + " [" + principal.getName() + "]");
            } else {
                out.println("Servlet.getUserPrincipal()=<null>");
            }
            out.println("Servlet.isCallerInRole(\"user\")=" + request.isUserInRole("user"));
            out.println("Servlet.isCallerInRole(\"manager\")=" + request.isUserInRole("manager"));
            out.println("Servlet.isCallerInRole(\"fake\")=" + request.isUserInRole("fake"));
            out.println();
    
            out.println("@EJB=" + secureEJBLocal);
            if (secureEJBLocal != null) {
                principal = secureEJBLocal.getCallerPrincipal();
                if (principal != null) {
                    out.println("@EJB.getCallerPrincipal()=" + principal + " [" + principal.getName() + "]");
                } else {
                    out.println("@EJB.getCallerPrincipal()=<null>");
                }
                out.println("@EJB.isCallerInRole(\"user\")=" + secureEJBLocal.isCallerInRole("user"));
                out.println("@EJB.isCallerInRole(\"manager\")=" + secureEJBLocal.isCallerInRole("manager"));
                out.println("@EJB.isCallerInRole(\"fake\")=" + secureEJBLocal.isCallerInRole("fake"));
    
                try {
                    secureEJBLocal.allowUserMethod();
                    out.println("@EJB.allowUserMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.allowUserMethod() DENIED");
                }
    
                try {
                    secureEJBLocal.allowManagerMethod();
                    out.println("@EJB.allowManagerMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.allowManagerMethod() DENIED");
                }
    
                try {
                    secureEJBLocal.allowFakeMethod();
                    out.println("@EJB.allowFakeMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.allowFakeMethod() DENIED");
                }
    
                try {
                    secureEJBLocal.denyAllMethod();
                    out.println("@EJB.denyAllMethod() ALLOWED");
                } catch (EJBAccessException e) {
                    out.println("@EJB.denyAllMethod() DENIED");
                }
            }
            out.println();
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.xml.ws.handler.Handler;
    import javax.xml.ws.handler.MessageContext;
    
    public class ServerHandler implements Handler {
        public boolean handleMessage(MessageContext messageContext) {
            WebserviceServlet.write("        ServerHandler handleMessage");
            return true;
        }
    
        public void close(MessageContext messageContext) {
            WebserviceServlet.write("        ServerHandler close");
        }
    
        public boolean handleFault(MessageContext messageContext) {
            WebserviceServlet.write("        ServerHandler handleFault");
            return true;
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.xml.ws.Service;
    import java.io.PrintStream;
    import java.net.URL;
    
    public class WebserviceClient {
        /**
         * Unfortunately, to run this example with CXF you need to have a HUGE class path.  This
         * is just what is required to run CXF:
         * <p/>
         * jaxb-api-2.0.jar
         * jaxb-impl-2.0.3.jar
         * <p/>
         * saaj-api-1.3.jar
         * saaj-impl-1.3.jar
         * <p/>
         * <p/>
         * cxf-api-2.0.2-incubator.jar
         * cxf-common-utilities-2.0.2-incubator.jar
         * cxf-rt-bindings-soap-2.0.2-incubator.jar
         * cxf-rt-core-2.0.2-incubator.jar
         * cxf-rt-databinding-jaxb-2.0.2-incubator.jar
         * cxf-rt-frontend-jaxws-2.0.2-incubator.jar
         * cxf-rt-frontend-simple-2.0.2-incubator.jar
         * cxf-rt-transports-http-jetty-2.0.2-incubator.jar
         * cxf-rt-transports-http-2.0.2-incubator.jar
         * cxf-tools-common-2.0.2-incubator.jar
         * <p/>
         * geronimo-activation_1.1_spec-1.0.jar
         * geronimo-annotation_1.0_spec-1.1.jar
         * geronimo-ejb_3.0_spec-1.0.jar
         * geronimo-jpa_3.0_spec-1.1.jar
         * geronimo-servlet_2.5_spec-1.1.jar
         * geronimo-stax-api_1.0_spec-1.0.jar
         * jaxws-api-2.0.jar
         * axis2-jws-api-1.3.jar
         * <p/>
         * wsdl4j-1.6.1.jar
         * xml-resolver-1.2.jar
         * XmlSchema-1.3.1.jar
         */
        public static void main(String[] args) throws Exception {
            PrintStream out = System.out;
    
            Service helloPojoService = Service.create(new URL("http://localhost:8080/ejb-examples/hello?wsdl"), null);
            HelloPojo helloPojo = helloPojoService.getPort(HelloPojo.class);
            out.println();
            out.println("Pojo Webservice");
            out.println("    helloPojo.hello(\"Bob\")=" + helloPojo.hello("Bob"));
            out.println("    helloPojo.hello(null)=" + helloPojo.hello(null));
            out.println();
    
            Service helloEjbService = Service.create(new URL("http://localhost:8080/HelloEjbService?wsdl"), null);
            HelloEjb helloEjb = helloEjbService.getPort(HelloEjb.class);
            out.println();
            out.println("EJB Webservice");
            out.println("    helloEjb.hello(\"Bob\")=" + helloEjb.hello("Bob"));
            out.println("    helloEjb.hello(null)=" + helloEjb.hello(null));
            out.println();
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.servlet;
    
    import javax.jws.HandlerChain;
    import javax.servlet.ServletException;
    import javax.servlet.ServletOutputStream;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import javax.xml.ws.WebServiceRef;
    import java.io.IOException;
    
    public class WebserviceServlet extends HttpServlet {
    
        @WebServiceRef
        @HandlerChain(file = "client-handlers.xml")
        private HelloPojo helloPojo;
    
        @WebServiceRef
        @HandlerChain(file = "client-handlers.xml")
        private HelloEjb helloEjb;
    
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
    
            OUT = out;
            try {
                out.println("Pojo Webservice");
                out.println("    helloPojo.hello(\"Bob\")=" + helloPojo.hello("Bob"));
                out.println();
                out.println("    helloPojo.hello(null)=" + helloPojo.hello(null));
                out.println();
                out.println("EJB Webservice");
                out.println("    helloEjb.hello(\"Bob\")=" + helloEjb.hello("Bob"));
                out.println();
                out.println("    helloEjb.hello(null)=" + helloEjb.hello(null));
                out.println();
            } finally {
                OUT = out;
            }
        }
    
        private static ServletOutputStream OUT;
    
        public static void write(String message) {
            try {
                ServletOutputStream out = OUT;
                out.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
