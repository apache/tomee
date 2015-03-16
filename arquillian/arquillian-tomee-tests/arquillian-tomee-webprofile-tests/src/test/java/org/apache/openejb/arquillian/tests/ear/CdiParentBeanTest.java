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

import org.apache.openejb.arquillian.tests.ear.cdi.SimpleBean;
import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertFalse;

/**
 * @version $Rev$ $Date$
 */
@RunAsClient
@RunWith(Arquillian.class)
public class CdiParentBeanTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return ShrinkWrap.create(EnterpriseArchive.class, "myear.ear")
                .addAsLibraries(ShrinkWrap.create(JavaArchive.class, "parent-cdi-beans.jar")
                                .addClass(SimpleBean.class)
                                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"))
                .addAsModule(ShrinkWrap.create(WebArchive.class, "web.war")
                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                        .addClasses(ClassLoaderServlet.class, CdiParentBeanTest.class));
    }

    @Test
    public void test() throws Exception {
        final String slurp = IO.slurp(new URL(url, "/myear/web/classloader"));
        assertFalse(slurp + " should not contain WebappClassLoader", slurp.toLowerCase().contains("webappclassloader"));
    }
}
