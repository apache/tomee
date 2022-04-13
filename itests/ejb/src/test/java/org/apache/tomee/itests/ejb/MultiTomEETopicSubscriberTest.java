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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.itests.ejb;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MultiTomEETopicSubscriberTest {

    private BrokerService broker;

    @Before
    public void setUp() throws Exception {
        // start an ActiveMQ broker
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(true);
        broker.addConnector("tcp://localhost:0"); // pick a random available port

        broker.start();
    }

    @After
    public void tearDown() throws Exception {
        broker.stop();
    }

    @Test
    public void test() throws Exception {
        // get the ActiveMQ OpenWire port
        final TransportConnector tc = broker.getTransportConnectors().iterator().next();
        final int port = tc.getConnectUri().getPort();

        // start 2 TomEE servers
        final TomEE tomee1 = buildTomEE(port);
        final TomEE tomee2 = buildTomEE(port);

        // the key thing here is that both of these servers should be able to subscribe to the topic
        // from their respective MDBs without exceptions.

        // lets send some test messages from 1 of the servers
        IO.slurp(new URL("http://localhost:" + tomee1.getPort() + "/test/api/messages/test"));
        Thread.sleep(5000);

        // and check that all the messages were received on both servers
        final String result1 = IO.slurp(new URL("http://localhost:" + tomee1.getPort() + "/test/api/messages/count"));
        final String result2 = IO.slurp(new URL("http://localhost:" + tomee2.getPort() + "/test/api/messages/count"));

        Assert.assertEquals(1000, Integer.parseInt(result1));
        Assert.assertEquals(1000, Integer.parseInt(result2));
    }

    private TomEE buildTomEE(final int activemqPort) throws Exception {
        return TomEE.plus()
                .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                        .add(MessageCounter.class)
                        .add(MessageReceiver.class)
                        .add(MessageResource.class)
                        .add(MessageSender.class)
                        .asJar())
                .home(h -> updateSystemProperties(h, activemqPort))
                .build();
    }

    private void updateSystemProperties(final File home, final int activemqPort) {
        try {
            final File systemProps = Files.file(home, "conf", "system.properties");
            String props = IO.slurp(systemProps);

            props = props + "\namq=new://Resource?type=ActiveMQResourceAdapter" +
                    "\namq.DataSource=" +
                    "\namq.BrokerXmlConfig=" +
                    "\namq.ServerUrl=tcp://localhost:" + activemqPort +
                    "\ntarget=new://Resource?type=Topic" +
                    "\nmdbs=new://Container?type=MESSAGE" +
                    "\nmdbs.ResourceAdapter=amq" +
                    "\nmdbs.pool=false" +
                    "\ncf=new://Resource?type=" + ConnectionFactory.class.getName() +
                    "\ncf.ResourceAdapter=amq" +
                    "\nmdb.activation.clientId={ejbName}-{uniqueId}";

            IO.copy(IO.read(props), systemProps);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}