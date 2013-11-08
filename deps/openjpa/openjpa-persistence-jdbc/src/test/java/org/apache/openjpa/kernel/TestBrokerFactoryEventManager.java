/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.kernel;

import javax.persistence.Persistence;

import junit.framework.TestCase;
import org.apache.openjpa.event.BrokerFactoryListener;
import org.apache.openjpa.event.BrokerFactoryEvent;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

public class TestBrokerFactoryEventManager
    extends TestCase {

    public void testCreateEvent() {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.cast(
                Persistence.createEntityManagerFactory("test"));
        ListenerImpl listener = new ListenerImpl();
        emf.getConfiguration().getBrokerFactoryEventManager()
            .addListener(listener);
        emf.createEntityManager().close();
        assertTrue(listener.createEventReceived);
        emf.close();
    }

    private class ListenerImpl implements BrokerFactoryListener {

        boolean createEventReceived = false;

        public void eventFired(BrokerFactoryEvent event) {
            if (event.getEventType()
                == BrokerFactoryEvent.BROKER_FACTORY_CREATED)
                createEventReceived = true;
        }
    }
}
