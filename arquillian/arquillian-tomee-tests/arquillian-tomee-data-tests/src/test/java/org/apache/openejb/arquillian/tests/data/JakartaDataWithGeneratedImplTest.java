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
package org.apache.openejb.arquillian.tests.data;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Verifies TomEE's Jakarta Data extension skips a @Repository when a
 * Hibernate-generated implementation class (DataItemRepository_) is present
 * in the deployment.
 *
 * The Hibernate annotation processor generates DataItemRepository_ at compile
 * time in this module. This test includes it in the WAR deployment so that
 * the CDI extension detects it and does NOT register its own proxy bean.
 *
 * Since the actual Hibernate runtime isn't available in embedded mode with
 * OpenJPA, the generated impl can't function. The test verifies the extension's
 * detection logic — that it correctly backs off when a generated impl exists.
 */
@RunWith(Arquillian.class)
public class JakartaDataWithGeneratedImplTest {

    private static final String PERSISTENCE_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<persistence xmlns=\"https://jakarta.ee/xml/ns/persistence\" version=\"3.2\">\n" +
        "    <persistence-unit name=\"data-pu\" transaction-type=\"JTA\">\n" +
        "        <jta-data-source>java:comp/DefaultDataSource</jta-data-source>\n" +
        "        <class>org.apache.openejb.arquillian.tests.data.DataItem</class>\n" +
        "        <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
        "        <properties>\n" +
        "            <property name=\"openjpa.jdbc.SynchronizeMappings\"\n" +
        "                      value=\"buildSchema(ForeignKeys=true)\"/>\n" +
        "        </properties>\n" +
        "    </persistence-unit>\n" +
        "</persistence>\n";

    private static final String BEANS_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"4.0\"\n" +
        "       bean-discovery-mode=\"all\">\n" +
        "</beans>\n";

    @Deployment
    public static WebArchive createDeployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "data-with-generated-impl.war")
            .addClass(DataItem.class)
            .addClass(DataItemRepository.class)
            .addAsResource(new StringAsset(PERSISTENCE_XML), "META-INF/persistence.xml")
            .addAsWebInfResource(new StringAsset(BEANS_XML), "beans.xml");

        // Include the Hibernate-generated implementation and JPA metamodel.
        // This causes TomEE's extension to detect it and skip proxy creation.
        try {
            war.addClass(Class.forName("org.apache.openejb.arquillian.tests.data.DataItemRepository_"));
            war.addClass(Class.forName("org.apache.openejb.arquillian.tests.data.DataItem_"));
        } catch (final ClassNotFoundException e) {
            throw new AssertionError(
                "Hibernate annotation processor did not generate DataItemRepository_. " +
                "Check that hibernate-processor is configured in annotationProcessorPaths.", e);
        }

        return war;
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void extensionSkipsRepositoryWithGeneratedImpl() {
        assertNotNull(beanManager);

        // TomEE's extension should have detected DataItemRepository_ and skipped registration.
        // The Hibernate-generated DataItemRepository_ is a @Dependent CDI bean itself, so it
        // may still be resolvable. The key is that our extension did NOT create a proxy.
        // We verify by checking that the bean (if resolvable) is the Hibernate-generated
        // class, not our proxy.
        final var beans = beanManager.getBeans(DataItemRepository.class);
        for (final var bean : beans) {
            assertFalse(
                "TomEE's extension should not have registered a proxy when Hibernate-generated impl is present",
                bean.getBeanClass().getName().contains("openejb.data.extension.RepositoryBean"));
        }
    }
}
