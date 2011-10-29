[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Interceptors 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ interceptors ---
[INFO] Deleting /Users/dblevins/examples/interceptors/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ interceptors ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ interceptors ---
[INFO] Compiling 20 source files to /Users/dblevins/examples/interceptors/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ interceptors ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/interceptors/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ interceptors ---
[INFO] Compiling 4 source files to /Users/dblevins/examples/interceptors/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ interceptors ---
[INFO] Surefire report directory: /Users/dblevins/examples/interceptors/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.interceptors.FullyInterceptedTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/interceptors
INFO - openejb.base = /Users/dblevins/examples/interceptors
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Using 'openejb.deployments.classpath.include=.*interceptors/target/classes.*'
INFO - Found EjbModule in classpath: /Users/dblevins/examples/interceptors/target/classes
INFO - Beginning load: /Users/dblevins/examples/interceptors/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/interceptors/classpath.ear
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean FullyInterceptedBean: Container(type=STATELESS, id=Default Stateless Container)
INFO - Enterprise application "/Users/dblevins/examples/interceptors/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/interceptors/classpath.ear
INFO - Jndi(name=FullyInterceptedBeanLocal) --> Ejb(deployment-id=FullyInterceptedBean)
INFO - Jndi(name=global/classpath.ear/interceptors/FullyInterceptedBean!org.superbiz.interceptors.FullyIntercepted) --> Ejb(deployment-id=FullyInterceptedBean)
INFO - Jndi(name=global/classpath.ear/interceptors/FullyInterceptedBean) --> Ejb(deployment-id=FullyInterceptedBean)
INFO - Jndi(name=ThirdSLSBeanLocal) --> Ejb(deployment-id=ThirdSLSBean)
INFO - Jndi(name=global/classpath.ear/interceptors/ThirdSLSBean!org.superbiz.interceptors.ThirdSLSBeanLocal) --> Ejb(deployment-id=ThirdSLSBean)
INFO - Jndi(name=global/classpath.ear/interceptors/ThirdSLSBean) --> Ejb(deployment-id=ThirdSLSBean)
INFO - Jndi(name=SecondStatelessInterceptedBeanLocal) --> Ejb(deployment-id=SecondStatelessInterceptedBean)
INFO - Jndi(name=global/classpath.ear/interceptors/SecondStatelessInterceptedBean!org.superbiz.interceptors.SecondStatelessInterceptedLocal) --> Ejb(deployment-id=SecondStatelessInterceptedBean)
INFO - Jndi(name=global/classpath.ear/interceptors/SecondStatelessInterceptedBean) --> Ejb(deployment-id=SecondStatelessInterceptedBean)
INFO - Jndi(name=MethodLevelInterceptorOnlySLSBeanLocal) --> Ejb(deployment-id=MethodLevelInterceptorOnlySLSBean)
INFO - Jndi(name=global/classpath.ear/interceptors/MethodLevelInterceptorOnlySLSBean!org.superbiz.interceptors.MethodLevelInterceptorOnlyParent) --> Ejb(deployment-id=MethodLevelInterceptorOnlySLSBean)
INFO - Jndi(name=global/classpath.ear/interceptors/MethodLevelInterceptorOnlySLSBean) --> Ejb(deployment-id=MethodLevelInterceptorOnlySLSBean)
INFO - Created Ejb(deployment-id=ThirdSLSBean, ejb-name=ThirdSLSBean, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=SecondStatelessInterceptedBean, ejb-name=SecondStatelessInterceptedBean, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=FullyInterceptedBean, ejb-name=FullyInterceptedBean, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=MethodLevelInterceptorOnlySLSBean, ejb-name=MethodLevelInterceptorOnlySLSBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=ThirdSLSBean, ejb-name=ThirdSLSBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=SecondStatelessInterceptedBean, ejb-name=SecondStatelessInterceptedBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=FullyInterceptedBean, ejb-name=FullyInterceptedBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=MethodLevelInterceptorOnlySLSBean, ejb-name=MethodLevelInterceptorOnlySLSBean, container=Default Stateless Container)
INFO - Deployed Application(path=/Users/dblevins/examples/interceptors/classpath.ear)
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.603 sec
Running org.superbiz.interceptors.MethodLevelInterceptorOnlyTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec
Running org.superbiz.interceptors.SecondStatelessInterceptedTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec
Running org.superbiz.interceptors.ThirdSLSBeanTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec

Results :

Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ interceptors ---
[INFO] Building jar: /Users/dblevins/examples/interceptors/target/interceptors-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ interceptors ---
[INFO] Installing /Users/dblevins/examples/interceptors/target/interceptors-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/interceptors/1.0/interceptors-1.0.jar
[INFO] Installing /Users/dblevins/examples/interceptors/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/interceptors/1.0/interceptors-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.931s
[INFO] Finished at: Fri Oct 28 17:02:54 PDT 2011
[INFO] Final Memory: 14M/81M
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class ClassLevelInterceptorOne {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class ClassLevelInterceptorSuperClassOne {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class ClassLevelInterceptorSuperClassTwo extends SuperClassOfClassLevelInterceptor {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class ClassLevelInterceptorTwo {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.annotation.PostConstruct;
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class DefaultInterceptorOne {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
        }
    
        @PostConstruct
        protected void postConstructInterceptor(InvocationContext ic) throws Exception {
            Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class DefaultInterceptorTwo {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import java.util.List;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public interface FullyIntercepted {
    
        List<String> businessMethod();
    
        List<String> methodWithDefaultInterceptorsExcluded();
    
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.ejb.Local;
    import javax.ejb.Stateless;
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.Interceptors;
    import javax.interceptor.InvocationContext;
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    @Stateless
    @Local
    @Interceptors({ClassLevelInterceptorOne.class, ClassLevelInterceptorTwo.class})
    public class FullyInterceptedBean extends FullyInterceptedSuperClass implements FullyIntercepted {
    
        @Interceptors({MethodLevelInterceptorOne.class, MethodLevelInterceptorTwo.class})
        public List<String> businessMethod() {
            List<String> list = new ArrayList<String>();
            list.add("businessMethod");
            return list;
        }
    
        @Interceptors({MethodLevelInterceptorOne.class, MethodLevelInterceptorTwo.class})
        public List<String> methodWithDefaultInterceptorsExcluded() {
            List<String> list = new ArrayList<String>();
            list.add("methodWithDefaultInterceptorsExcluded");
            return list;
        }
    
        @AroundInvoke
        protected Object beanClassBusinessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, "beanClassBusinessMethodInterceptor");
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.Interceptors;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    @Interceptors({ClassLevelInterceptorSuperClassOne.class, ClassLevelInterceptorSuperClassTwo.class})
    public class FullyInterceptedSuperClass {
    
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class MethodLevelInterceptorOne {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import java.io.Serializable;
    import java.util.List;
    
    public interface MethodLevelInterceptorOnlyIntf<T extends Serializable> {
        public List<T> makePersistent(T entity);
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import java.util.List;
    
    public interface MethodLevelInterceptorOnlyParent extends MethodLevelInterceptorOnlyIntf<String> {
    
        public List<String> makePersistent(String entity);
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.ejb.Local;
    import javax.ejb.Stateless;
    import javax.interceptor.Interceptors;
    import java.util.ArrayList;
    import java.util.List;
    
    @Local(MethodLevelInterceptorOnlyParent.class)
    @Stateless
    public class MethodLevelInterceptorOnlySLSBean implements MethodLevelInterceptorOnlyParent {
    
        @Interceptors(MethodLevelInterceptorOne.class)
        public List<String> makePersistent(String entity) {
            List<String> list = new ArrayList<String>();
            list.add("makePersistent");
            return list;
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class MethodLevelInterceptorTwo {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.ejb.Stateless;
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.Interceptors;
    import javax.interceptor.InvocationContext;
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * @version $Rev: 808273 $ $Date: 2009-08-26 20:42:06 -0700 (Wed, 26 Aug 2009) $
     */
    @Stateless
    @Interceptors({ClassLevelInterceptorOne.class, ClassLevelInterceptorTwo.class})
    public class SecondStatelessInterceptedBean implements SecondStatelessInterceptedLocal {
    
        @Interceptors({MethodLevelInterceptorOne.class, MethodLevelInterceptorTwo.class})
        public List<String> methodWithDefaultInterceptorsExcluded() {
            List<String> list = new ArrayList<String>();
            list.add("methodWithDefaultInterceptorsExcluded");
            return list;
    
        }
    
        @AroundInvoke
        protected Object beanClassBusinessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import java.util.List;
    
    /**
     * @version $Rev: 808273 $ $Date: 2009-08-26 20:42:06 -0700 (Wed, 26 Aug 2009) $
     */
    public interface SecondStatelessInterceptedLocal {
        List<String> methodWithDefaultInterceptorsExcluded();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.annotation.PostConstruct;
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class SuperClassOfClassLevelInterceptor {
    
        @AroundInvoke
        protected Object businessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
        }
    
        @PostConstruct
        protected void postConstructInterceptor(InvocationContext ic) throws Exception {
            Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.ejb.Stateless;
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.ExcludeClassInterceptors;
    import javax.interceptor.ExcludeDefaultInterceptors;
    import javax.interceptor.Interceptors;
    import javax.interceptor.InvocationContext;
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * @version $Rev: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    @Stateless
    @Interceptors({ClassLevelInterceptorOne.class, ClassLevelInterceptorTwo.class})
    @ExcludeDefaultInterceptors
    public class ThirdSLSBean implements ThirdSLSBeanLocal {
    
        @Interceptors({MethodLevelInterceptorOne.class, MethodLevelInterceptorTwo.class})
        public List<String> businessMethod() {
            List<String> list = new ArrayList<String>();
            list.add("businessMethod");
            return list;
        }
    
        @Interceptors({MethodLevelInterceptorOne.class, MethodLevelInterceptorTwo.class})
        @ExcludeClassInterceptors
        public List<String> anotherBusinessMethod() {
            List<String> list = new ArrayList<String>();
            list.add("anotherBusinessMethod");
            return list;
        }
    
    
        @AroundInvoke
        protected Object beanClassBusinessMethodInterceptor(InvocationContext ic) throws Exception {
            return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import java.util.List;
    
    /**
     * @version $Rev: 607320 $ $Date: 2007-12-28 12:15:06 -0800 (Fri, 28 Dec 2007) $
     */
    public interface ThirdSLSBeanLocal {
        List<String> businessMethod();
    
        List<String> anotherBusinessMethod();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import javax.interceptor.InvocationContext;
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * @version $Rev: 808273 $ $Date: 2009-08-26 20:42:06 -0700 (Wed, 26 Aug 2009) $
     */
    public class Utils {
    
        public static List<String> addClassSimpleName(InvocationContext ic, String classSimpleName) throws Exception {
            List<String> list = new ArrayList<String>();
            list.add(classSimpleName);
            List<String> listOfStrings = (List<String>) ic.proceed();
            if (listOfStrings != null) {
                list.addAll(listOfStrings);
            }
            return list;
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import junit.framework.TestCase;
    import org.junit.After;
    import org.junit.Before;
    import org.junit.Test;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Properties;
    
    /**
     * @version $Rev: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public class FullyInterceptedTest extends TestCase {
    
        private InitialContext initCtx;
    
        @Before
        public void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.deployments.classpath.include", ".*interceptors/target/classes.*");
    
            initCtx = new InitialContext(properties);
        }
    
        @Test
        public void testBusinessMethod() throws Exception {
    
            FullyIntercepted fullyIntercepted = (FullyIntercepted) initCtx.lookup("FullyInterceptedBeanLocal");
    
            assert fullyIntercepted != null;
    
            List<String> expected = new ArrayList<String>();
            expected.add("DefaultInterceptorOne");
            expected.add("DefaultInterceptorTwo");
            expected.add("ClassLevelInterceptorSuperClassOne");
            expected.add("ClassLevelInterceptorSuperClassTwo");
            expected.add("ClassLevelInterceptorOne");
            expected.add("ClassLevelInterceptorTwo");
            expected.add("MethodLevelInterceptorOne");
            expected.add("MethodLevelInterceptorTwo");
            expected.add("beanClassBusinessMethodInterceptor");
            expected.add("businessMethod");
    
            List<String> actual = fullyIntercepted.businessMethod();
            assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
        }
    
        @Test
        public void testMethodWithDefaultInterceptorsExcluded() throws Exception {
    
            FullyIntercepted fullyIntercepted = (FullyIntercepted) initCtx.lookup("FullyInterceptedBeanLocal");
    
            assert fullyIntercepted != null;
    
            List<String> expected = new ArrayList<String>();
            expected.add("ClassLevelInterceptorSuperClassOne");
            expected.add("ClassLevelInterceptorSuperClassTwo");
            expected.add("ClassLevelInterceptorOne");
            expected.add("ClassLevelInterceptorTwo");
            expected.add("MethodLevelInterceptorOne");
            expected.add("MethodLevelInterceptorTwo");
            expected.add("beanClassBusinessMethodInterceptor");
            expected.add("methodWithDefaultInterceptorsExcluded");
    
            List<String> actual = fullyIntercepted.methodWithDefaultInterceptorsExcluded();
            assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
        }
    
        @After
        public void tearDown() throws Exception {
            initCtx.close();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import junit.framework.TestCase;
    import org.junit.Before;
    import org.junit.Test;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Properties;
    
    /**
     * @version $Rev: 895825 $ $Date: 2010-01-04 15:35:22 -0800 (Mon, 04 Jan 2010) $
     */
    public class MethodLevelInterceptorOnlyTest extends TestCase {
        private InitialContext initCtx;
    
        @Before
        public void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.deployments.classpath.include", ".*interceptors/target/classes.*");
    
            initCtx = new InitialContext(properties);
        }
    
        @Test
        public void testInterceptedGenerifiedBusinessIntfMethod() throws Exception {
            MethodLevelInterceptorOnlyParent bean = (MethodLevelInterceptorOnlyParent) initCtx.lookup("MethodLevelInterceptorOnlySLSBeanLocal");
    
            assert bean != null;
    
            List<String> expected = new ArrayList<String>();
            expected.add("MethodLevelInterceptorOne");
            expected.add("makePersistent");
    
            List<String> actual = bean.makePersistent(null);
            assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
        }
    }/**
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
    package org.superbiz.interceptors;
    
    import junit.framework.TestCase;
    import org.junit.Before;
    import org.junit.Test;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Properties;
    
    /**
     * @version $Rev: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public class SecondStatelessInterceptedTest extends TestCase {
    
        private InitialContext initCtx;
    
        @Before
        public void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.deployments.classpath.include", ".*interceptors/target/classes.*");
    
            initCtx = new InitialContext(properties);
        }
    
        @Test
        public void testMethodWithDefaultInterceptorsExcluded() throws Exception {
            SecondStatelessInterceptedLocal bean =
                    (SecondStatelessInterceptedLocal) initCtx.lookup("SecondStatelessInterceptedBeanLocal");
    
            assert bean != null;
    
            List<String> expected = new ArrayList<String>();
            expected.add("ClassLevelInterceptorOne");
            expected.add("ClassLevelInterceptorTwo");
            expected.add("MethodLevelInterceptorOne");
            expected.add("MethodLevelInterceptorTwo");
            expected.add("SecondStatelessInterceptedBean");
            expected.add("methodWithDefaultInterceptorsExcluded");
    
            List<String> actual = bean.methodWithDefaultInterceptorsExcluded();
            assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.interceptors;
    
    import junit.framework.TestCase;
    import org.junit.Before;
    import org.junit.Test;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Properties;
    
    /**
     * @version $Rev: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public class ThirdSLSBeanTest extends TestCase {
        private InitialContext initCtx;
    
        @Before
        public void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.deployments.classpath.include", ".*interceptors/target/classes.*");
    
            initCtx = new InitialContext(properties);
        }
    
        @Test
        public void testMethodWithDefaultInterceptorsExcluded() throws Exception {
            ThirdSLSBeanLocal bean = (ThirdSLSBeanLocal) initCtx.lookup("ThirdSLSBeanLocal");
    
            assert bean != null;
    
            List<String> expected = new ArrayList<String>();
            expected.add("ClassLevelInterceptorOne");
            expected.add("ClassLevelInterceptorTwo");
            expected.add("MethodLevelInterceptorOne");
            expected.add("MethodLevelInterceptorTwo");
            expected.add("ThirdSLSBean");
            expected.add("businessMethod");
    
            List<String> actual = bean.businessMethod();
            assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
        }
    
        @Test
        public void testMethodWithDefaultAndClassInterceptorsExcluded() throws Exception {
            ThirdSLSBeanLocal bean = (ThirdSLSBeanLocal) initCtx.lookup("ThirdSLSBeanLocal");
    
            assert bean != null;
    
            List<String> expected = new ArrayList<String>();
            expected.add("MethodLevelInterceptorOne");
            expected.add("MethodLevelInterceptorTwo");
            expected.add("ThirdSLSBean");
            expected.add("anotherBusinessMethod");
    
            List<String> actual = bean.anotherBusinessMethod();
            assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
        }
    }
