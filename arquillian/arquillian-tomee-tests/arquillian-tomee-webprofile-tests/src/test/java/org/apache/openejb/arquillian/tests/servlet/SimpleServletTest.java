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
package org.apache.openejb.arquillian.tests.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.openejb.arquillian.tests.TestRun;
import org.apache.openejb.arquillian.tests.TestSetup;
import org.apache.openejb.arquillian.tests.resenventry.Blue;
import org.apache.openejb.arquillian.tests.resenventry.Green;
import org.apache.openejb.arquillian.tests.resenventry.Orange;
import org.apache.openejb.arquillian.tests.resenventry.Purple;
import org.apache.openejb.arquillian.tests.resenventry.Red;
import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


@RunWith(Arquillian.class)
public class SimpleServletTest {
    @ArquillianResource(SimpleServlet.class)
    private URL url;

    @Test
    public void testRed() throws Exception {
        validateTest("simple");
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new WebModule(SimpleServletTest.class, SimpleServletTest.class).getArchive();
    }

    protected void validateTest(String expectedOutput) throws IOException {
        final InputStream is = new URL(url + "simple").openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        final String output = new String(os.toByteArray(), "UTF-8");
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + expectedOutput + "\n" + output, output.contains(expectedOutput));
    }
}



