package org.superbiz.connector.application;
import org.superbiz.connector.api.InboundListener;

import javax.ejb.MessageDriven;


import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;

@MessageDriven(name = "Receiver")
public class Receiver implements InboundListener {

    @Inject
    private MessagesReceived messagesReceived;

    @Resource
    private MessageDrivenContext context;
    
    @Override
    public void receiveMessage(String message) {
        messagesReceived.messageReceived(message);
    }
}
