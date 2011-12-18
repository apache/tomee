package org.superbiz.dynamic.mbean;

import org.apache.openejb.api.Proxy;

import javax.ejb.Singleton;

@Singleton
@Proxy(DynamicMBeanHandler.class)
@ObjectName(DynamicMBeanClient.OBJECT_NAME)
public interface DynamicMBeanClient {
    static final String OBJECT_NAME = "test:group=DynamicMBeanClientTest";

    int getCounter();
    void setCounter(int i);
    int length(String aString);
}
