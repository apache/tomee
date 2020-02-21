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

import org.apache.openejb.arquillian.common.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptScopesStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

@RunWith(Arquillian.class) // TODO: move it and EarWebAppFirstClassLoaderTest to webprofile?
public class EarClassLoaderTest {
    @Deployment(testable = false)
    public static Archive<?> ear() {
        return ShrinkWrap.create(EnterpriseArchive.class, "broken.ear")
                .addAsModule(
                        ShrinkWrap.create(WebArchive.class, "broken-web.war")
                                .addClasses(LoadJodaFromTheWebAppResource.class)
                                .addAsLibraries(
                                        Maven.configureResolver()
                                                .workOffline()
                                                .withClassPathResolution(true)
                                                .resolve("joda-time:joda-time:2.5")
                                                .using(new AcceptScopesStrategy(ScopeType.COMPILE, ScopeType.RUNTIME))
                                                .asFile()
                                )
                );
    }

    @ArquillianResource
    private URL url;

    @Test
    public void checkIfWasCorretlyLoaded() throws IOException {
        // embedded case uses the classpath for a lot of reasons
        assumeFalse(System.getProperty("openejb.arquillian.adapter", "embedded").contains("embedded"));
        final String slurp = IO.slurp(new URL(url.toExternalForm() + (url.getPath().isEmpty() ? "/broken-web/" : "") + "joda"));
        assertTrue(slurp.endsWith("broken-web/WEB-INF/lib/joda-time-2.5.jar"));
    }
}
