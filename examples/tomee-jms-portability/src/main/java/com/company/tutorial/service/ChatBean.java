/*package com.company.tutorial.service;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;


@MessageDriven(name="ChatBean", activationConfig = {
@ActivationConfigProperty(
      propertyName = "destinationType", 
      propertyValue = "javax.jms.Queue"),
   @ActivationConfigProperty(
      propertyName = "destination", 
      propertyValue = "${ChatBean}")})
      //propertyValue = "ChatBean")})
public class ChatBean implements MessageListener {
	

	@Resource(name = "amq_qcf")
    private ConnectionFactory amq_connectionFactory;

    @Resource(name = "AnswerQueue")
    private Queue answerQueue;

    public void onMessage(Message message) {
        try {

            final TextMessage textMessage = (TextMessage) message;
            final String question = textMessage.getText();
            System.out.println("*************Properties: mdb.activation.destination : " + System.getProperty("mdb.activation.destination"));
            System.out.println("*************Properties: java.util.logging.config.file : " + System.getProperty("java.util.logging.config.file"));
            
            if ("Hello World!".equals(question)) {

                respond("Hello, Test Case!");

            } else if ("How are you?".equals(question)) {

                respond("I'm doing well.");

            } else if ("Still spinning?".equals(question)) {

                respond("Once every day, as usual.");

            }
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }

    private void respond(String text) throws JMSException {

        Connection connection = null;
        Session session = null;

        try {
            connection = amq_connectionFactory.createConnection();
            connection.start();

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create a MessageProducer from the Session to the Topic or Queue
            MessageProducer producer = session.createProducer(answerQueue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // Create a message
            TextMessage message = session.createTextMessage(text);

            // Tell the producer to send the message
            producer.send(message);
        } finally {
            // Clean up
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
*/