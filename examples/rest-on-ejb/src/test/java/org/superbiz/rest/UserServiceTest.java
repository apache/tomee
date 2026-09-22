/*
 *      Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.superbiz.rest;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class UserServiceTest {

    private static List<User> users = new ArrayList<>();

    @ArquillianResource
    private URL base;

    @EJB
    private UserService service;

    @Deployment
    public static WebArchive app() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(User.class, UserService.class)
                .addAsResource(new ClassLoaderAsset("META-INF/persistence.xml"), "META-INF/persistence.xml");
    }

    @Before
    public void createSomeRecords() {
        if (users.isEmpty()) { // the test runs inside TomEE, the records are created once for all tests
            users.add(service.create("foo", "foopwd", "foo@foo.com"));
            users.add(service.create("bar", "barpwd", "bar@bar.com"));
        }
    }

    @Test
    public void create() {
        WebClient.create(base.toExternalForm())
                .path("/user/create")
                .query("name", "dummy")
                .query("pwd", "unbreakable")
                .query("mail", "foo@bar.fr")
                .put("{}");
        List<User> list = service.list(0, 100);
        for (User u : list) {
            if (!users.contains(u)) {
                service.delete(u.getId());
                return;
            }
        }
        fail("user was not added");
    }

    @Test
    public void delete() throws Exception {
        User user = service.create("todelete", "dontforget", "delete@me.com");

        WebClient.create(base.toExternalForm()).path("/user/delete/" + user.getId()).delete();

        user = service.find(user.getId());
        assertNull(user);
    }

    @Test
    public void show() {
        User user = WebClient.create(base.toExternalForm())
                .path("/user/show/" + users.iterator().next().getId())
                .get(User.class);
        assertEquals("foo", user.getFullname());
        assertEquals("foopwd", user.getPassword());
        assertEquals("foo@foo.com", user.getEmail());
    }

    @Test
    public void list() throws Exception {
        String users = WebClient.create(base.toExternalForm())
                .path("/user/list")
                .get(String.class);
        assertEquals(users,
                inline("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<users>" +
                        "  <user>" +
                        "    <email>bar@bar.com</email>" +
                        "    <fullname>bar</fullname>" +
                        "    <id>2</id>" +
                        "    <password>barpwd</password>" +
                        "  </user>" +
                        "  <user>" +
                        "    <email>foo@foo.com</email>" +
                        "    <fullname>foo</fullname>" +
                        "    <id>1</id>" +
                        "    <password>foopwd</password>" +
                        "  </user>" +
                        "</users>"), inline(users)
        );
    }

    private static String inline(String s) {
        return s.replace(System.getProperty("line.separator"), "").replace("\n", "")
                .replace(" ", "").replace("\t", "");
    }

    @Test
    public void update() throws Exception {
        User created = service.create("name", "pwd", "mail");
        Response response = WebClient.create(base.toExternalForm())
                .path("/user/update/" + created.getId())
                .query("name", "corrected")
                .query("pwd", "userpwd")
                .query("mail", "it@is.ok")
                .post(null);

        JAXBContext ctx = JAXBContext.newInstance(User.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        User modified = (User) unmarshaller.unmarshal(InputStream.class.cast(response.getEntity()));

        assertEquals("corrected", modified.getFullname());
        assertEquals("userpwd", modified.getPassword());
        assertEquals("it@is.ok", modified.getEmail());
        service.delete(created.getId());
    }
}
