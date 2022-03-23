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
package org.apache.openejb.arquillian.unpack;

import org.junit.Assert;
import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.loader.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import java.io.File;

@RunWith(Arquillian.class)
public class UnpackTest extends Assert {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addAsLibraries(JarLocation.jarLocation(Test.class))
                .addClass(TestServlet.class)
                .addClass(TestEjb.class)
                .addClass(UnpackTest.class)
                .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class).version("3.0")
                        .createServlet().servletName("servlet").servletClass(TestServlet.class.getName()).up()
                        .createServletMapping().servletName("servlet").urlPattern("/ejb").up()
                        .exportAsString()));
    }

    @EJB
    private TestEjb ejb;

    // This test isn't well implemented because we don't copy
    // the war in packed.  Just need to do that and then get this
    // test to pass legitimately.
    @Test
    public void testNotUnpacked() throws Exception {

        final String property = System.getProperty("catalina.base");
        assertNotNull(property);

        final File home = new File(property);
        final File webapps = new File(home, "webapps");
        final File unpacktest = new File(webapps, "unpacktest");

        // Catalina home should exist
        assertTrue(home.exists());

        // webapps should exist
        assertTrue(webapps.exists());

        // exploded test dir should NOT exist
        assertFalse(unpacktest.exists());
    }

}
