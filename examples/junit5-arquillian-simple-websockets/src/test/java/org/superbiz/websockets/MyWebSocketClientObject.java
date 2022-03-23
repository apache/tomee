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

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class MyWebSocketClientObject {

    public static CountDownLatch latch = new CountDownLatch(1);
    public static CountDownLatch payloadLatch = new CountDownLatch(1);
    public static String response;

    @OnMessage
    public void processMessage(String message) {

        if(message.equals("Successfully opened session")) {
            response = message;
            latch.countDown();
        }

        if(message.contains("Received:")) {
            payloadLatch.countDown();
            response = message;
        }
    }
}