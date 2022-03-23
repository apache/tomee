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
package org.apache.openejb.arquillian.tests.hibernate;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.persistence.EntityManagerFactory;
import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore
@RunWith(Arquillian.class)
public class HibernateTest {

    public static void main(String[] args) {
        final File[] files = Maven.resolver()
                .loadPomFromFile("src/test/resources/hibernate-pom.xml")
                .importCompileAndRuntimeDependencies().resolve().withTransitivity()
                .asFile();
    }

    @Deployment
    public static WebArchive war() {
        File[] hibernate;
        try { // try offline first since it is generally faster
            hibernate = Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile("src/test/resources/hibernate-pom.xml")
                    .importCompileAndRuntimeDependencies().resolve().withTransitivity()
                    .asFile();
        } catch (ResolutionException re) { // try on central
            hibernate = Maven.resolver()
                    .loadPomFromFile("src/test/resources/hibernate-pom.xml")
                    .importCompileAndRuntimeDependencies().resolve().withTransitivity()
                    .asFile();
        }

        return ShrinkWrap.create(WebArchive.class, "hibernate-app.war")
                .addAsWebInfResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence version=\"2.0\"\n" +
                        "    xmlns=\"http://java.sun.com/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence" +
                        "                         http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\">\n" +
                        "  <persistence-unit name=\"hibernate\">\n" +
                        "    <provider>org.hibernate.jpa.HibernatePersistence</provider>\n" +
                        "    <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
                        "    <properties>\n" +
                        "      <property name=\"hibernate.hbm2ddl.auto\" value=\"create-drop\" />\n" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>"), ArchivePaths.create("persistence.xml"))
                .addAsLibraries(hibernate)
                .addAsLibraries(JarLocation.jarLocation(ResolutionException.class))
                .addAsLibraries(JarLocation.jarLocation(org.jboss.shrinkwrap.resolver.api.maven.filter.MavenResolutionFilter.class));
    }

    @Test // using an internal lookup because in tomee embedded new InitialContext() is not guaranteed
    public void checkEmIsHibernateOne() throws Exception {
        AppInfo info = null;
        for (final AppInfo app : SystemInstance.get().getComponent(Assembler.class).getDeployedApplications()) {
            if (app.appId.endsWith("hibernate-app")) {
                info = app;
                break;
            }
        }

        assertNotNull(info);
        final EntityManagerFactory emf = (EntityManagerFactory)
                SystemInstance.get().getComponent(ContainerSystem.class)
                        .getJNDIContext().lookup(Assembler.PERSISTENCE_UNIT_NAMING_CONTEXT + info.persistenceUnits.iterator().next().id);
        assertTrue(((ReloadableEntityManagerFactory) emf).getDelegate().getClass().getName().startsWith("org.hibernate."));
    }
}
