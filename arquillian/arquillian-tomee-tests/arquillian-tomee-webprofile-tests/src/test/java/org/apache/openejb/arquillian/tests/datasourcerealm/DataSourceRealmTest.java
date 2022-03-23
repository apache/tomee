/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.datasourcerealm;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static jakarta.xml.bind.DatatypeConverter.printBase64Binary;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class DataSourceRealmTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, DataSourceRealmTest.class.getName() + ".war")
                .addClasses(AddUser.class, User.class, Role.class, RoleId.class)
                .addAsWebInfResource(new StringAsset( // JPA for user/role provisioning and table init
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                                "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "             xsi:schemaLocation=\"\n" +
                                "              http://java.sun.com/xml/ns/persistence\n" +
                                "              http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"\n" +
                                "             version=\"2.0\">\n" +
                                "  <persistence-unit name=\"users\">\n" +
                                "    <jta-data-source>jdbc/users-managed</jta-data-source>\n" +
                                "    <non-jta-data-source>jdbc/users</non-jta-data-source>\n" +
                                "    <class>org.apache.openejb.arquillian.tests.datasourcerealm.User</class>\n" +
                                "    <class>org.apache.openejb.arquillian.tests.datasourcerealm.Role</class>\n" +
                                "    <class>org.apache.openejb.arquillian.tests.datasourcerealm.RoleId</class>\n" +
                                "    <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
                                "    <properties>\n" +
                                "      <property name=\"openejb.jpa.init-entitymanager\" value=\"true\" />\n" +
                                "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>\n" +
                                "      <property name=\"openjpa.RuntimeUnenhancedClasses\" value=\"supported\"/>\n" +
                                "      <property name=\"eclipselink.ddl-generation\" value=\"drop-and-create-tables\"/>\n" +
                                "    </properties>\n" +
                                "  </persistence-unit>\n" +
                                "</persistence>"), "persistence.xml")
                .addAsManifestResource(new StringAsset(
                        "<Context>\n" +
                                "  <Realm className=\"org.apache.catalina.realm.DataSourceRealm\" \n" +
                                "       dataSourceName=\"jdbc/users\" localDataSource=\"true\"\n" +
                                "       userTable=\"users\" userNameCol=\"user_name\" userCredCol=\"user_pass\"\n" +
                                "       userRoleTable=\"user_roles\" roleNameCol=\"user_role\">\n" +
                                "\n" +
                                "    <CredentialHandler className=\"org.apache.catalina.realm.MessageDigestCredentialHandler\" algorithm=\"md5\" />\n" +
                                "  </Realm>\n" +
                                "</Context>"), "context.xml")
                .addAsWebInfResource(new StringAsset(
                        "<Resources>\n" +
                                "  <Resource id=\"jdbc/users-managed\" type=\"DataSource\">\n" +
                                "  JtaManaged = true\n" +
                                "  JdbcUrl = jdbc:hsqldb:mem:DataSourceRealmTest_users\n" +
                                "  LogSql = true\n" +
                                "  </Resource>\n" +
                                "  <Resource id=\"jdbc/users\" type=\"DataSource\">\n" +
                                "  JtaManaged = false\n" +
                                "  JdbcUrl = jdbc:hsqldb:mem:DataSourceRealmTest_users\n" +
                                "  LogSql = true\n" +
                                "  </Resource>\n" +
                                "</Resources>"), "resources.xml")
                .addAsWebResource(new StringAsset("touched"), "index.html")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .setWebXML(new StringAsset(
                        Descriptors.create(WebAppDescriptor.class)
                                .getOrCreateSecurityConstraint()
                                .createWebResourceCollection()
                                .webResourceName("all")
                                .urlPattern("/*")
                                .up()
                                .getOrCreateAuthConstraint()
                                .roleName("arquillian")
                                .up()
                                .up()
                                .getOrCreateLoginConfig()
                                .authMethod("BASIC")
                                .up()
                                .exportAsString()));
    }

    @ArquillianResource
    private URL base;

    @Test(expected = IOException.class)
    public void forbidden() throws IOException {
        IO.slurp(base);
    }

    @Test
    public void allowed() throws IOException, URISyntaxException {
        assertEquals("touched", ClientBuilder.newClient()
                .target(base.toURI()).request(MediaType.TEXT_PLAIN)
                .header("Authorization", "Basic " + printBase64Binary("test:pwd".getBytes("UTF-8")))
                .get(String.class));
    }
}
