package org.superbiz.dynamic.mbean;

import org.apache.openejb.api.Proxy;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;

/**
 * @author rmannibucau
 */
@Singleton
@Proxy(DynamicMBeanHandler.class)
@ObjectName(value = DynamicRemoteMBeanClient.OBJECT_NAME, url = "service:jmx:rmi:///jndi/rmi://localhost:8243/jmxrmi")
public interface DynamicRemoteMBeanClient {
    static final String OBJECT_NAME = "test:group=DynamicMBeanClientTest";

    int getCounter();
    void setCounter(int i);
    int length(String aString);

    @PreDestroy void clean();
}
