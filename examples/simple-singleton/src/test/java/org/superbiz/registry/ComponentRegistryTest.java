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
package org.superbiz.registry;

import junit.framework.TestCase;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.net.URI;
import java.util.Date;

//START SNIPPET: code
public class ComponentRegistryTest extends TestCase {

    public void test() throws Exception {

        final Context context = EJBContainer.createEJBContainer().getContext();

        // Both references below will point to the exact same instance
        ComponentRegistry one = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");

        ComponentRegistry two = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");


        // Let's prove both references point to the same instance


        // Set a URL into 'one' and retrieve it from 'two'

        URI expectedUri = new URI("foo://bar/baz");

        one.setComponent(URI.class, expectedUri);

        URI actualUri = two.getComponent(URI.class);

        assertSame(expectedUri, actualUri);


        // Set a Date into 'two' and retrieve it from 'one'

        Date expectedDate = new Date();

        two.setComponent(Date.class, expectedDate);

        Date actualDate = one.getComponent(Date.class);

        assertSame(expectedDate, actualDate);

    }
}
//END SNIPPET: code
