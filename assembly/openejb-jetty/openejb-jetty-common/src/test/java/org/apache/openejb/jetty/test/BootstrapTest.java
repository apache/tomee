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
package org.apache.openejb.jetty.test;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.jetty.common.OpenEJBLifecycle;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.InitialContext;
import java.io.IOException;

public class BootstrapTest {
    private static final int SERVER_PORT = 8091;
    private static final String SERVER_BASE_URL = "http://localhost:" + SERVER_PORT;
    private Server server;

    @Before
    public void setup() throws Exception {
        OpenEJBLifecycle ejbLifecycle = new OpenEJBLifecycle();
        ejbLifecycle.addApplication("target/test/ejb-examples-1.1-SNAPSHOT.war");

        server = new Server(SERVER_PORT);
        server.addBean(ejbLifecycle);
        ejbLifecycle.setServer(server);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        OpenEJB.destroy();
        InitialContext context = new InitialContext();
        context.unbind("java:openejb");
    }

    @Test
    public void testNothing(){}
    
//    @Test TODO
    public void testShouldInjectEjbsIntoServlet() throws Exception {
        String url = SERVER_BASE_URL + "/annotated";

        String[] stringsToCheck = new String[] { "@EJB=proxy=org.superbiz.servlet.AnnotatedEJBLocal;deployment=AnnotatedEJB;pk=null",
            "@EJB.getName()=foo",
            "@EJB.getDs()=org.apache.openejb.resource.jdbc.BasicManagedDataSource",
            "JNDI=proxy=org.superbiz.servlet.AnnotatedEJBLocal;deployment=AnnotatedEJB;pk=null",
            "@EJB=proxy=org.superbiz.servlet.AnnotatedEJBRemote;deployment=AnnotatedEJB;pk=null",
            "JNDI=proxy=org.superbiz.servlet.AnnotatedEJBRemote;deployment=AnnotatedEJB;pk=null",
            "@Resource=org.apache.openejb.resource.jdbc.BasicManagedDataSource",
            "JNDI=org.apache.openejb.resource.jdbc.BasicManagedDataSource" };

        checkStringsAppearOnPage(url, stringsToCheck);
    }

    private void checkStringsAppearOnPage(String url, String[] stringsToCheck) throws IOException {
        final WebClient client = new WebClient();
        TextPage page = client.getPage(url);

        for (String stringToCheck : stringsToCheck) {
            Assert.assertTrue(page.getContent().contains(stringToCheck));
        }
    }

//    @Test TODO
    public void testShouldLoadPersistenceContext() throws Exception {
        String url = SERVER_BASE_URL + "/jpa";

        String[] stringsToCheck = new String[] { "@PersistenceUnit=org.apache.openjpa.persistence.EntityManagerFactoryImpl",
            "Loaded [JpaBean id=",
            "Removed [JpaBean id=" };

        checkStringsAppearOnPage(url, stringsToCheck);
    }

//    @Test TODO
    public void testShouldJndiTree() throws Exception {
        String url = SERVER_BASE_URL + "/jndi";

        String[] stringsToCheck = new String[] { "env=",
            "env/__=",
            "env/__/web.xml=",
            "env/__/web.xml/env-entry=org.eclipse.jetty.plus.jndi.EnvEntry",
            "env/jpa-example=org.apache.openjpa.persistence.EntityManagerFactoryImpl",
            "env/org.superbiz.servlet.AnnotatedServlet=",
            "env/org.superbiz.servlet.AnnotatedServlet/ds=org.apache.openejb.resource.jdbc.BasicManagedDataSource",
            "env/org.superbiz.servlet.AnnotatedServlet/localEJB=proxy=org.superbiz.servlet.AnnotatedEJBLocal;deployment=AnnotatedEJB;pk=null",
            "env/org.superbiz.servlet.AnnotatedServlet/remoteEJB=proxy=org.superbiz.servlet.AnnotatedEJBRemote;deployment=AnnotatedEJB;pk=null",
            "env/org.superbiz.servlet.RunAsServlet=",
            "env/org.superbiz.servlet.RunAsServlet/secureEJBLocal=proxy=org.superbiz.servlet.SecureEJBLocal;deployment=SecureEJB;pk=null",
            "env/org.superbiz.servlet.SecureServlet=",
            "env/org.superbiz.servlet.SecureServlet/secureEJBLocal=proxy=org.superbiz.servlet.SecureEJBLocal;deployment=SecureEJB;pk=null",
            "env/web.xml=",
            "env/web.xml/Data Source=org.apache.openejb.resource.jdbc.BasicManagedDataSource",
            "env/web.xml/EjbRemote=proxy=org.superbiz.servlet.AnnotatedEJBRemote;deployment=AnnotatedEJB;pk=null",
            "env/web.xml/EjLocal=proxy=org.superbiz.servlet.AnnotatedEJBLocal;deployment=AnnotatedEJB;pk=null",
            "env/web.xml/env-entry=WebValue",
            "env/web.xml/PersistenceContext=org.apache.openejb.persistence.JtaEntityManager",
            "env/web.xml/PersistenceUnit=org.apache.openjpa.persistence.EntityManagerFactoryImpl",
            "env/web.xml/Queue=queue://web.xml/Queue" };

        checkStringsAppearOnPage(url, stringsToCheck);
    }
}
