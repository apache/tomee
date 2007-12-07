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
package org.apache.openejb.examples.injection;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class InvoiceBeanTest extends TestCase {

    private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*resource-injection.*");

        initialContext = new InitialContext(properties);
    }

    public void testAddLineItem() throws Exception {
        Invoice order = (Invoice) initialContext.lookup("InvoiceBeanRemote");
        assertNotNull(order);
        LineItem item = new LineItem("ABC-1", "Test Item");

        try {
            order.addLineItem(item);
        } catch (TooManyItemsException tmie) {
            fail("Test failed due to: " + tmie.getMessage());
        }
    }

    public void testGetMaxLineItems() throws Exception {
        Invoice order = (Invoice) initialContext.lookup("InvoiceBeanRemote");
        assertNotNull(order);

        int maxLineItems = order.getMaxLineItems();

        assertEquals(15, maxLineItems);
    }
}
