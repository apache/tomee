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

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies repository methods work when called from a CDI {@code @Transactional} method.
 *
 * The {@code @Transactional} interceptor makes every {@code UserTransaction} call throw
 * {@code IllegalStateException} for the duration of the method, so the repository has to join
 * the active transaction without going through {@code UserTransaction}.
 */
@RunWith(Arquillian.class)
public class JakartaDataTransactionalTest {

    private static final String PERSISTENCE_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <persistence xmlns="https://jakarta.ee/xml/ns/persistence" version="3.2">
            <persistence-unit name="data-pu" transaction-type="JTA">
                <jta-data-source>java:comp/DefaultDataSource</jta-data-source>
                <class>org.apache.openejb.arquillian.tests.data.SimpleItem</class>
                <exclude-unlisted-classes>true</exclude-unlisted-classes>
                <properties>
                    <property name="jakarta.persistence.schema-generation.database.action"
                              value="create"/>
                    <property name="openjpa.jdbc.SynchronizeMappings"
                              value="buildSchema(ForeignKeys=true)"/>
                </properties>
            </persistence-unit>
        </persistence>
        """;

    private static final String BEANS_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <beans xmlns="https://jakarta.ee/xml/ns/jakartaee" version="4.0"
               bean-discovery-mode="all">
        </beans>
        """;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "data-transactional.war")
            .addClass(SimpleItem.class)
            .addClass(SimpleItemRepository.class)
            .addClass(TransactionalItemService.class)
            .addAsResource(new StringAsset(PERSISTENCE_XML), "META-INF/persistence.xml")
            .addAsWebInfResource(new StringAsset(BEANS_XML), "beans.xml");
    }

    @Inject
    private TransactionalItemService service;

    @Inject
    private SimpleItemRepository repository;

    @Test
    public void insertAndFindInsideTransactional() {
        final Long id = service.insert("tx-insert");
        assertNotNull("Item should have an ID after insert", id);

        assertEquals("tx-insert", service.findLabel(id));
    }

    @Test
    public void saveManagedEntityInsideTransactional() {
        final Long id = service.insert("tx-before");

        service.rename(id, "tx-after");

        assertEquals("tx-after", repository.findById(id).orElseThrow().getLabel());
    }

    @Test
    public void deleteByIdInsideTransactional() {
        final Long id = service.insert("tx-delete");

        service.delete(id);

        assertTrue("Deleted item should not be found", repository.findById(id).isEmpty());
    }
}
