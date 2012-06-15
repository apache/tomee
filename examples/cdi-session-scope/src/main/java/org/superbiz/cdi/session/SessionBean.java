package org.superbiz.cdi.session;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;

@SessionScoped
public class SessionBean implements Serializable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
