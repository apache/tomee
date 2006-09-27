package org.apache.openejb.assembler.classic;

public class EntityBeanInfo extends EnterpriseBeanInfo {

    public String primKeyClass;
    public String primKeyField;
    public String persistenceType;
    public String reentrant;
    public String [] cmpFieldNames;
    public QueryInfo [] queries;

    public EntityBeanInfo() {
        type = ENTITY;
    }

}
