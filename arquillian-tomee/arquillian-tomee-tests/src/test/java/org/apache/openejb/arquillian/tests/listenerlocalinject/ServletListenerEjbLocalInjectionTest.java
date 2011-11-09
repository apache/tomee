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
package org.apache.openejb.arquillian.tests.listenerlocalinject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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

@RunWith(Arquillian.class)
public class ServletListenerEjbLocalInjectionTest {

    public static final String TEST_NAME = ServletListenerEjbLocalInjectionTest.class.getSimpleName();

    @Test
    public void localEjbInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Local: OpenEJB is employed at TomEE Software Inc.";
        validateTest(expectedOutput);
    }

    @Test
    public void localBeanEjbInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: OpenEJB shops at Apache Marketplace";
        validateTest(expectedOutput);
    }

//    @Test
    public void pojoInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: OpenEJB is on the wheel of a 2011 Lexus IS 350";
        validateTest(expectedOutput);
    }

    @Test
    public void stringEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: tomee@apache.org";
        validateTest(expectedOutput);
    }

    @Test
    public void integerTypeEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Connection Pool: 20";
        validateTest(expectedOutput);
    }

//    @Test
    public void longTypeEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Start Count: 200000";
        validateTest(expectedOutput);
    }

    @Test
    public void shortTypeEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Init Size: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void byteTypeEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Total Quantity: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void booleanTypeEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Enable Email: true";
        validateTest(expectedOutput);
    }

    @Test
    public void charTypeEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: Option Default: X";
        validateTest(expectedOutput);
    }

//    @Test
    public void classEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: java.lang.String";
        validateTest(expectedOutput);
    }

//    @Test
    public void enumEnvEntryInjectionShouldSucceedInCtxtListener() throws Exception {
        final String expectedOutput = "Context: DefaultCode: OK";
        validateTest(expectedOutput);
    }

    @Test
    public void localEjbInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Local: OpenEJB is employed at TomEE Software Inc.";
        validateTest(expectedOutput);
    }

    @Test
    public void localBeanEjbInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: OpenEJB shops at Apache Marketplace";
        validateTest(expectedOutput);
    }

//    @Test
    public void pojoInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: OpenEJB is on the wheel of a 2011 Lexus IS 350";
        validateTest(expectedOutput);
    }

    @Test
    public void stringEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: tomee@apache.org";
        validateTest(expectedOutput);
    }

    @Test
    public void integerTypeEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Connection Pool: 20";
        validateTest(expectedOutput);
    }

//    @Test
    public void longTypeEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Start Count: 200000";
        validateTest(expectedOutput);
    }

    @Test
    public void shortTypeEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Init Size: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void byteTypeEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Total Quantity: 5";
        validateTest(expectedOutput);
    }

    @Test
    public void booleanTypeEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Enable Email: true";
        validateTest(expectedOutput);
    }

    @Test
    public void charTypeEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: Option Default: X";
        validateTest(expectedOutput);
    }

//    @Test
    public void classEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: java.lang.String";
        validateTest(expectedOutput);
    }

//    @Test
    public void enumEnvEntryInjectionShouldSucceedInSessionListener() throws Exception {
        final String expectedOutput = "Session: DefaultCode: OK";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .listener(PojoServletContextListener.class)
                .listener(PojoServletSessionListener.class)
                .servlet(ServletToCheckListener.class, "/" + TEST_NAME);

        addEnvEntry(descriptor, "returnEmail", "java.lang.String", "tomee@apache.org");
        addEnvEntry(descriptor, "connectionPool", "java.lang.Integer", "20");
        addEnvEntry(descriptor, "startCount", "java.lang.Long", "200000");
        addEnvEntry(descriptor, "initSize", "java.lang.Short", "5");
        addEnvEntry(descriptor, "enableEmail", "java.lang.Boolean", "true");
        addEnvEntry(descriptor, "totalQuantity", "java.lang.Byte", "5");
        addEnvEntry(descriptor, "optionDefault", "java.lang.Character", "X");
        addEnvEntry(descriptor, "auditWriter", "java.lang.Class", "java.lang.String");
        //addEnvEntry(descriptor, "defaultCode", "java.lang.Enum", "org.apache.openejb.arquillian.ServletListenerPojoInjectionTest$Code.OK");

        final WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PojoServletContextListener.class)
                .addClass(PojoServletSessionListener.class)
                .addClass(ServletToCheckListener.class)
                .addClass(Car.class)
                .addClass(CompanyLocal.class)
                .addClass(Company.class)
                .addClass(DefaultCompany.class)
                .addClass(SuperMarket.class)
                .addClass(Code.class)
                .addClass(ContextAttributeName.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));



        return archive;
    }

    private static void addEnvEntry(WebAppDescriptor descriptor, String name, String type, String value) {
        Node appNode = ((NodeDescriptor) descriptor).getRootNode();
        appNode.createChild("/env-entry")
                .createChild("env-entry-name").text(name).getParent()
                .createChild("env-entry-type").text(type).getParent()
                .createChild("env-entry-value").text(value)
/*
                .parent()
                .create("injection-target")
                .create("injection-target-class").text("org.apache.openejb.arquillian.ServletPojoInjectionTest$PojoServletContextListener")
                .parent()
                .create("injection-target-name").text(name)
*/
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



