/*
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
package org.apache.openejb.arquillian.tests.jaxrs.ear;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.application6.ApplicationDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class EarTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        final WebArchive servletWar = ShrinkWrap.create(WebArchive.class, "servlet.war").addClass(HelloServlet.class);
        final WebArchive jaxRSWar = ShrinkWrap.create(WebArchive.class, "rest.war").addClasses(HelloApplication.class, HelloEndpoint.class);

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(servletWar)
                .addAsModule(jaxRSWar);


        final ApplicationDescriptor descriptor = Descriptors.create(ApplicationDescriptor.class);
        descriptor.id("TestApplication").description("My Test Application")
                .createModule().getOrCreateWeb().webUri("servlet.war").contextRoot("TestApplication").up().up()
                .createModule().getOrCreateWeb().webUri("rest.war").contextRoot("TestApplication-REST").up().up();

        ear.setApplicationXML(new StringAsset(descriptor.exportAsString()));

        return ear;
    }

    @Test
    public void test() throws Exception {
        Assert.assertEquals("Hello rest!", IO.slurp(new URL(url, "/TestApplication-REST/api/")));
        Assert.assertEquals("Hello servlets!", IO.slurp(new URL(url, "/TestApplication/")));
    }
}
