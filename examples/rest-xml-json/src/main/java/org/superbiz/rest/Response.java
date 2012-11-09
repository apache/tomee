package org.superbiz.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Response {
    private String value;

    public Response() {
        // no-op
    }

    public Response(final String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
