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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.net.MalformedURLException;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class RemoteEjbTest {
    @Test
    public void run() throws NamingException, MalformedURLException {
        final int http = NetworkUtil.getNextAvailablePort();
        try (final Container container = new Container(
                new Configuration()
                    .withEjbRemote(true)
                    .http(http)
                    .property("openejb.container.additional.exclude", "org.apache.tomee.embedded.")
                    .property("openejb.additional.include", "tomee-"))
                .deployPathsAsWebapp("app", singletonList(JarLocation.jarLocation(RemoteEjbTest.class).toURI().toURL()), new File("target/foo"))) {
            assertEquals(
                "pong",
                MyRemote.class.cast(new RemoteInitialContextFactory()
                    .getInitialContext(new PropertiesBuilder().p(Context.PROVIDER_URL, "http://localhost:" + http + "/tomee/ejb").build())
                    .lookup("global/app/MyRemoteBean!org.apache.tomee.embedded.RemoteEjbTest$MyRemote")).ping());
        }
    }

    @Stateless
    public static class MyRemoteBean implements MyRemote {
        @Override
        public String ping() {
            return "pong";
        }
    }

    @Remote
    public interface MyRemote {
        String ping();
    }
}
