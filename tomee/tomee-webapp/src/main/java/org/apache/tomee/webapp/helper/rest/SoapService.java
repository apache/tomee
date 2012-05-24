package org.apache.tomee.webapp.helper.rest;

public class SoapService extends Service {
    private String port;

    public SoapService(String name, String address, String port) {
        super(name, address);
        this.port = port;
    }

    public SoapService() {
        // no-op
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
