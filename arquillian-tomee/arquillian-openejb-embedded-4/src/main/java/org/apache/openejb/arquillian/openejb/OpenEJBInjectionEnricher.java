package org.apache.openejb.arquillian.openejb;

import java.lang.reflect.Method;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.webbeans.inject.OWBInjector;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;

public class OpenEJBInjectionEnricher implements TestEnricher {
    @Inject
    @DeploymentScoped
    private Instance<AppContext> appContext;

    @Inject
    @ContainerScoped
    private Instance<ContainerSystem> containerSystem;

    @Override
    public void enrich(final Object testInstance) {
        try {
            OWBInjector beanInjector = new OWBInjector(appContext.get().getWebBeansContext());
            beanInjector.inject(testInstance);
        } catch (Throwable t) {
            // ignored
        }

        final BeanContext context = containerSystem.get().getBeanContext(testInstance.getClass().getName());
        if (context != null) {
            ThreadContext callContext = new ThreadContext(context, null, Operation.INJECTION);
            ThreadContext oldContext = ThreadContext.enter(callContext);
            try {
                final InjectionProcessor processor = new InjectionProcessor<Object>(testInstance, context.getInjections(), context.getJndiContext());
                processor.createInstance();
            } catch (OpenEJBException e) {
                // ignored
            } finally {
                ThreadContext.exit(oldContext);
            }
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
