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

package org.apache.openejb.arquillian.tests.core.ejb.ejbjar;

import org.apache.openejb.arquillian.tests.Runner;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class DescriptorComplementsAnnotationsTest {
    private static final String TEST_NAME = DescriptorComplementsAnnotationsTest.class.getSimpleName();
    private static final String SERVLET_NAME = "TestServlet";
    private static final java.lang.String RESOURCE_EJB_JAR_XML = "ejb-jar.xml";
    private static final java.lang.String CONTENT_LOCATION_EJB_JAR_XML = "org/apache/openejb/arquillian/tests/core/ejb/descriptor_complements_annotations.xml";

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version(WebAppVersionType._3_0)
                .createServlet()
                .servletName(SERVLET_NAME)
                .servletClass(DescriptorComplementsAnnotationsServlet.class.getName()).up()
                .createServletMapping()
                .servletName(SERVLET_NAME)
                .urlPattern("/" + TEST_NAME).up();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(DescriptorComplementsAnnotationsTest.class)
                .addClass(DescriptorComplementsAnnotationsServlet.class)
                .addClass(Bean.class)
                .addClass(FirstBean.class)
                .addClass(SecondBean.class)
                .addClass(Runner.class)
                .addAsLibraries(JarLocation.jarLocation(Test.class))
                .addAsWebInfResource(new ClassLoaderAsset(CONTENT_LOCATION_EJB_JAR_XML), RESOURCE_EJB_JAR_XML)
                .setWebXML(new StringAsset(descriptor.exportAsString()));

        return archive;
    }

    private void validateTest(String testName) throws IOException {
        final String expectedOutput = testName + "=true";

        try (InputStream is = new URL(url.toExternalForm() + TEST_NAME + "?test=" + testName).openStream()) {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();

            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = is.read(buffer)) > -1) {
                os.write(buffer, 0, bytesRead);
            }

            final String output = new String(os.toByteArray(), "UTF-8");
            assertNotNull("Response shouldn't be null", output);
            assertTrue("Output should contain: " + expectedOutput
                    + "\nActual output:\n" + output, output.contains(expectedOutput));
        }
    }

    @Test
    public void verifyFirstBeanInjection() throws IOException {
        validateTest("verifyFirstBeanInjection");
    }

    @Test
    public void verifySecondBeanInjection() throws IOException {
        validateTest("verifySecondBeanInjection");
    }

    @Test
    public void verifyOverridenBeanInjection() throws IOException {
        validateTest("verifyOverridenBeanInjection");
    }


}
