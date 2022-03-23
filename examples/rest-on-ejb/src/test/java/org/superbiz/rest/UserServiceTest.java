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
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class UserServiceTest {

    private static Context context;
    private static UserService service;
    private static List<User> users = new ArrayList<>();

    @BeforeClass
    public static void start() throws NamingException {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        context = EJBContainer.createEJBContainer(properties).getContext();

        // create some records
        service = (UserService) context.lookup("java:global/rest-on-ejb/UserService");
        users.add(service.create("foo", "foopwd", "foo@foo.com"));
        users.add(service.create("bar", "barpwd", "bar@bar.com"));
    }

    @AfterClass
    public static void close() throws NamingException {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void create() {
        WebClient.create("http://localhost:4204/rest-on-ejb")
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

        WebClient.create("http://localhost:4204/rest-on-ejb").path("/user/delete/" + user.getId()).delete();

        user = service.find(user.getId());
        assertNull(user);
    }

    @Test
    public void show() {
        User user = WebClient.create("http://localhost:4204/rest-on-ejb")
                .path("/user/show/" + users.iterator().next().getId())
                .get(User.class);
        assertEquals("foo", user.getFullname());
        assertEquals("foopwd", user.getPassword());
        assertEquals("foo@foo.com", user.getEmail());
    }

    @Test
    public void list() throws Exception {
        String users = WebClient.create("http://localhost:4204/rest-on-ejb")
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
        Response response = WebClient.create("http://localhost:4204/rest-on-ejb")
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
