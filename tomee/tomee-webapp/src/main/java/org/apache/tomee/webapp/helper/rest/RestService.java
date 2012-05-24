package org.apache.tomee.webapp.helper.rest;

public class RestService {
    private String name;
    private String wadl;

    public RestService(final String name, final String wadl) {
        this.name = name;
        this.wadl = wadl;
    }

    public RestService() {
        // no-op
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWadl() {
        return wadl;
    }

    public void setWadl(String wadl) {
        this.wadl = wadl;
    }
}
