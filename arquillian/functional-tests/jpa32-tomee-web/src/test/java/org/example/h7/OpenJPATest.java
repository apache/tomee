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
package org.example.h7;

import jakarta.persistence.EntityManagerFactory;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ArquillianExtension.class)
public class OpenJPATest {

    @Deployment
    public static WebArchive war() {


        return ShrinkWrap.create(WebArchive.class, "openjpa-app.war")
                .addAsWebInfResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence version=\"3.1\"\n" +
                        "    xmlns=\"https://jakarta.ee/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    xsi:schemaLocation=\"https://jakarta.ee/xml/ns/persistence" +
                        "                         https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd\">\n" +
                        "  <persistence-unit name=\"test\">\n" +
                        "    <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
                        "    <properties>\n" +
                        "      <property name=\"openejb.jpa.init-entitymanager\" value=\"true\" />\n" +
                        "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>"), ArchivePaths.create("persistence.xml"));
    }

    @Test // using an internal lookup because in tomee embedded new InitialContext() is not guaranteed
    public void checkEmIsOpenJPAOne() throws Exception {
        AppInfo info = null;
        for (final AppInfo app : SystemInstance.get().getComponent(Assembler.class).getDeployedApplications()) {
            if (app.appId.endsWith("openjpa-app")) {
                info = app;
                break;
            }
        }

        assertNotNull(info);
        final EntityManagerFactory emf = (EntityManagerFactory)
                SystemInstance.get().getComponent(ContainerSystem.class)
                        .getJNDIContext().lookup(Assembler.PERSISTENCE_UNIT_NAMING_CONTEXT + info.persistenceUnits.iterator().next().id);
        assertTrue(((ReloadableEntityManagerFactory) emf).getDelegate().getClass().getName().startsWith("org.apache."));
    }
}
