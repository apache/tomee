package org.apache.openejb.assembler.classic;

import java.util.Properties;

public abstract class ContainerInfo extends InfoObject {

    public static final int ENTITY_CONTAINER = 0;

    public static final int STATEFUL_SESSION_CONTAINER = 2;

    public static final int STATELESS_SESSION_CONTAINER = 3;

    public String description;
    public String displayName;
    public String containerName;
    public String codebase;
    public String className;
    public EnterpriseBeanInfo[] ejbeans;
    public Properties properties;
    public String[] constructorArgs;

    public int containerType;
}
