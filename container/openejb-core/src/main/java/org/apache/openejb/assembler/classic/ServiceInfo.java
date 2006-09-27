package org.apache.openejb.assembler.classic;

import java.util.Properties;

public class ServiceInfo extends InfoObject implements ServiceTypeConstants {

    public int serviceType;
    public String description;
    public String serviceName;
    public String displayName;
    public String factoryClassName;
    public String codebase;
    public Class factoryClass;
    public Properties properties;

}
