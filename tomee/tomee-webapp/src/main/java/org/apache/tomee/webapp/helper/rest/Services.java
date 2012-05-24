package org.apache.tomee.webapp.helper.rest;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Services {
    private List<Application> applications = new ArrayList<Application>();

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public Application returnOrCreateApplication(final String name) {
        for (Application app : applications) {
            if (app.getName().equals(name)) {
                return app;
            }
        }

        final Application app = new Application();
        app.setName(name);
        applications.add(app);
        return app;
    }
}
