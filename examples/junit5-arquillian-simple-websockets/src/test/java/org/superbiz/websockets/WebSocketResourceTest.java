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

package org.superbiz.websockets;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.websocket.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@RunAsClient
@ExtendWith(ArquillianExtension.class)
public class WebSocketResourceTest {

    @ArquillianResource()
    private URL base;

    @Deployment(testable = false)
    public static WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "demo.war")
                .addClasses(WebSocketResource.class)
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"), "web.xml")
                .addAsWebInfResource(new File("src/main/resources/META-INF/beans.xml"), "beans.xml");
    }

    @Test
    public void testConnectAndReceiveMessage() throws Exception {
        Session session = connectToServer(MyWebSocketClientObject.class);
        assertNotNull(session);

        assertTrue(MyWebSocketClientObject.latch.await(2, TimeUnit.SECONDS));
        assertEquals("Successfully opened session", MyWebSocketClientObject.response);

        session.close();
    }

    @Test
    public void testConnectAndSendPayload() throws Exception {

        String payload = "I am the payload sent to this resource.";

        Session session = connectToServer(MyWebSocketClientObject.class);
        assertNotNull(session);

        assertTrue(MyWebSocketClientObject.latch.await(2, TimeUnit.SECONDS));
        assertEquals("Successfully opened session", MyWebSocketClientObject.response);

        session.getBasicRemote().sendText(payload);

        assertTrue(MyWebSocketClientObject.payloadLatch.await(2, TimeUnit.SECONDS));
        assertEquals("Received: " + payload, MyWebSocketClientObject.response);

        session.close();
    }

    /**
     * Method used to supply connection to the server by passing the naming of
     * the websocket endpoint
     */
    public Session connectToServer(Class<?> endpoint) throws DeploymentException, IOException, URISyntaxException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        assertNotNull(container);
        return container.connectToServer(endpoint, new URI("ws", base.getUserInfo(), base.getHost(), base.getPort(),base.getPath() + "api/socket",null, null));
    }

}
