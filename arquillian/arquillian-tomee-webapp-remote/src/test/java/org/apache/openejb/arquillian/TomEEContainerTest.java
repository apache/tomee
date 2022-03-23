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

import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

// todo: add arquillian enricher to use @ArquillianResource URl url;
@RunWith(Arquillian.class)
public class TomEEContainerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
        		.addClass(TestServlet.class).addClass(TestEjb.class).addClass(TomEEContainerTest.class)
                .setWebXML(new StringAsset(
                		Descriptors.create(WebAppDescriptor.class)
                			.version("3.0")
                                .createServlet().servletName("servlet-ejb").servletClass(TestServlet.class.getName()).up()
                                .createServletMapping().servletName("servlet-ejb").urlPattern("/ejb").up()
                                .exportAsString()));
    }

    @EJB
    private TestEjb ejb;
    
    @Test 
    public void testEjbIsNotNull() throws Exception {
    	Assert.assertNotNull(ejb);
    }

    @Test
    public void testShouldBeAbleToAccessServletAndEjb() throws Exception {
        InputStream is = new URL("http://localhost:" + System.getProperty("tomee.httpPort", "10080") + "/test/ejb").openStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        String output = new String(os.toByteArray(), "UTF-8");
        Assert.assertTrue(output.contains("Hello, OpenEJB"));
    }

}
