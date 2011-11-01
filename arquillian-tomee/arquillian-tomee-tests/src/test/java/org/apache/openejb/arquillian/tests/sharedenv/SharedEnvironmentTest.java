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
package org.apache.openejb.arquillian.tests.sharedenv;

import org.apache.openejb.arquillian.tests.TestRun;
import org.apache.openejb.arquillian.tests.TestSetup;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;



@RunWith(Arquillian.class)
public class SharedEnvironmentTest extends TestSetup {

    public static final String TEST_NAME = SharedEnvironmentTest.class.getSimpleName();

    @Test
    public void testCdi() throws Exception {
        validateTest("testCdi=true");
    }

    @Test
    public void testEjb() throws Exception {
        validateTest("testEjb=true");
    }

    @Test
    public void testFilter() throws Exception {
        validateTest("testFilter=true");
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        WebArchive deployment = new SharedEnvironmentTest().createDeployment(TestRun.class, PojoServletFilter.class, Orange.class, Green.class, Environment.class);
        deployment.as(ExplodedExporter.class).exportExploded(new File("/tmp"));
		return deployment;
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor.filter(PojoServletFilter.class, "/" + getTestContextName());
        addEnvEntry(descriptor, "returnEmail", "java.lang.String", "tomee@apache.org");
        addEnvEntry(descriptor, "connectionPool", "java.lang.Integer", "20");
        addEnvEntry(descriptor, "startCount", "java.lang.Long", "200000");
        addEnvEntry(descriptor, "initSize", "java.lang.Short", "6");
        addEnvEntry(descriptor, "enableEmail", "java.lang.Boolean", "true");
        addEnvEntry(descriptor, "totalQuantity", "java.lang.Byte", "5");
        addEnvEntry(descriptor, "optionDefault", "java.lang.Character", "X");
    }


    private static void addEnvEntry(WebAppDescriptor descriptor, String name, String type, String value) {
        Node appNode = ((NodeDescriptor) descriptor).getRootNode();
        appNode.createChild("/env-entry")
                .createChild("env-entry-name").text(name).getParent()
                .createChild("env-entry-type").text(type).getParent()
                .createChild("env-entry-value").text(value);

    }
}



