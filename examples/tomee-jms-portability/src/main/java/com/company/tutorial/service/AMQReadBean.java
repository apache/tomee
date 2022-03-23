/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.company.tutorial.service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;


//1  -D<deploymentId>.activation.<property>=<value>
//2. -D<ejbName>.activation.<property>=<value>
//3. -D<message-listener-interface>.activation.<property>=<value>
//4. -Dmdb.activation.<property>=<value>

public class AMQReadBean implements MessageListener {
	
    public void onMessage(Message message) {
        try {
            final TextMessage textMessage = (TextMessage) message;
            final String text = textMessage.getText();
            System.out.println("*************Properties: mdb.activation.destination : " + System.getProperty("mdb.activation.destination"));
            System.out.println("*************Properties: java.util.logging.config.file : " + System.getProperty("java.util.logging.config.file"));
            
            System.out.println("****Read Message****"+ text);
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }
}