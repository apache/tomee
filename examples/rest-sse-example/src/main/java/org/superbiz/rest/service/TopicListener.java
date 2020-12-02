package org.superbiz.rest.service;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "EVENT"),
})
public class TopicListener implements MessageListener {

    @Inject
    private Event<Message> messageReceivedEvent;

    @Override
    public void onMessage(final Message message) {
        messageReceivedEvent.fire(message);
    }
}
