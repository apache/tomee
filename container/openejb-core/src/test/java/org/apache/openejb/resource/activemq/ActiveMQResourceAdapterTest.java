/**
 *
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
package org.apache.openejb.resource.activemq;

import junit.framework.TestCase;
import org.apache.activemq.broker.BrokerService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.reflection.Reflections;

import java.util.concurrent.TimeUnit;

public class ActiveMQResourceAdapterTest extends TestCase {
    @Override
    protected void tearDown() throws Exception {
        cleanup();
    }
    @Override
    protected void setUp() throws Exception {
        cleanup();
    }

    private void cleanup() throws Exception {
        for (final BrokerService bs : ActiveMQFactory.getBrokers()) {
            bs.stop();
        }
    }

    public void test() throws Exception {
        final ActiveMQResourceAdapter resourceAdapter = new ActiveMQResourceAdapter();
        resourceAdapter.setServerUrl("vm://localhost?waitForStart=30000&async=false");

        final String brokerAddress = NetworkUtil.getLocalAddress("broker:(tcp://", ")?useJmx=false");
        resourceAdapter.setBrokerXmlConfig(brokerAddress);
        resourceAdapter.setStartupTimeout(new Duration(10, TimeUnit.SECONDS));

        //    DataSource Default Unmanaged JDBC Database
        //
        resourceAdapter.start(null);
    }

    public void testSchedulerSupport() throws Exception {
        final ActiveMQResourceAdapter resourceAdapter = new ActiveMQResourceAdapter();
        resourceAdapter.setServerUrl("vm://localhost?waitForStart=30000&async=false");
        resourceAdapter.setStartupTimeout(new Duration(10, TimeUnit.SECONDS));

        final String brokerAddress = NetworkUtil.getLocalAddress("broker:(tcp://", ")?useJmx=false&schedulerSupport=true");
        resourceAdapter.setBrokerXmlConfig(brokerAddress);
        resourceAdapter.start(null);
        assertTrue(Boolean.class.cast(Reflections.get(ActiveMQFactory.getBrokers().iterator().next(), "schedulerSupport")));
        resourceAdapter.stop();

        resourceAdapter.setBrokerXmlConfig(NetworkUtil.getLocalAddress("broker:(tcp://", ")?useJmx=false&schedulerSupport=false"));
        resourceAdapter.start(null);
        assertFalse(Boolean.class.cast(Reflections.get(ActiveMQFactory.getBrokers().iterator().next(), "schedulerSupport")));
        resourceAdapter.stop();
    }
}
