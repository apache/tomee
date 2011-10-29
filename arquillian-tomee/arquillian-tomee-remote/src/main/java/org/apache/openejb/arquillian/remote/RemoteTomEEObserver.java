package org.apache.openejb.arquillian.remote;

import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author rmannibucau
 */
public class RemoteTomEEObserver {
    @Inject @SuiteScoped private InstanceProducer<BeanManager> beanManager;
    @Inject @SuiteScoped private InstanceProducer<Context> context;

    public void beforeSuite(@Observes BeforeSuite event) {
        beanManager.set(ThreadSingletonServiceImpl.get().getBeanManagerImpl());
        try {
            context.set(new InitialContext());
        } catch (NamingException e) {
            // no-op
        }
    }
}
