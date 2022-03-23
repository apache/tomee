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

package org.superbiz.cdi.stereotype;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

public class StereotypeTest {

    private static EJBContainer container;
    private static Journey journey;

    @BeforeClass
    public static void start() throws NamingException {
        container = EJBContainer.createEJBContainer();
        journey = (Journey) container.getContext().lookup("java:global/cdi-alternative-and-stereotypes/Journey");
    }

    @AfterClass
    public static void shutdown() {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void assertVehicleInjected() {
        assertEquals("the fastest", journey.vehicle());
    }

    @Test
    public void assertMockOverrideWorks() {
        assertEquals("simply the best", journey.category());
    }
}
