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
package org.apache.openejb.arquillian.tests.filterpersistence;

import org.apache.openejb.arquillian.tests.TestRun;
import org.apache.openejb.arquillian.tests.TestSetup;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ServletFilterPersistenceInjectionTest extends TestSetup {

    @Test
    public void transactionInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testUserTransaction=true";
        validateTest(expectedOutput);
    }

    @Test
    public void persistentContextInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testEntityManager=true";
        validateTest(expectedOutput);
    }

    @Test
    public void persistenceUnitInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "testEntityManagerFactory=true";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new ServletFilterPersistenceInjectionTest().createDeployment(TestRun.class, PersistenceServletFilter.class, Address.class);
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor.filter(PersistenceServletFilter.class, "/" + getTestContextName());
    }

    public void decorateArchive(WebArchive archive) {
        archive.addAsManifestResource("persistence.xml", ArchivePaths.create("persistence.xml"));
    }

}



