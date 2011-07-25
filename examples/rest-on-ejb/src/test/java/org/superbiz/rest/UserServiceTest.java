/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
import sun.awt.geom.AreaOp;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

/**
 * In standalone only GET and POST are managed.
 *
 * @author Romain Manni-Bucau
 */
public class UserServiceTest {
    private static Context context;

    @BeforeClass public static void start() throws NamingException {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        context = EJBContainer.createEJBContainer(properties).getContext();

        // create some records
        UserService service = (UserService) context.lookup("java:global/rest-on-ejb/UserService");
        service.create("foo", "foopwd", "foo@foo.com");
        service.create("bar", "barpwd", "bar@bar.com");
    }

    @AfterClass public static void close() throws NamingException {
        if (context != null) {
            context.close();
        }
    }

    @Test public void show() {
        User user = WebClient.create("http://localhost:4204")
                .path("/user/show/1")
                .get(User.class);
        assertEquals("foo", user.getFullname());
        assertEquals("foopwd", user.getPassword());
        assertEquals("foo@foo.com", user.getEmail());
    }

    @Test public void list() throws Exception {
        String users = WebClient.create("http://localhost:4204")
                .path("/user/list")
                .get(String.class);
        assertEquals(
            "<users>" +
                "<user>" +
                    "<email>foo@foo.com</email>" +
                    "<fullname>foo</fullname>" +
                    "<id>1</id>" +
                    "<password>foopwd</password>" +
                "</user>" +
                "<user>" +
                    "<email>bar@bar.com</email>" +
                    "<fullname>bar</fullname>" +
                    "<id>2</id>" +
                    "<password>barpwd</password>" +
                "</user>" +
            "</users>", users);
    }
}
