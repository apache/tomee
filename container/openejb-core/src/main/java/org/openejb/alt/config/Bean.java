package org.openejb.alt.config;

import org.openejb.alt.config.ejb11.EjbLocalRef;
import org.openejb.alt.config.ejb11.EjbRef;
import org.openejb.alt.config.ejb11.EnvEntry;
import org.openejb.alt.config.ejb11.ResourceRef;
import org.openejb.alt.config.ejb11.SecurityRoleRef;

public interface Bean {

    public static final String BMP_ENTITY = "BMP_ENTITY";
    public static final String CMP_ENTITY = "CMP_ENTITY";
    public static final String STATEFUL   = "STATEFUL";
    public static final String STATELESS  = "STATELESS";

    public String getType();

    public Object getBean();

    public String getEjbName();
    public String getEjbClass();
    public String getHome();
    public String getRemote();
    public String getLocalHome();
    public String getLocal();

    public EjbRef[] getEjbRef();
    public EjbLocalRef[] getEjbLocalRef();
    public EnvEntry[] getEnvEntry();
    public ResourceRef[] getResourceRef();
    public SecurityRoleRef[] getSecurityRoleRef();

}

