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
package org.apache.openejb.arquillian.tests.jsp;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class DataEarTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        final JavaArchive clientJar = ShrinkWrap.create(JavaArchive.class, "client.jar")
                .addClasses(Data.class, DataBusiness.class, DataBusinessHome.class);

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb-jar.jar")
                .addClasses(DataBusinessBean.class)
                .addAsResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/jsp/ejb-jar.xml"), "META-INF/ejb-jar.xml");

        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war")
                .add(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/jsp/test.jsp"), "test.jsp")
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/jsp/web.xml"), "web.xml");

        final EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsLibrary(clientJar)
                .addAsModule(ejbJar)
                .addAsModule(testWar);

        System.out.println(archive.toString(true));
        return archive;
    }


    @Test
    @RunAsClient
    public void ejbCallFromJsp() throws Exception {
        final String output = IO.slurp(new URL("http://" + url.getHost() + ":" + url.getPort() + "/test/test/test.jsp"));
        System.out.println(output);
        Assert.assertEquals("this is a test", output.trim());
    }
}