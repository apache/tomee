package org.superbiz.application;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MessagesReceived {

    private final List<String> messagesReceived = new ArrayList<String>();

    public List<String> getMessagesReceived() {
        return messagesReceived;
    }

    public void messageReceived(final String message) {
        messagesReceived.add(message);
    }
}
