package org.openejb.alt.config;

import org.openejb.alt.config.ejb11.EjbLocalRef;
import org.openejb.alt.config.ejb11.EjbRef;import org.openejb.alt.config.ejb11.Entity;import org.openejb.alt.config.ejb11.EnvEntry;import org.openejb.alt.config.ejb11.ResourceRef;import org.openejb.alt.config.ejb11.SecurityRoleRef;

public class EntityBean implements Bean {

    Entity bean;
    String type;

    EntityBean(Entity bean) {
        this.bean = bean;
        if ( bean.getPersistenceType().equals("Container") ) {
            type = CMP_ENTITY;
        } else {
            type = BMP_ENTITY;
        }
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
	
    public String getType() {
        return type;
    }

    public Object getBean() {
        return bean;
    }

    public String getEjbName(){
        return bean.getEjbName();
    }

    public String getEjbClass(){
        return bean.getEjbClass();
    }

    public String getHome(){
        return bean.getHome();
    }

    public String getRemote(){
        return bean.getRemote();
    }

    public String getPrimaryKey(){
        return bean.getPrimKeyClass();
    }

    public EjbRef[] getEjbRef(){
        return bean.getEjbRef();
    }

    public EnvEntry[] getEnvEntry(){
        return bean.getEnvEntry();
    }

    public ResourceRef[] getResourceRef(){
        return bean.getResourceRef();
    }

    public SecurityRoleRef[] getSecurityRoleRef(){
        return bean.getSecurityRoleRef();
    }

}

