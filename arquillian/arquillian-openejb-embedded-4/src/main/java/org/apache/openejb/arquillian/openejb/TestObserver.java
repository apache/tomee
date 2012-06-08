package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
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
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class)
                                        .getBeanContext(event.getEvent().getTestClass().getJavaClass().getName());
        ThreadContext oldCtx = null;
        ClassLoader oldCl = null;

        if (context != null) {
            oldCtx = ThreadContext.enter(new ThreadContext(context, null));
        } else {
            oldCl = Thread.currentThread().getContextClassLoader();
            setTCCL(classLoader.get());
        }

        try {
            event.proceed();
        } finally {
            if (context != null) {
                ThreadContext.exit(oldCtx);
            } else {
                setTCCL(oldCl);
            }
        }
    }

    private void setTCCL(final ClassLoader cl) {
        Thread.currentThread().setContextClassLoader(cl);
    }
}
