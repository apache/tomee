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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.arquillian.tests.jaxws.dd;

import org.apache.openejb.arquillian.tests.jaxws.Hello;
import org.apache.openejb.arquillian.tests.jaxws.HelloWS;
import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class JAXWSDDTest {
    @ArquillianResource
    private URL deployedUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0");

        return ShrinkWrap.create(WebArchive.class, JAXWSDDTest.class.getSimpleName() + ".war")
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addClass(Hello.class).addClass(HelloWS.class)
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/jaxws/dd/ejb-jar.xml"), "ejb-jar.xml");
    }

    @Test
    public void invokeStdDeployment() throws Exception {
        checkWSDLExists("HelloWS");
    }

    @Test
    public void invokeDDDeployment() throws Exception {
        checkWSDLExists("HelloWSDD");
    }

    private void checkWSDLExists(final String name) throws Exception {
        final URL url = new URL(deployedUrl.toExternalForm() + "/webservices/" + name + "?wsdl");
        assertTrue(IO.slurp(url).contains(name));
    }
}
