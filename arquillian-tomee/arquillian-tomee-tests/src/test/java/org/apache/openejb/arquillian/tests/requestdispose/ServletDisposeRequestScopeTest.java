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
package org.apache.openejb.arquillian.tests.requestdispose;

import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletDisposeRequestScopeTest {

    public static final String TEST_NAME = ServletDisposeRequestScopeTest.class.getSimpleName();

    @Test
    public void dispose() throws Exception {
        invoke();
        final String secondOutput = invoke();
        assertTrue(secondOutput.contains("disposed=true")); // check the previous cycle disposed the bean
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return new WebModule(ServletDisposeRequestScopeTest.class, ServletDisposeRequestScopeTest.class)
                .getArchive()
                .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
                        .version("3.0").exportAsString()))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    private String invoke() throws IOException {
        final InputStream is = new URL("http://localhost:" + System.getProperty("tomee.httpPort", "11080") + "/" + TEST_NAME + "/test").openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[512];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        return new String(os.toByteArray(), "UTF-8");
    }

}



