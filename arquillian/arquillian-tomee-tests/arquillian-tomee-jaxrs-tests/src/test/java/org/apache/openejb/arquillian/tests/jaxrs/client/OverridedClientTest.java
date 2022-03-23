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
package org.apache.openejb.arquillian.tests.jaxrs.client;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.ClientBuilder;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class OverridedClientTest {
    @Deployment
    public static Archive<?> war() {
        final MavenResolverSystem resolver = Maven.resolver();
        final String jerseyVersion = "2.17";
        final String hkApi = "2.4.0-b16";
        return ShrinkWrap.create(WebArchive.class, "OverridedClientTest.war")
                .addAsServiceProvider(ClientBuilder.class.getName(), "org.glassfish.jersey.client.JerseyClientBuilder") // missing in jersey
                .addAsLibraries(resolver.resolve("org.glassfish.jersey.core:jersey-common:" + jerseyVersion).withoutTransitivity().asFile())
                .addAsLibraries(resolver.resolve("org.glassfish.jersey.core:jersey-client:" + jerseyVersion).withoutTransitivity().asFile())
                .addAsLibraries(resolver.resolve("org.glassfish.hk2:hk2-api:" + hkApi).withoutTransitivity().asFile())
                .addAsLibraries(resolver.resolve("org.glassfish.hk2:hk2-core:" + hkApi).withoutTransitivity().asFile())
                .addAsLibraries(resolver.resolve("org.glassfish.jersey.bundles.repackaged:jersey-guava:" + jerseyVersion).withoutTransitivity().asFile());
    }

    @Test
    public void run() {
        assertEquals("org.glassfish.jersey.client.JerseyClientBuilder", ClientBuilder.newBuilder().getClass().getName());
    }
}
