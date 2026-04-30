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
package org.apache.openejb.arquillian.tests.jsf.cset;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.facesconfig22.WebFacesConfigDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertTrue;

// Test that reproduces https://issues.apache.org/jira/browse/TOMEE-4603
// Rendering a Facelet that combines a JSTL c:set in request scope with an EL
// reference to the implicit param map used to trigger a duplicate class
// definition in the OpenWebBeans NormalScopeProxyFactory for java.util.Map.
@RunWith(Arquillian.class)
public class CsetRequestScopeTest {

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebFacesConfigDescriptor facesConfig = Descriptors.create(WebFacesConfigDescriptor.class);

        final WebAppDescriptor webXml = Descriptors.create(WebAppDescriptor.class)
                .version(WebAppVersionType._3_0)
                .createServlet()
                    .servletName("Faces Servlet")
                    .servletClass("jakarta.faces.webapp.FacesServlet")
                    .loadOnStartup(1)
                .up()
                .createServletMapping()
                    .servletName("Faces Servlet")
                    .urlPattern("*.xhtml")
                .up();

        return ShrinkWrap.create(WebArchive.class, "csetrequestscope.war")
                .setWebXML(new StringAsset(webXml.exportAsString()))
                .addAsWebInfResource(new StringAsset(facesConfig.exportAsString()), "faces-config.xml")
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\" version=\"4.0\"/>"), "beans.xml")
                .addAsWebResource("org/apache/openejb/arquillian/tests/jsf/cset/csetrequestscope1.xhtml",
                        "csetrequestscope1.xhtml");
    }

    @Test
    public void csetInRequestScopeIsResolvable() throws Exception {
        final String result = IO.slurp(new URL(base, "csetrequestscope1.xhtml"));
        assertTrue("expected c:set value 'someValue' in rendered output but got: " + result,
                result.contains("someValue"));
    }
}
