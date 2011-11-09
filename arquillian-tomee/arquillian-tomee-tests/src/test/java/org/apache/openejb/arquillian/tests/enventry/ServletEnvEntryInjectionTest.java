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
package org.apache.openejb.arquillian.tests.enventry;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * TODO Add float and double
 */
@RunWith(Arquillian.class)
public class ServletEnvEntryInjectionTest {

    public static final String TEST_NAME = ServletEnvEntryInjectionTest.class.getSimpleName();


    @Test
    public void stringEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "tomee@apache.org";
        validateTest(expectedOutput);
    }

    @Test
    public void integerTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Connection Pool: 20";
        validateTest(expectedOutput);
    }

    @Test
    public void longTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Start Count: 200000";
        validateTest(expectedOutput);
    }

    @Test
    public void shortTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Init Size: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void byteTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Total Quantity: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void booleanTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Enable Email: true";
        validateTest(expectedOutput);
    }

    @Test
    public void charTypeEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Option Default: X";
        validateTest(expectedOutput);
    }

    @Test
    public void classEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "java.lang.String";
        validateTest(expectedOutput);
    }

    @Test
    public void enumEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "DefaultCode: OK";
        validateTest(expectedOutput);
    }

    @Test
    public void lookupEnvEntryInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Name:";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .servlet(PojoServlet.class, "/" + TEST_NAME);

        addEnvEntry(descriptor, "returnEmail", "java.lang.String", "tomee@apache.org");
        addEnvEntry(descriptor, "connectionPool", "java.lang.Integer", "20");
        addEnvEntry(descriptor, "startCount", "java.lang.Long", "200000");
        addEnvEntry(descriptor, "initSize", "java.lang.Short", "5");
        addEnvEntry(descriptor, "enableEmail", "java.lang.Boolean", "true");
        addEnvEntry(descriptor, "totalQuantity", "java.lang.Byte", "5");
        addEnvEntry(descriptor, "optionDefault", "java.lang.Character", "X");
        addEnvEntry(descriptor, "auditWriter", "java.lang.Class", "java.lang.String");
        addEnvEntry(descriptor, "defaultCode", Code.class.getName(), "OK");

        Node appNode = ((NodeDescriptor) descriptor).getRootNode();
        appNode.createChild("/env-entry")
                .createChild("env-entry-name").text("name").getParent()
                .createChild("lookup-name").text("java:module/ModuleName");


        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PojoServlet.class)
                .addClass(Code.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()));



        return archive;
    }

    private static void addEnvEntry(WebAppDescriptor descriptor, String name, String type, String value) {
        Node appNode = ((NodeDescriptor) descriptor).getRootNode();
        appNode.createChild("/env-entry")
                .createChild("env-entry-name").text(name).getParent()
                .createChild("env-entry-type").text(type).getParent()
                .createChild("env-entry-value").text(value)
        ;

    }

    private void validateTest(String expectedOutput) throws IOException {
        final InputStream is = new URL("http://localhost:9080/" + TEST_NAME + "/" + TEST_NAME).openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        String output = new String(os.toByteArray(), "UTF-8");
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + expectedOutput, output.contains(expectedOutput));
    }

}



