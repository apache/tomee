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

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies TomEE's Jakarta Data extension creates a working proxy for a @Repository
 * when NO Hibernate-generated implementation class exists.
 *
 * Uses {@link SimpleItemRepository} which the Hibernate annotation processor cannot
 * fully implement (method-name query convention without @Find), so no
 * SimpleItemRepository_ is generated. TomEE's extension handles it.
 */
@RunWith(Arquillian.class)
public class JakartaDataNoGeneratedImplTest {

    private static final String PERSISTENCE_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<persistence xmlns=\"https://jakarta.ee/xml/ns/persistence\" version=\"3.2\">\n" +
        "    <persistence-unit name=\"data-pu\" transaction-type=\"JTA\">\n" +
        "        <jta-data-source>java:comp/DefaultDataSource</jta-data-source>\n" +
        "        <class>org.apache.openejb.arquillian.tests.data.SimpleItem</class>\n" +
        "        <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
        "        <properties>\n" +
        "            <property name=\"jakarta.persistence.schema-generation.database.action\"\n" +
        "                      value=\"create\"/>\n" +
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
        return ShrinkWrap.create(WebArchive.class, "data-no-generated-impl.war")
            .addClass(SimpleItem.class)
            .addClass(SimpleItemRepository.class)
            .addAsResource(new StringAsset(PERSISTENCE_XML), "META-INF/persistence.xml")
            .addAsWebInfResource(new StringAsset(BEANS_XML), "beans.xml");
    }

    @Inject
    private SimpleItemRepository repository;

    @Resource
    private UserTransaction utx;

    @Test
    public void repositoryIsInjected() {
        assertNotNull("Repository should be injected via TomEE's Jakarta Data extension", repository);
    }

    @Test
    public void insertAndFindById() throws Exception {
        utx.begin();
        try {
            final SimpleItem item = new SimpleItem("alpha");
            repository.insert(item);
            assertNotNull("Item should have an ID after insert", item.getId());

            final Optional<SimpleItem> found = repository.findById(item.getId());
            assertTrue("Should find item by ID", found.isPresent());
            assertEquals("alpha", found.get().getLabel());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void findByLabel() throws Exception {
        utx.begin();
        try {
            repository.insert(new SimpleItem("beta"));
            repository.insert(new SimpleItem("gamma"));
            repository.insert(new SimpleItem("beta"));

            final List<SimpleItem> betas = repository.findByLabel("beta");
            assertEquals("Should find 2 items with label 'beta'", 2, betas.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void deleteById() throws Exception {
        utx.begin();
        try {
            final SimpleItem item = new SimpleItem("delta");
            repository.insert(item);
            final Long id = item.getId();
            assertNotNull(id);

            repository.deleteById(id);

            assertTrue("Deleted item should not be found", repository.findById(id).isEmpty());
        } finally {
            utx.rollback();
        }
    }
}
