//package com.company.tutorial.service;
//
//import junit.framework.TestCase;
//
//import java.util.Properties;
//
//import javax.annotation.Resource;
//import javax.ejb.embeddable.EJBContainer;
//import javax.jms.Connection;
//import javax.jms.ConnectionFactory;
//import javax.jms.JMSException;
//import javax.jms.MessageConsumer;
//import javax.jms.MessageProducer;
//import javax.jms.Queue;
//import javax.jms.Session;
//import javax.jms.TextMessage;
//
//import org.apache.openejb.loader.SystemInstance;
//import org.apache.openejb.testing.Configuration;
//import org.apache.openejb.testng.PropertiesBuilder;
//
//public class ChatBeanTest extends TestCase {
//
//    @Resource
//    private ConnectionFactory connectionFactory;
//
//    @Resource(name = "ChatBean")
//    private Queue questionQueue;
//
//    @Resource(name = "AnswerQueue")
//    private Queue answerQueue;
//    
//    public void test() throws Exception {
//
//        EJBContainer.createEJBContainer().getContext().bind("inject", this);
//
//        final Connection connection = connectionFactory.createConnection();
//
//        connection.start();
//
//        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//        final MessageProducer questions = session.createProducer(questionQueue);
//
//        final MessageConsumer answers = session.createConsumer(answerQueue);
//
//        sendText("Hello World!", questions, session);
//
//        assertEquals("Hello, Test Case!", receiveText(answers));
//
//        sendText("How are you?", questions, session);
//
//        assertEquals("I'm doing well.", receiveText(answers));
//
//        sendText("Still spinning?", questions, session);
//
//        assertEquals("Once every day, as usual.", receiveText(answers));
//
//    }
//
//    private void sendText(String text, MessageProducer questions, Session session) throws JMSException {
//
//        questions.send(session.createTextMessage(text));
//
//    }
//
//    private String receiveText(MessageConsumer answers) throws JMSException {
//
//        return ((TextMessage) answers.receive(1000)).getText();
//
//    }
//
//}