package org.superbiz.model;

import java.io.Serializable;

import javax.inject.Named;
import javax.mvc.RedirectScoped;

@Named("message")
@RedirectScoped
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageRedirect;

    public String getMessageRedirect() {
        return messageRedirect;
    }

    public void setMessageRedirect(String messageRedirect) {
        this.messageRedirect = messageRedirect;
    }
}