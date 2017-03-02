package com.company.tutorial.service;

import java.io.IOException;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


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