package org.apache.tomee.microprofile.opentelemetry;

import io.smallrye.opentelemetry.implementation.cdi.WithSpanInterceptorBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

@ApplicationScoped
public class WithSpanInterceptorProducer {

    @Inject
    private BeanManager beanManager;

    @Produces
    public WithSpanInterceptorBean createWithSpanInterceptorBean() {
        return new WithSpanInterceptorBean(beanManager); //This is a hack as the actual interceptor bean from Smallrye has no default constructor breaking OWB.
    }
}