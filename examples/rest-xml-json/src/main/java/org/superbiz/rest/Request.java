package org.superbiz.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Request {
    private String value;

    public Request() {
        //no-op
    }

    public Request(final String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
