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
package org.apache.openejb.arquillian;

import org.apache.catalina.realm.MemoryRealm;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.arquillian.managed.ConcurrencyServlet;
import org.apache.openejb.arquillian.managed.User;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ManagedExecutorServiceGetPrincipalInTaskTest {
    @Deployment(testable = false)
    public static Archive<?> app() {
        ClassLoader cl = ManagedExecutorServiceGetPrincipalInTaskTest.class.getClassLoader();
        URL tcUserFile = cl.getResource("managed/tomcat-users.xml");

        return ShrinkWrap.create(WebArchive.class, "mp.war")
            .addClasses(ConcurrencyServlet.class, User.class)
            .addAsManifestResource(new StringAsset(
                "<Context>" +
                "   <Realm className=\"" + MemoryRealm.class.getName() +
                    "\" pathname=\""+ tcUserFile.getFile() + "\" />" +
                "</Context>"), "context.xml")
                .addAsWebInfResource(new UrlAsset(cl.getResource("managed/web.xml")), "web.xml");
    }

    @ArquillianResource
    private URL url;

    @Test
    public void run() throws IOException {
        assertEquals("test", IO.slurp(new URL(url, "async")).trim());
    }
}
