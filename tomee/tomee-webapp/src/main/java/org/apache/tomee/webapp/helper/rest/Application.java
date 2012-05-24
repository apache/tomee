package org.apache.tomee.webapp.helper.rest;

import java.util.ArrayList;
import java.util.List;

public class Application {
    private String name;
    private List<RestService> services = new ArrayList<RestService>();

    public List<RestService> getServices() {
        return services;
    }

    public void setServices(List<RestService> services) {
        this.services = services;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
