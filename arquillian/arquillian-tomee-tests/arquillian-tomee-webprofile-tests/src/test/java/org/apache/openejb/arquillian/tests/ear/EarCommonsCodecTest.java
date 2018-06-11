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
package org.apache.openejb.arquillian.tests.ear;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class EarCommonsCodecTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        final File[] codecLibs = Maven.resolver().resolve("commons-codec:commons-codec:1.11").withTransitivity().asFile();

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "beans.jar");
        ejbJar.addClass(CodecBean.class);

        final WebArchive webapp = ShrinkWrap.create(WebArchive.class, "servlet.war").addClass(CodecServlet.class);

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "codec.ear").addAsModule(ejbJar).addAsModule(webapp);
        ear.addAsLibraries(codecLibs);

        return ear;
    }

    @Test
    public void test() throws Exception {
        final URL servlet = new URL(url, "/codec/servlet/codec");
        final String slurp = IO.slurp(servlet);

        System.out.println(slurp);
    }


}
