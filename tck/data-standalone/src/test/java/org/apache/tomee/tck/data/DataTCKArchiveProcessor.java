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
package org.apache.tomee.tck.data;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Arquillian ApplicationArchiveProcessor that adds a persistence.xml to
 * Jakarta Data TCK deployments. The TCK entities are JPA @Entity annotated,
 * but the TCK does not provide a persistence.xml since that is the
 * responsibility of the Jakarta Data implementation/runtime.
 *
 * This processor adds a persistence unit with auto-discovery (exclude-unlisted-classes=false)
 * so that all JPA entities in the deployment are automatically found.
 */
public class DataTCKArchiveProcessor implements ApplicationArchiveProcessor {

    private static final String PERSISTENCE_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<persistence xmlns=\"https://jakarta.ee/xml/ns/persistence\"\n" +
        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "             xsi:schemaLocation=\"https://jakarta.ee/xml/ns/persistence\n" +
        "               https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd\"\n" +
        "             version=\"3.2\">\n" +
        "    <persistence-unit name=\"tck-pu\" transaction-type=\"JTA\">\n" +
        "        <jta-data-source>java:comp/DefaultDataSource</jta-data-source>\n" +
        "        <exclude-unlisted-classes>false</exclude-unlisted-classes>\n" +
        "        <properties>\n" +
        "            <!-- Standard JPA schema generation (works with all providers) -->\n" +
        "            <property name=\"jakarta.persistence.schema-generation.database.action\"\n" +
        "                      value=\"drop-and-create\"/>\n" +
        "            <!-- OpenJPA-specific schema generation -->\n" +
        "            <property name=\"openjpa.jdbc.SynchronizeMappings\"\n" +
        "                      value=\"buildSchema(ForeignKeys=true)\"/>\n" +
        "            <property name=\"openjpa.Log\" value=\"DefaultLevel=WARN\"/>\n" +
        "            <!-- EclipseLink-specific schema generation -->\n" +
        "            <property name=\"eclipselink.ddl-generation\"\n" +
        "                      value=\"drop-and-create-tables\"/>\n" +
        "            <property name=\"eclipselink.ddl-generation.output-mode\"\n" +
        "                      value=\"database\"/>\n" +
        "            <property name=\"eclipselink.logging.level\" value=\"WARNING\"/>\n" +
        "        </properties>\n" +
        "    </persistence-unit>\n" +
        "</persistence>\n";

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

            // Add persistence.xml if not already present
            if (!archive.contains("WEB-INF/classes/META-INF/persistence.xml")) {
                war.addAsResource(new StringAsset(PERSISTENCE_XML), "META-INF/persistence.xml");
            }

            // Add beans.xml with bean-discovery-mode="all" so that @Repository
            // interfaces are scanned and ProcessAnnotatedType fires for them
            if (!archive.contains("WEB-INF/beans.xml")) {
                war.addAsWebInfResource(new StringAsset(BEANS_XML), "beans.xml");
            }
        }
    }
}
