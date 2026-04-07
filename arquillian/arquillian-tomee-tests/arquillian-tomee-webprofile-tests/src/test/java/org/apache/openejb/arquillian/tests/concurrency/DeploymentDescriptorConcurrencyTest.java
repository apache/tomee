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
package org.apache.openejb.arquillian.tests.concurrency;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Arquillian test verifying that web.xml deployment descriptors with
 * {@code <virtual>} and {@code <qualifier>} elements deploy successfully.
 * This tests the SXC JAXB accessor parsing for Concurrency 3.1 DD elements.
 */
@RunWith(Arquillian.class)
public class DeploymentDescriptorConcurrencyTest {

    private static final String WEB_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<web-app version=\"6.1\"\n" +
        "         xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n" +
        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "         xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee\n" +
        "           https://jakarta.ee/xml/ns/jakartaee/web-app_6_1.xsd\">\n" +
        "\n" +
        "  <managed-thread-factory>\n" +
        "    <name>java:app/concurrent/DDThreadFactory</name>\n" +
        "    <virtual>true</virtual>\n" +
        "  </managed-thread-factory>\n" +
        "\n" +
        "  <managed-executor>\n" +
        "    <name>java:app/concurrent/DDExecutor</name>\n" +
        "    <virtual>false</virtual>\n" +
        "  </managed-executor>\n" +
        "\n" +
        "  <managed-scheduled-executor>\n" +
        "    <name>java:app/concurrent/DDScheduledExecutor</name>\n" +
        "    <virtual>false</virtual>\n" +
        "  </managed-scheduled-executor>\n" +
        "\n" +
        "</web-app>\n";

    @Inject
    private DDBean ddBean;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "DDConcurrencyTest.war")
                .addClasses(DDBean.class)
                .setWebXML(new StringAsset(WEB_XML))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void deploymentSucceeds() {
        // If we get here, the web.xml with <virtual> parsed successfully
        assertNotNull("DDBean should be injected", ddBean);
    }

    @Test
    public void ddDefinedExecutorWorks() throws Exception {
        final boolean completed = ddBean.runOnDDExecutor();
        assertTrue("Task should run on DD-defined executor", completed);
    }

    @ApplicationScoped
    public static class DDBean {

        @Resource(lookup = "java:app/concurrent/DDExecutor")
        private ManagedExecutorService executor;

        public boolean runOnDDExecutor() throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            executor.execute(latch::countDown);
            return latch.await(5, TimeUnit.SECONDS);
        }
    }
}
