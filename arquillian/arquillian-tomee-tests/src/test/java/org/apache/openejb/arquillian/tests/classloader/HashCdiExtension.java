package org.apache.openejb.arquillian.tests.classloader;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.util.HashMap;
import java.util.Map;

// bean manager holder to check we are compatible with such impl
public class HashCdiExtension implements Extension {
    public static Map<ClassLoader, BeanManager> BMS = new HashMap<ClassLoader, BeanManager>();

    protected void setBeanManager(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        BMS.put(tccl, beanManager);
    }
}
