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
package org.apache.openejb.arquillian.tests.realm;

import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CdiLazyRealmTOMEE1490Test {
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "example.war")
                .addClasses(SimpleEndpoint.class, MyCdiLazyRealm.class)
                .addAsManifestResource(new StringAsset("<Context preemptiveAuthentication=\"true\">\n" +
                        "  <Valve className=\"" + BasicAuthenticator.class.getName() + "\" />\n" +
                        "  <Realm cdi=\"true\"\n" +
                        "         className=\"org.apache.tomee.catalina.realm.LazyRealm\"\n" +
                        "         realmClass=\"" + MyCdiLazyRealm.class.getName() + "\" />\n" +
                        "</Context>"), "context.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL webapp;

    @Test
    public void success() throws IOException {
        assertEquals("ok", WebClient.create(webapp.toExternalForm() + "/simple", "user", "pwd", null).get(String.class));
    }

    @Test(expected = Exception.class)
    public void failure() throws IOException {
        assertEquals("ok", WebClient.create(webapp.toExternalForm() + "/simple", "user", "wrong", null).get(String.class));
    }
}
