package org.superbiz.mdb;

public class ChatRespondCreator {
    public String respond(String question) {
        if ("Hello World!".equals(question)) {
            return "Hello, Test Case!";
        } else if ("How are you?".equals(question)) {
            return "I'm doing well.";
        } else if ("Still spinning?".equals(question)) {
            return "Once every day, as usual.";
        }
        return null;
    }
}
