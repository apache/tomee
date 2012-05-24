package org.apache.tomee.webapp.helper.rest;

public class Service {
    private String name;
    private String address;

    public Service(final String name, final String address) {
        this.name = name;
        this.address = address;
    }

    public Service() {
        // no-op
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
