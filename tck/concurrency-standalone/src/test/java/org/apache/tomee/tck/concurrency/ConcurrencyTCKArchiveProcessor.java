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
package org.apache.tomee.tck.concurrency;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Arquillian ApplicationArchiveProcessor that adds a beans.xml with
 * {@code bean-discovery-mode="all"} to Concurrency TCK deployments.
 *
 * <p>This is needed because OWB (OpenWebBeans) in TomEE does not yet
 * auto-enable {@code @Priority} interceptors without an explicit beans.xml.
 * The {@code AsynchronousInterceptor} relies on this to be activated
 * in deployed WARs.</p>
 */
public class ConcurrencyTCKArchiveProcessor implements ApplicationArchiveProcessor {

    private static final String BEANS_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n" +
        "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "       xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee\n" +
        "         https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd\"\n" +
        "       version=\"4.0\"\n" +
        "       bean-discovery-mode=\"all\">\n" +
        "</beans>\n";

    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {
            final WebArchive war = (WebArchive) archive;

            if (!archive.contains("WEB-INF/beans.xml")) {
                war.addAsWebInfResource(new StringAsset(BEANS_XML), "beans.xml");
            }
        }
    }
}
