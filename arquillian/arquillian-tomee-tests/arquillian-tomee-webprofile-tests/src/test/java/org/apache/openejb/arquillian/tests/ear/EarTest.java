/*
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
package org.apache.openejb.arquillian.tests.ear;

import org.apache.openejb.loader.JarLocation;
import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
@RunWith(Arquillian.class)
public class EarTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "beans.jar");
        ejbJar.addClass(Bean.class);
        ejbJar.addClass(EarTest.class);

        final WebArchive webapp = ShrinkWrap.create(WebArchive.class, "green.war").addClass(Hello.class);
        System.out.println(webapp.toString(true));
        System.out.println();

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "red.ear").addAsModule(ejbJar).addAsModule(webapp);
        ear.addAsLibraries(JarLocation.jarLocation(Test.class));

        System.out.println(ear.toString(true));
        System.out.println();

        return ear;
    }

    @Test
    public void test() throws Exception {

        final URL servlet = new URL(url, "/red/green/blue");

        System.out.println(servlet.toExternalForm());
        final String slurp = IO.slurp(servlet);
        Assert.assertEquals(Test.class.getName(), slurp);
        System.out.println(slurp);
    }


    @Singleton
    @Startup
    public static class Bean {

        @PostConstruct
        private void post() throws InterruptedException {
//            Thread.sleep(TimeUnit.MINUTES.toMillis(1));
        }

        public String getMessage() {
            return Test.class.getName();
        }
    }

    @WebServlet("/blue")
    public static class Hello extends HttpServlet {

        @EJB
        private Bean bean;

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().print(bean.getMessage());
        }
    }
}
