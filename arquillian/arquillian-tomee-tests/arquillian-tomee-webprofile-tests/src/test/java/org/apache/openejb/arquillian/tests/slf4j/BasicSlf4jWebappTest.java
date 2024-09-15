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
package org.apache.openejb.arquillian.tests.slf4j;

import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class BasicSlf4jWebappTest {
    @ArquillianResource(SimpleServlet.class)
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        final WebArchive archive = new WebModule(BasicSlf4jWebappTest.class.getSimpleName()).getArchive();
        archive.addClass(SimpleServlet.class);
        archive.add(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/slf4j/logback.xml"), "WEB-INF/logback.xml");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Test
    public void validate() throws Exception {
        final String launchProfile = System.getProperty("arquillian.launch");
        if ("tomee-embedded".equals(launchProfile)) {
            System.out.println("Skipping this test in TomEE embedded");
            return;
        }

        final InputStream is = new URL(url.toExternalForm() + "logtest").openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        final String output = os.toString(StandardCharsets.UTF_8);
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + "It works!" + "\n" + output, output.contains("It works!"));
        assertTrue("Output should contain: " + "Logger Factory: org.slf4j.jul.JDK14LoggerFactory" + "\n" + output, output.contains("Logger Factory: org.slf4j.jul.JDK14LoggerFactory"));
        assertTrue("Output should contain: " + "slf4j-jdk14-2.0.16.jar" + "\n" + output, output.contains("slf4j-jdk14-2.0.16.jar"));
    }
}
