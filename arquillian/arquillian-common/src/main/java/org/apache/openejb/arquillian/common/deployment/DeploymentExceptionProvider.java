package org.apache.openejb.arquillian.common.deployment;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

public class DeploymentExceptionProvider implements ResourceProvider {
    private Class<?> lastType;

    @Override
    public boolean canProvide(final Class<?> type) {
        if (DeploymentExceptionObserver.availableExceptionTypes().contains(type)) {
            lastType = type;
            return true;
        }
        lastType = null;
        return false;
    }

    @Override
    public Object lookup(final ArquillianResource resource, final Annotation... qualifiers) {
        return DeploymentExceptionObserver.getExceptions(lastType);
    }
}
