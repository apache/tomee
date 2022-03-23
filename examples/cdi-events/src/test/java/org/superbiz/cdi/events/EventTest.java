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
package org.superbiz.cdi.events;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;
import javax.naming.NamingException;

import static org.junit.Assert.assertTrue;

public class EventTest {

    private static EJBContainer container;
    private static String initialLogProperty;

    @Inject
    private Observer observer;

    @BeforeClass
    public static void start() throws NamingException {
        initialLogProperty = System.getProperty("openejb.logger.external");
        System.setProperty("openejb.logger.external", "true");
        container = EJBContainer.createEJBContainer();
    }

    @AfterClass
    public static void shutdown() {
        if (container != null) {
            container.close();
        }
        if (initialLogProperty != null) {
            System.setProperty("openejb.logger.external", initialLogProperty);
        } else {
            System.getProperties().remove("openejb.logger.external");
        }
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
    }

    @After
    public void reset() throws NamingException {
        container.getContext().unbind("inject");
    }

    @Test
    public void observe() throws InterruptedException {
        Thread.sleep(4000);
        assertTrue(observer.getDates().size() > 3); // in 4s normally at least 3 events were received
    }
}
