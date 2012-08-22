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
package org.superbiz.client;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.client.app.Listener;

import java.net.URL;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

@RunAsClient
@RunWith(Arquillian.class)
public class SenderTest {
    private static final String APP_NAME = "app-server";
    private static final String MESSAGE = "sender test message";

    // this sample is just about configuration so
    // to avoid complicated code in this demo we use sleep
    // shouldn't be done this way in real life
    private static final int RETRY = 10;
    private static final int SLEEP_RETRY = 500;

    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .addPackage(Listener.class.getPackage());

    }

    @BeforeClass
    public static void configureClientResources() {
        // can be set this way or with the key Resource/<type>
        // in fact we create on client side a mini jndi tree
        // the key is the jndi name (the one used for the lookup)
        System.setProperty("aConnectionFactory", "connectionfactory:org.apache.activemq.ActiveMQConnectionFactory:tcp://localhost:61616");
        System.setProperty("aQueue", "queue:org.apache.activemq.command.ActiveMQQueue:LISTENER");
    }

    @Test
    public void send() throws Exception {
        final String rawUrl = url.toExternalForm(); // app-server webapp url
        final String providerUrl = rawUrl.substring(0, rawUrl.length() - APP_NAME.length() - 1) + "tomee/ejb";

        // send the message
        Sender.send(providerUrl, MESSAGE);

        // check the message was received, we can need to wait a bit
        for (int i = 0; i < RETRY; i++) {
            final String message = IO.slurp(new URL(rawUrl + "messages")); // the servlet URL
            System.out.println("Server received: " + message);
            try {
                assertThat(message, containsString(MESSAGE));
                return; // done!
            } catch (AssertionError ae) {
                Thread.sleep(SLEEP_RETRY); // wait a bit that the message was received
            }
        }
        fail();
    }
}
