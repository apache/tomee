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
package org.apache.openejb.arquillian.tests.jsf.ejb;

import org.apache.openejb.arquillian.tests.jsf.JSFs;
import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class JSFInjectionTest extends JSFs {
    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return base("jsf-injection-test.war")
                .addClasses(DummyEJB.class, DummyManagedBean.class)
                .addAsWebResource(new ClassLoaderAsset(
                        JSFInjectionTest.class.getPackage().getName().replace('.', '/').concat("/").concat("dummy.xhtml")), "dummy.xhtml");
    }

    @Test
    public void testEjbInjection() throws Exception {
        validateTest("foobar");
    }

    private void validateTest(final String expectedOutput) throws IOException {
        final String output = IO.slurp(new URL(url.toExternalForm() + "dummy.xhtml"));
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + expectedOutput + "; and not " + output, output.contains(expectedOutput));
    }
}
