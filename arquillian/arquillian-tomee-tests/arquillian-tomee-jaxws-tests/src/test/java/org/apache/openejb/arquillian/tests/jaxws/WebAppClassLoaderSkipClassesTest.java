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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxws;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@RunWith(Arquillian.class)
@RunAsClient
public class WebAppClassLoaderSkipClassesTest {
    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive war() {
        final File[] libs = Maven
                .configureResolver()
                .workOffline()
                .resolve("commons-logging:commons-logging:1.2")
                .withTransitivity()
                .asFile();


        return ShrinkWrap.create(WebArchive.class, "WebAppClassLoaderTest.war")
                .addClasses(TestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsLibraries(libs);
    }

    @Test
    public void valid() throws IOException {
        final String output = IO.slurp(new URL(url.toExternalForm() + "test"));
        Assert.assertFalse(output.contains("WEB-INF")); // shouldn't be loaded from the webapp
    }
}
