package org.apache.openejb.arquillian.common.deployment;

import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeploymentExceptionObserver {
    private static final Map<Class<?>, Exception> EXCEPTIONS = new HashMap<Class<?>, Exception>();
    private static final Map<Class<?>, Exception> PARENT_EXCEPTIONS = new HashMap<Class<?>, Exception>();

    public void observes(@Observes final EventContext<DeployDeployment> dd) throws Exception {
        try {
            dd.proceed();
        } catch (Exception t) {
            if (t == null) {
                EXCEPTIONS.put(NullPointerException.class, new NullPointerException());
            } else {
                EXCEPTIONS.put(t.getClass(), t);
                Throwable current = t.getCause();
                while (current != null) {
                    if (current instanceof Exception) {
                        PARENT_EXCEPTIONS.put(current.getClass(), (Exception) current);
                    }
                    if (current.getCause() != current) {
                        current = current.getCause();
                    }
                }
            }
            throw t;
        }
    }

    public static Exception getExceptions(final Class<?> clazz) {
        final Exception ex = EXCEPTIONS.get(clazz);
        if (ex != null) {
            return ex;
        }
        return PARENT_EXCEPTIONS.get(clazz);
    }

    public static Set<Class<?>> availableExceptionTypes() {
        final Set<Class<?>> set = new HashSet<Class<?>>(EXCEPTIONS.keySet());
        set.addAll(PARENT_EXCEPTIONS.keySet());
        return set;
    }

    public void cleanUp(@Observes final EventContext<AfterClass> event) throws Exception {
        EXCEPTIONS.clear();
        PARENT_EXCEPTIONS.clear();
    }
}
