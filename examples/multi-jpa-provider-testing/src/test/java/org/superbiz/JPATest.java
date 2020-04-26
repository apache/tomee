/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.model.Person;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class JPATest {

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(Person.class)
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml"), ArchivePaths.create("persistence.xml"));
    }

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void persist() {
        assertNotNull(em);

        // do something with the em
        final Person p = new Person();
        p.setName("Apache OpenEJB");
        em.persist(p);
    }
}
