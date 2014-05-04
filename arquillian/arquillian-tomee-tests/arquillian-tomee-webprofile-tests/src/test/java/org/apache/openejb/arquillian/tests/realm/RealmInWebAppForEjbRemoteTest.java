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

import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.catalina.realm.LazyRealm;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class RealmInWebAppForEjbRemoteTest
{
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "realm-test.war")
                .addClasses(HardCodedRealm.class, Simple.class, SimpleEJB.class)
                .addAsManifestResource(new StringAsset("<Context antiJARLocking=\"true\">\n" +
                        "<Realm className=\"" + LazyRealm.class.getName() + "\"" +
                        "       realmClass=\"" + HardCodedRealm.class.getName() + "\" />\n" +
                        "</Context>"), "context.xml");
    }

    @ArquillianResource
    private URL webapp;

    @Test
    public void lookup() throws NamingException
    {
        if ("true".equals(SystemInstance.get().getProperty("embedded"))) { /// tomee webapp is not deployed so skipping
            return;
        }

        final Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        p.put(Context.PROVIDER_URL, "http://" + webapp.getHost() + ":" + webapp.getPort() + "/tomee/ejb");
        p.put(Context.SECURITY_PRINCIPAL, "tom");
        p.put(Context.SECURITY_CREDENTIALS, "ee");
        p.put("openejb.authentication.realmName", "realm-test"); // webapp name to force login using the matching realm
        final Context ctx = new InitialContext(p);
        final Simple myBean = Simple.class.cast(ctx.lookup("java:global/realm-test/SimpleEJB!" + Simple.class.getName()));
        assertEquals("tom", myBean.name());
    }
}
