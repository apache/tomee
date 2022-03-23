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
package org.apache.openejb.activemq;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jms.JMSConnectionFactoryDefinition;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SimpleLog
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class JMSConnectionFactoryTest {
    @JMSConnectionFactoryDefinition(name = "testConnectionFactory", maxPoolSize = 5, user = "test", password = "still a test")
    public static class JMSConfiguration {
    }

    @Test
    public void checkConnectionFactoryIsThere() throws NamingException {
        // auto created
        assertNotNull(SystemInstance.get().getComponent(ContainerSystem.class).getContainer("Default Managed Container"));

        final List<ResourceInfo> resources = SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.resources;
        boolean found = false;
        for (final ResourceInfo r : resources) {
            if (r.id.equals("JMSConnectionFactoryTest/testConnectionFactory")) { // prefixed with app name
                found = true;
                break;
            }
        }
        assertTrue(found);

        // these lookup must pass
        final Context jndiContext = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();

        final Object directLookup = jndiContext.lookup("openejb:Resource/testConnectionFactory");
        assertNotNull(directLookup);

        final Object appLookup = jndiContext.lookup("openejb:Resource/JMSConnectionFactoryTest/testConnectionFactory");
        assertNotNull(appLookup);

        // facade are not the same but the underlying connection factory should be
        assertEquals(Reflections.get(directLookup, "factory"), Reflections.get(appLookup, "factory"));
    }
}
