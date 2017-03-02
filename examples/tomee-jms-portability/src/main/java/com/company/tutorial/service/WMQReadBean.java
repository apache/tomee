package com.company.tutorial.service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


//1  -D<deploymentId>.activation.<property>=<value>
//2. -D<ejbName>.activation.<property>=<value>
//3. -D<message-listener-interface>.activation.<property>=<value>
//4. -Dmdb.activation.<property>=<value>

public class WMQReadBean implements MessageListener {
	
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