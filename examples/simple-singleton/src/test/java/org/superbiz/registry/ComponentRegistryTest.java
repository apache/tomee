/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.registry;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.net.URI;
import java.util.Collection;
import java.util.Date;

public class ComponentRegistryTest {

    private final static EJBContainer ejbContainer = EJBContainer.createEJBContainer();

    @Test
    public void oneInstancePerMultipleReferences() throws Exception {

        final Context context = ejbContainer.getContext();

        // Both references below will point to the exact same instance
        final ComponentRegistry one = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");
        final ComponentRegistry two = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");

        final URI expectedUri = new URI("foo://bar/baz");
        one.setComponent(URI.class, expectedUri);
        final URI actualUri = two.getComponent(URI.class);
        Assert.assertSame(expectedUri, actualUri);

        two.removeComponent(URI.class);
        URI uri = one.getComponent(URI.class);
        Assert.assertNull(uri);

        one.removeComponent(URI.class);
        uri = two.getComponent(URI.class);
        Assert.assertNull(uri);

        final Date expectedDate = new Date();
        two.setComponent(Date.class, expectedDate);
        final Date actualDate = one.getComponent(Date.class);
        Assert.assertSame(expectedDate, actualDate);

        Collection<?> collection = one.getComponents();
        System.out.println(collection);
        Assert.assertEquals("Reference 'one' - ComponentRegistry contains one record", collection.size(), 1);

        collection = two.getComponents();
        Assert.assertEquals("Reference 'two' - ComponentRegistry contains one record", collection.size(), 1);
    }

    @AfterClass
    public static void closeEjbContainer() {
        ejbContainer.close();
    }
}
