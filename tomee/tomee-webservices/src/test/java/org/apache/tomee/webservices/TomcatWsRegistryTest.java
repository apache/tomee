/*
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
package org.apache.tomee.webservices;

import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TomcatWsRegistryTest {

    @Test
    public void securityConstraintCoversAllHttpMethods() {
        final StandardContext context = new StandardContext();

        TomcatWsRegistry.addSecurityConstraints(context, "ws-test", "CONFIDENTIAL");

        final SecurityConstraint[] constraints = context.findConstraints();
        assertEquals(1, constraints.length);
        final SecurityConstraint constraint = constraints[0];
        assertTrue(constraint.getAuthConstraint());
        assertEquals("CONFIDENTIAL", constraint.getUserConstraint());

        final SecurityCollection[] collections = constraint.findCollections();
        assertEquals(1, collections.length);
        final SecurityCollection collection = collections[0];
        assertTrue(collection.findPattern("/*"));
        assertEquals(0, collection.findMethods().length);
        assertEquals(0, collection.findOmittedMethods().length);
        for (final String method : new String[]{"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD", "TRACE", "FOO"}) {
            assertTrue(method + " must be covered", collection.findMethod(method));
        }

        assertTrue(context.getDenyUncoveredHttpMethods());
    }
}
