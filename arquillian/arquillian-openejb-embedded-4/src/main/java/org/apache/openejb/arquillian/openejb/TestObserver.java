package org.apache.openejb.arquillian.openejb;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.Test;

public class TestObserver {
    @Inject
    @SuiteScoped
    private Instance<ClassLoader> classLoader;

    public void observe(@Observes EventContext<Test> event) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        setTCCL(classLoader.get());
        try {
            event.proceed();
        } finally {
            setTCCL(cl);
        }
    }

    private void setTCCL(final ClassLoader cl) {
        Thread.currentThread().setContextClassLoader(cl);
    }
}
