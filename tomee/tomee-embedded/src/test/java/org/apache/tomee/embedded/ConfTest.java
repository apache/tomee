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

import org.apache.catalina.Realm;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.realm.JAASRealm;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.catalina.TomEERealm;
import org.apache.tomee.loader.TomcatHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfTest {
    @Test
    public void run() {
        try (final Container container = new Container(new Configuration().conf("ConfTest"))) {
            final StandardServer standardServer = TomcatHelper.getServer();
            final Realm engineRealm = standardServer.findServices()[0].getContainer().getRealm();
            assertTrue(String.valueOf(engineRealm), TomEERealm.class.isInstance(engineRealm));
            assertTrue(String.valueOf(engineRealm), JAASRealm.class.isInstance(TomEERealm.class.cast(engineRealm).getNestedRealms()[0]));
            final JAASRealm jaas = JAASRealm.class.cast(TomEERealm.class.cast(engineRealm).getNestedRealms()[0]);
            assertEquals("PropertiesLoginModule", jaas.getAppName());
            assertEquals("org.apache.openejb.core.security.jaas.UserPrincipal", jaas.getUserClassNames());
            assertEquals("org.apache.openejb.core.security.jaas.GroupPrincipal", jaas.getRoleClassNames());

            assertEquals("test", SystemInstance.get().getProperty("ConfTest.value"));
        }
    }
}
