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
package org.apache.openejb.arquillian.tests.cdi.producer;

import java.net.URL;
import org.apache.openejb.arquillian.tests.TestRun;
import org.apache.openejb.arquillian.tests.Tests;
import org.apache.openejb.arquillian.tests.cdi.constructor.ServletCdiConstructorInjectionTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class ServletCdiProducerTest {
    public static final String TEST_NAME = ServletCdiConstructorInjectionTest.class.getSimpleName();

    @ArquillianResource
    private URL url;

    @Test
    public void pojoInjectionShouldSucceed() throws Exception {
        validateTest("OpenEJB is on the wheel of a 2011 Lexus IS 350");
    }

    @Test
    public void beanManagerInjectionShouldSucceed() throws Exception {
        validateTest("beanManager");
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .servlet(SimpleServlet.class, "/" + TEST_NAME);

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(TestRun.class)
                .addClass(SimpleServlet.class)
                .addClass(Car.class)
                .addClass(AssemblyLine.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));



        return archive;
    }

    private void validateTest(String expectedOutput) throws IOException {
        Tests.assertOutput(url.toExternalForm() + TEST_NAME, expectedOutput);
    }

}
