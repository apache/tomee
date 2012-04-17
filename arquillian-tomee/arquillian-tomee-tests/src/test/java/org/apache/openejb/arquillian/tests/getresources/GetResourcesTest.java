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

package org.apache.openejb.arquillian.tests.getresources;

import java.io.IOException;
import java.net.URL;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.openejb.arquillian.tests.Tests.assertOutput;

/**
 * jira: TOMEE-42.
 *
 */
@RunWith(Arquillian.class)
public class GetResourcesTest {
    public static final String TEST_NAME = GetResourcesTest.class.getSimpleName();

    @ArquillianResource
    private URL url;

    @Deployment(testable = false) public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(GetResourcesServletExporter.class)
                .addClass(GetResourcesListener.class)
                .addClass(GetResourcesHolder.class)
                .addAsWebResource(Thread.currentThread().getContextClassLoader().getResource("test.getresources"), "/config/test.getresources")
                .addAsWebResource(Thread.currentThread().getContextClassLoader().getResource("test.getresources"), "/config/test.getresources2")
                .addAsLibraries(JarLocation.jarLocation(Test.class))
                .setWebXML(new StringAsset(
                      Descriptors.create(WebAppDescriptor.class)
                        .version("3.0").exportAsString()));
    }

    @Test public void check() throws IOException {
        assertOutput(url.toExternalForm() + "get-resources", "foundFromListener=1");
        assertOutput(url.toExternalForm() + TEST_NAME + "get-resources", "servletContextGetResource=ok");
    }
}
