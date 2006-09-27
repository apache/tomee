package org.apache.openejb.alt.config;

import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.ResourceRef;

public class EntityBean implements Bean {

    org.apache.openejb.jee.EntityBean bean;
    String type;

    EntityBean(org.apache.openejb.jee.EntityBean bean) {
        this.bean = bean;
        if (bean.getPersistenceType() == PersistenceType.CONTAINER) {
            type = CMP_ENTITY;
        } else {
            type = BMP_ENTITY;
        }
    }

    public String getLocal() {
        return bean.getLocal();
    }

    public String getLocalHome() {
        return bean.getLocalHome();
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

    public String getPrimaryKey() {
        return bean.getPrimKeyClass();
    }

    public ResourceRef[] getResourceRef() {
        return bean.getResourceRef().toArray(new ResourceRef[]{});
    }
}

