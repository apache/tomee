package org.apache.openejb.rest;

import org.apache.openejb.core.ivm.naming.AbstractThreadLocalProxy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class ThreadLocalProviders extends AbstractThreadLocalProxy<Providers> implements Providers {
    @Override
    public <T> ContextResolver<T> getContextResolver(Class<T> rawType, MediaType mediaType) {
        return get().getContextResolver(rawType, mediaType);
    }

    @Override
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> rawType) {
        return get().getExceptionMapper(rawType);
    }

    @Override
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return get().getMessageBodyReader(rawType, genericType, annotations, mediaType);
    }

    @Override
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return get().getMessageBodyWriter(rawType, genericType, annotations, mediaType);
    }
}
