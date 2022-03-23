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
package org.apache.openejb.arquillian;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import jakarta.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class TomEEContainerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addClass(TestServlet.class).addClass(TestEjb.class).addClass(TomEEContainerTest.class)
                .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class).version("3.0")
                        .createServlet().servletName("servlet").servletClass(TestServlet.class.getName()).up()
                        .createServletMapping().servletName("servlet").urlPattern("/ejb").up()
                    .exportAsString()));
    }

    @EJB
    private TestEjb ejb;

    @Test
    public void testEjbIsNotNull() throws Exception {
        assertNotNull(ejb);
    }

    @Test
    public void testShouldBeAbleToAccessServletAndEjb() throws Exception {
        // using this hook to get the url since we are deployed in the container and we don't have metadata
        // to get the url injected...we probably need 2 deployments for such a test
        URL url = new URL("http://127.0.0.1:" + System.getProperty("tomee.httpPort", "10080") + "/test/ejb");
        InputStream is = url.openStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        String output = new String(os.toByteArray(), "UTF-8");
        assertTrue(output.contains("Hello, OpenEJB"));
    }

}
