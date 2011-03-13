package org.superbiz.moviefun;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "notifications")})
public class NotificationsBean implements MessageListener {

    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();

            System.out.println("");
            System.out.println("====================================");
            System.out.println("Notification received: " + text);
            System.out.println("====================================");
            System.out.println("");

            NotificationMonitor.showAlert(text);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}