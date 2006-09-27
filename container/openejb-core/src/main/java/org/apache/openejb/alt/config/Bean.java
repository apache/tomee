package org.apache.openejb.alt.config;

import org.apache.openejb.jee.ResourceRef;

public interface Bean {

    public static final String BMP_ENTITY = "BMP_ENTITY";
    public static final String CMP_ENTITY = "CMP_ENTITY";
    public static final String STATEFUL = "STATEFUL";
    public static final String STATELESS = "STATELESS";

    public String getType();

    public Object getBean();

    public String getEjbName();

    public String getEjbClass();

    public String getHome();

    public String getRemote();

    public String getLocalHome();

    public String getLocal();

    public ResourceRef[] getResourceRef();
}

