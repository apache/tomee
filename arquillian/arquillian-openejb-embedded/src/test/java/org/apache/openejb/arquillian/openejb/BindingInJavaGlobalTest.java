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
package org.apache.openejb.arquillian.openejb;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class BindingInJavaGlobalTest {
    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence version=\"2.0\"\n" +
                        "             xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "             xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence\n" +
                        "                       http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\">\n" +
                        "  <persistence-unit name=\"person\">\n" +
                        "    <jta-data-source>java:global/db</jta-data-source>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>"), "persistence.xml");
    }

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Test
    public void checkSimpleBiding() throws NamingException {
        final BasicDataSource ds = (BasicDataSource) new InitialContext().lookup("java:global/db");
        assertEquals("jdbc:hsqldb:mem:global", ds.getUrl());
    }

    @Test
    public void checkJpaBiding() throws NamingException {
        assertEquals("jdbc:hsqldb:mem:global", ((BasicDataSource) ((ReloadableEntityManagerFactory) emf).info().getJtaDataSource()).getUrl());
    }
}
