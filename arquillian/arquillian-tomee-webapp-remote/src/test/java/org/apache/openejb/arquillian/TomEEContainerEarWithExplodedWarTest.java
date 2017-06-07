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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.assembler.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.application7.ApplicationDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class TomEEContainerEarWithExplodedWarTest {
    private static final String TARGET_FOLDER = "target";
    private static final String EAR_FILE = "test-ear.ear";
    private static final String WAR_FILE = "test-web.war";
    private static final String CONTEXT_PATH = "hello";
    private static final String SERVLET_NAME = "servlet-ejb";
    private static final String URL_PATTERN = "/ejb";
    private static final String EXPECTED_CONTENT = "Hello";
    private static final String INITIAL_CONTEXT_FACTORY_VALUE = "org.apache.openejb.client.RemoteInitialContextFactory";
    private static final String DEPLOYER_LOOKUP_NAME = "openejb/DeployerBusinessRemote";

    @Deployment
    public static WebArchive createDeployment() throws Exception {
        final WebArchive web = ShrinkWrap.create(WebArchive.class, WAR_FILE)
                .addClass(TestServlet.class)
                .addClass(TestEjb.class)
                .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
                        .createServlet().servletName(SERVLET_NAME).servletClass(TestServlet.class.getName()).up()
                        .createServletMapping().servletName(SERVLET_NAME).urlPattern(URL_PATTERN).up()
                        .exportAsString()));
        ShrinkWrap.create(EnterpriseArchive.class, EAR_FILE).addAsModule(web)
                .setApplicationXML(new StringAsset(Descriptors.create(ApplicationDescriptor.class)
                        .createModule().getOrCreateWeb().contextRoot(CONTEXT_PATH).webUri(WAR_FILE).up().up()
                        .exportAsString()))
                .as(ExplodedExporter.class).exportExploded(new File(TARGET_FOLDER));
        return ShrinkWrap.create(WebArchive.class);
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testShouldBeAbleToAccessWebApp() throws Exception {
        final Deployer deployer = lookupDeployer();
        final File earFolder = new File(TARGET_FOLDER, EAR_FILE);
        deployer.deploy(earFolder.getAbsolutePath());
        Assert.assertTrue(IO.slurp(getUrl()).contains(EXPECTED_CONTENT));
        Files.deleteOnExit(earFolder);
    }

    private Deployer lookupDeployer() {
        Deployer deployer = null;
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY_VALUE);
        try {
            deployer = (Deployer) new InitialContext(properties).lookup(DEPLOYER_LOOKUP_NAME);
        } catch (NamingException e) {
            Assert.fail("Cannot lookup deployer " + e.getMessage());
        }
        return deployer;
    }

    private URL getUrl() throws MalformedURLException {
        return new URL(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/"
                + CONTEXT_PATH + URL_PATTERN);
    }
}
