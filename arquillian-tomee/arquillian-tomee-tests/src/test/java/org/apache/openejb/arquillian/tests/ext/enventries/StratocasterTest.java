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
package org.apache.openejb.arquillian.tests.ext.enventries;

import org.apache.openejb.arquillian.tests.Tests;
import org.apache.openejb.arquillian.tests.enventry.Code;
import org.apache.openejb.arquillian.tests.enventry.PojoServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.Node;
import org.jboss.shrinkwrap.descriptor.spi.NodeProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class StratocasterTest {

    public static final String TEST_NAME = StratocasterTest.class.getSimpleName();

    @Test
    public void lookupEnvEntryInjectionShouldSucceed() throws Exception {
        validateTest("[passed]");
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .servlet(Stratocaster.class, "/" + TEST_NAME);

        addEnvEntry(descriptor, "guitarStringGuages", "java.lang.String", "E1=0.052\nA=0.042\nD=0.030\nG=0.017\nB=0.013\nE=0.010");
        addEnvEntry(descriptor, "certificateOfAuthenticity", "java.lang.String", "/tmp/strat-certificate.txt");
        addEnvEntry(descriptor, "dateCreated", "java.lang.String", "1962-03-01");
        addEnvEntry(descriptor, "pickups", "java.lang.String", "S,S,S");
        addEnvEntry(descriptor, "style", "java.lang.String", "VINTAGE");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(PojoServlet.class)
                .addClass(Code.class)
                .addClass(Stratocaster.class)
                .addClass(Pickup.class)
                .addClass(PickupEditor.class)
                .addClass(Style.class)
                .addAsLibraries(new File("target/test-libs/junit.jar"))
                .setWebXML(new StringAsset(descriptor.exportAsString()));

        System.err.println(descriptor.exportAsString());

        return archive;
    }

    private static void addEnvEntry(WebAppDescriptor descriptor, String name, String type, String value) {
        Node rootNode = ((NodeProvider) descriptor).getRootNode();
        Node appNode = rootNode.get("/web-app").iterator().next();
        appNode.create("/env-entry")
                .create("env-entry-name").text(name)
                .parent()
                .create("env-entry-type").text(type)
                .parent()
                .create("env-entry-value").text(value);

    }

    private void validateTest(String expectedOutput) throws IOException {
        Tests.assertOutput("http://localhost:9080/" + TEST_NAME + "/" + TEST_NAME, expectedOutput);
    }

}
