package org.apache.tomee.microprofile.metrics;

import io.smallrye.metrics.jaxrs.JaxRsMetricsFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class MetricsRestFilter implements ContainerRequestFilter, ContainerResponseFilter, Providers {

    private final JaxRsMetricsFilter delegate = new JaxRsMetricsFilter();

    public MetricsRestFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        delegate.filter(requestContext);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        delegate.filter(requestContext, responseContext);
    }

    @Override
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return null;
    }

    @Override
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return null;
    }

    @Override
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
        return null;
    }

    @Override
    public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
        return null;
    }
}
