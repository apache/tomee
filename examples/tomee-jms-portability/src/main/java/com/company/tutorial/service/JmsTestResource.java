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

import java.io.IOException;

import jakarta.annotation.Resource;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/jms")
public class JmsTestResource {
 
    @Resource(name = "imq.bar")
    private Queue imq_barQueue;
   
    @Resource(name = "amq.bar")
    private Queue amq_barQueue;
    
    @Resource(name = "amq.bar.pop")
    private Queue amq_bar_popQueue;
    
    @Resource(name = "amq.variable")
    private Queue amq_variableQueue;
    
    @Resource(name = "imq_qcf")
    private ConnectionFactory imq_connectionFactory;
    
    @Resource(name = "amq_qcf")
    private ConnectionFactory amq_connectionFactory;
    
    
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @Path("/send2imq")
    public Response testSend1(@QueryParam("msg") String msg) throws IOException, JMSException {
        Connection connection = imq_connectionFactory.createConnection();
        connection.start();
 
        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(imq_barQueue);
        
        System.out.println("*************producer.getClass() = "+ producer.getClass());
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
        // Create a message
        TextMessage message = session.createTextMessage((msg!=null && !msg.isEmpty()) ? msg : "Hello World!");
 
        // Tell the producer to send the message
        producer.send(message);
 
        return Response.ok().build();
    }
    
    
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @Path("/send2amq")
    public Response testSend2(@QueryParam("msg") String msg) throws IOException, JMSException {
        Connection connection = amq_connectionFactory.createConnection();
        connection.start();
 
        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(amq_barQueue);
        
        System.out.println("*************producer.getClass() = "+ producer.getClass());
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
        // Create a message
        TextMessage message = session.createTextMessage((msg!=null && !msg.isEmpty()) ? msg : "Hello World!");
 
        // Tell the producer to send the message
        producer.send(message);
 
        return Response.ok().build();
    }
    
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @Path("/send2amq2")
    public Response testSend3(@QueryParam("msg") String msg) throws IOException, JMSException {
        Connection connection = amq_connectionFactory.createConnection();
        connection.start();
 
        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(amq_bar_popQueue);
        
        System.out.println("*************producer.getClass() = "+ producer.getClass());
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
        // Create a message
        TextMessage message = session.createTextMessage((msg!=null && !msg.isEmpty()) ? msg : "Hello World!");
 
        // Tell the producer to send the message
        producer.send(message);
 
        return Response.ok().build();
    }
    
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @Path("/send2amq3")
    public Response testSend4(@QueryParam("msg") String msg) throws IOException, JMSException {
        Connection connection = amq_connectionFactory.createConnection();
        connection.start();
 
        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(amq_variableQueue);
        
        System.out.println("*************producer.getClass() = "+ producer.getClass());
        System.out.println("*************amq.variable.destination : "+ System.getProperty("amq.variable.destination"));
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
        // Create a message
        TextMessage message = session.createTextMessage((msg!=null && !msg.isEmpty()) ? msg : "Hello World!");
 
        // Tell the producer to send the message
        producer.send(message);
 
        return Response.ok().build();
    }
}