package org.openejb.alt.config;

import org.openejb.alt.config.ejb11.EjbLocalRef;
import org.openejb.alt.config.ejb11.EjbRef;
import org.openejb.alt.config.ejb11.EnvEntry;
import org.openejb.alt.config.ejb11.ResourceRef;
import org.openejb.alt.config.ejb11.SecurityRoleRef;
import org.openejb.alt.config.ejb11.Session;

public class SessionBean implements Bean {

    Session bean;
    String type;

    SessionBean(Session bean) {
        this.bean = bean;
        if (bean.getSessionType().equals("Stateful")) {
            type = STATEFUL;
        } else {
            type = STATELESS;
        }
    }

    public String getType() {
        return type;
    }

    public Object getBean() {
        return bean;
    }

    public String getEjbName() {
        return bean.getEjbName();
    }

    public String getEjbClass() {
        return bean.getEjbClass();
    }

    public String getHome() {
        return bean.getHome();
    }

    public String getRemote() {
        return bean.getRemote();
    }

    public EjbLocalRef[] getEjbLocalRef() {
        return bean.getEjbLocalRef();
    }

    public String getLocal() {
        return bean.getLocal();
    }

    public String getLocalHome() {
        return bean.getLocalHome();
    }

    public EjbRef[] getEjbRef() {
        return bean.getEjbRef();
    }

    public EnvEntry[] getEnvEntry() {
        return bean.getEnvEntry();
    }

    public ResourceRef[] getResourceRef() {
        return bean.getResourceRef();
    }

    public SecurityRoleRef[] getSecurityRoleRef() {
        return bean.getSecurityRoleRef();
    }
}

