package org.apache.tomee.webapp.helper.rest;

import java.util.ArrayList;
import java.util.List;

public class Application {
    private String name;
    private List<Service> services = new ArrayList<Service>();

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
