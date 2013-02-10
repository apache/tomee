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
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class CdiParentBeanTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        final JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "parent-cdi-beans.jar")
                        .addClass(Bean.class)
                        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "myear.ear")
                .addAsLibraries(lib)
                .addAsModule(ShrinkWrap.create(WebArchive.class, "web.war")
                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                        .addClass(ClassLoaderServlet.class));
        ear.addAsLibraries(JarLocation.jarLocation(Test.class));

        return ear;
    }

    @Test
    public void test() throws Exception {
        final String slurp = IO.slurp(new URL(url, "/myear/web/classloader"));
        assertFalse(slurp + " should not contain WebappClassLoader", slurp.toLowerCase().contains("webappclassloader"));
    }

    public static class Bean {
        public String classloaderAsStr() {
            return getClass().getClassLoader().toString();
        }
    }

    @WebServlet("/classloader")
    public static class ClassLoaderServlet extends HttpServlet {
        @Inject
        private Bean bean;

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().print(bean.classloaderAsStr());
        }
    }
}
