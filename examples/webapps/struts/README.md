[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: Struts 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ struts ---
[INFO] Deleting /Users/dblevins/examples/webapps/struts/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ struts ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ struts ---
[INFO] Compiling 8 source files to /Users/dblevins/examples/webapps/struts/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ struts ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/struts/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ struts ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ struts ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/webapps/struts/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-war-plugin:2.1.1:war (default-war) @ struts ---
[INFO] Packaging webapp
[INFO] Assembling webapp [struts] in [/Users/dblevins/examples/webapps/struts/target/struts]
[INFO] Processing war project
[INFO] Copying webapp resources [/Users/dblevins/examples/webapps/struts/src/main/webapp]
[INFO] Webapp assembled in [129 msecs]
[INFO] Building war: /Users/dblevins/examples/webapps/struts/target/struts.war
[INFO] WEB-INF/web.xml already added, skipping
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ struts ---
[INFO] Installing /Users/dblevins/examples/webapps/struts/target/struts.war to /Users/dblevins/.m2/repository/org/superbiz/struts/struts/1.0/struts-1.0.war
[INFO] Installing /Users/dblevins/examples/webapps/struts/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/struts/struts/1.0/struts-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.987s
[INFO] Finished at: Fri Oct 28 17:03:38 PDT 2011
[INFO] Final Memory: 11M/81M
[INFO] ------------------------------------------------------------------------
    /*
    
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
    */
    package org.superbiz.struts;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    
    public class AddUser {
    
        private int id;
        private String firstName;
        private String lastName;
        private String errorMessage;
    
    
        public String getFirstName() {
            return firstName;
        }
    
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    
        public String getLastName() {
            return lastName;
        }
    
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    
        public String getErrorMessage() {
            return errorMessage;
        }
    
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String execute() {
    
            try {
                UserService service = null;
                Properties props = new Properties();
                props.put(Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.openejb.client.LocalInitialContextFactory");
                Context ctx = new InitialContext(props);
                service = (UserService) ctx.lookup("UserServiceImplLocal");
                service.add(new User(id, firstName, lastName));
            } catch (Exception e) {
                this.errorMessage = e.getMessage();
                return "failure";
            }
    
            return "success";
        }
    }
    /*
    
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
    */
    package org.superbiz.struts;
    
    import com.opensymphony.xwork2.ActionSupport;
    
    
    public class AddUserForm extends ActionSupport {
    
    }
    /*
    
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
     */
    package org.superbiz.struts;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    public class FindUser {
    
        private int id;
        private String errorMessage;
        private User user;
    
        public User getUser() {
            return user;
        }
    
        public void setUser(User user) {
            this.user = user;
        }
    
        public String getErrorMessage() {
            return errorMessage;
        }
    
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String execute() {
    
            try {
                UserService service = null;
                Properties props = new Properties();
                props.put(Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.openejb.client.LocalInitialContextFactory");
                Context ctx = new InitialContext(props);
                service = (UserService) ctx.lookup("UserServiceImplLocal");
                this.user = service.find(id);
            } catch (Exception e) {
                this.errorMessage = e.getMessage();
                return "failure";
            }
    
            return "success";
        }
    }
    /*
    
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
    */
    package org.superbiz.struts;
    
    import com.opensymphony.xwork2.ActionSupport;
    
    
    public class FindUserForm extends ActionSupport {
    
    }
    /*
    
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
     */
    package org.superbiz.struts;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.List;
    import java.util.Properties;
    
    public class ListAllUsers {
    
        private int id;
        private String errorMessage;
        private List<User> users;
    
        public List<User> getUsers() {
            return users;
        }
    
        public void setUsers(List<User> users) {
            this.users = users;
        }
    
        public String getErrorMessage() {
            return errorMessage;
        }
    
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String execute() {
    
            try {
                UserService service = null;
                Properties props = new Properties();
                props.put(Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.openejb.client.LocalInitialContextFactory");
                Context ctx = new InitialContext(props);
                service = (UserService) ctx.lookup("UserServiceImplLocal");
                this.users = service.findAll();
            } catch (Exception e) {
                this.errorMessage = e.getMessage();
                return "failure";
            }
    
            return "success";
        }
    }
    /*
    
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
    */
    package org.superbiz.struts;
    
    import javax.persistence.Entity;
    import javax.persistence.Id;
    import javax.persistence.Table;
    import java.io.Serializable;
    
    @Entity
    @Table(name = "USER")
    public class User implements Serializable {
        private long id;
        private String firstName;
        private String lastName;
    
        public User(long id, String firstName, String lastName) {
            super();
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    
        public User() {
        }
    
        @Id
        public long getId() {
            return id;
        }
    
        public void setId(long id) {
            this.id = id;
        }
    
        public String getFirstName() {
            return firstName;
        }
    
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    
        public String getLastName() {
            return lastName;
        }
    
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    
    }
    /*
    
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
    */
    package org.superbiz.struts;
    
    import java.util.List;
    
    public interface UserService {
        public void add(User user);
    
        public User find(int id);
    
        public List<User> findAll();
    }
    /*
    
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
    */
    package org.superbiz.struts;
    
    import javax.ejb.Stateless;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import java.util.List;
    
    @Stateless
    public class UserServiceImpl implements UserService {
    
        @PersistenceContext(unitName = "user")
        private EntityManager manager;
    
        public void add(User user) {
            manager.persist(user);
        }
    
        public User find(int id) {
            return manager.find(User.class, id);
        }
    
        public List<User> findAll() {
            return manager.createQuery("select u from User u").getResultList();
        }
    
    }
