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

package org.apache.openejb.junit5;

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.junit.ContextConfig;
import org.apache.openejb.junit.Property;
import org.apache.openejb.junit5.ejbs.BasicEjbLocal;
import org.apache.openejb.junit5.jee.EjbContainerExtension;
import org.apache.openejb.junit5.jee.transaction.TransactionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ejb.EJB;

import static org.junit.jupiter.api.Assertions.*;


@ContextConfig(properties = {
    @Property("openejb.deployments.classpath.include=.*openejb-junit5-backward.*"),
    @Property("java.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory")
})
@RunWithOpenEjb
@LocalClient
public class TestEjbBasic {
    @EJB
    private BasicEjbLocal sampleEjb;

    public TestEjbBasic() {
    }

    @Test
    public void testEjbInjection() {
        assertNotNull(sampleEjb);
    }

    @Test
    public void testEjbInvocation() {
        assertNotNull(sampleEjb);

        final String object = sampleEjb.concat("Hello", "World");
        assertEquals("Hello World", object);

        final double root = sampleEjb.squareroot(81);
        assertEquals(9, root, 0.0);
    }

    @Test
    public void testEjbException() {
        assertNotNull(sampleEjb);

        try {
            sampleEjb.squareroot(-1);
            fail("Call must fail with exception.");
        } catch (final Exception e) {
        }
    }
}
